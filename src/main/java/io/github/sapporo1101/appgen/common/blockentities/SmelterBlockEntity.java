package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.config.*;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.util.ConfigManager;
import appeng.util.SettingsFrom;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;
import com.glodblock.github.glodium.util.GlodUtil;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blocks.SmelterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class SmelterBlockEntity extends AENetworkedPoweredBlockEntity implements RecipeCraftingHolder, IGridTickable, IUpgradeableObject, IConfigurableObject {

    private static final int POWER_MAXIMUM_AMOUNT = 10_000;
    private static final int AE_PER_TICK = 20;
    private static final int STACK_SIZE = 64;

    private final RecipeType<SmeltingRecipe> recipeType = RecipeType.SMELTING;
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck = RecipeManager.createCheck(recipeType);

    private final InternalInventory inputInv = new AppEngInternalInventory(this, 1, STACK_SIZE);
    private final InternalInventory outputInv = new AppEngInternalInventory(this, 1, STACK_SIZE);
    private final InternalInventory inv = new CombinedInternalInventory(this.inputInv, this.outputInv);
    private final FilteredInternalInventory inputExposed = new FilteredInternalInventory(this.inputInv, AEItemFilters.INSERT_ONLY);
    private final FilteredInternalInventory outputExposed = new FilteredInternalInventory(this.outputInv, AEItemFilters.EXTRACT_ONLY);
    private final InternalInventory invExposed = new CombinedInternalInventory(this.inputExposed, this.outputExposed);
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AGSingletons.SMELTER, 4, this::saveChanges);
    private final ConfigManager configManager = new ConfigManager(this::onConfigChanged);

    private final Set<Direction> outputSides = EnumSet.noneOf(Direction.class);

    private boolean hasWork = false;
    private int maxProgress = 0;
    private int progress = 0;
    public boolean showWarning = false;

    public SmelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(GlodUtil.getTileType(SmelterBlockEntity.class, SmelterBlockEntity::new, AGSingletons.SMELTER), pos, blockState);
        this.getMainNode().setIdlePowerUsage(0).addService(IGridTickable.class, this);
        this.setInternalMaxPower(POWER_MAXIMUM_AMOUNT);
        this.configManager.registerSetting(Settings.AUTO_EXPORT, YesNo.NO);
    }

    private void updateBlockState(boolean working) {
        if (this.level != null && !this.notLoaded() && !this.isRemoved()) {
            BlockState current = this.level.getBlockState(this.worldPosition);
            if (current.getBlock() instanceof SmelterBlock) {
                BlockState newState = current.setValue(SmelterBlock.WORKING, working);
                if (current != newState) {
                    this.level.setBlock(this.worldPosition, newState, 2);
                }
            }
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        var nbt = input.get(AGSingletons.EXTRA_SETTING);
        if (nbt != null) {
            this.outputSides.clear();
            for (var side : nbt.getList("output_side", CompoundTag.TAG_STRING)) {
                this.outputSides.add(Direction.byName(side.getAsString()));
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder output, @Nullable Player player) {
        super.exportSettings(mode, output, player);
        if (mode == SettingsFrom.MEMORY_CARD) {
            var nbt = new CompoundTag();
            var sides = new ListTag();
            for (var side : this.outputSides) {
                sides.add(StringTag.valueOf(side.getName()));
            }
            nbt.put("output_side", sides);
            output.set(AGSingletons.EXTRA_SETTING, nbt);
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putInt("max_smelting_progress", maxProgress);
        data.putInt("smelting_progress", progress);
        data.putBoolean("has_smelting_work", hasWork);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        this.configManager.writeToNBT(data, registries);
        var sides = new ListTag();
        for (var side : this.outputSides) {
            sides.add(StringTag.valueOf(side.getName()));
        }
        data.put("output_side", sides);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.maxProgress = data.getInt("max_smelting_progress");
        this.setProgress(data.getInt("smelting_progress"));
        this.hasWork = data.getBoolean("has_smelting_work");
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.configManager.readFromNBT(data, registries);
        this.outputSides.clear();
        if (data.contains("output_side")) {
            var list = data.getList("output_side", CompoundTag.TAG_STRING);
            for (var name : list) {
                this.outputSides.add(Direction.byName(name.getAsString()));
            }
        } else {
            this.outputSides.addAll(List.of(Direction.values()));
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        drops.add(this.inputInv.getStackInSlot(0));
        drops.add(this.outputInv.getStackInSlot(0));
        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);
        if (this.level == null) return;
        ItemStack inputStack = this.inputInv.getStackInSlot(0);
        ItemStack outputStack = this.outputInv.getStackInSlot(0);
        if (canSmelt(this.level.registryAccess(), getRecipeHolder(this.level, inputStack, this), inputStack, outputStack, this)) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
        }
    }

    private void onConfigChanged(IConfigManager manager, Setting<?> setting) {
        if (setting == Settings.AUTO_EXPORT) this.onOutputSideChanged();
    }

    private void onOutputSideChanged() {
        if (this.hasAutoExportWork()) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
        }
        this.saveChanges();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Inscriber, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getMainNode().isOnline()) {
            this.setWorking(false);
            this.saveChanges();
            return TickRateModulation.SLOWER;
        }

        int oldMaxProgress = this.maxProgress;
        int oldProgress = this.progress;
        boolean oldHasWork = this.hasWork;
        ItemStack inputStack = this.inputInv.getStackInSlot(0);
        ItemStack outputStack = this.outputInv.getStackInSlot(0);
        if (this.level == null) return TickRateModulation.SAME;
        if (!this.hasAutoExportWork() && !this.hasWork && inputStack.isEmpty()) return TickRateModulation.SLEEP;
        RecipeHolder<?> recipeholder = getRecipeHolder(this.level, inputStack, this);

        if (!this.hasWork && canSmelt(this.level.registryAccess(), recipeholder, inputStack, outputStack, this)) {
            this.setWorking(true);
            this.maxProgress = getMaxProgress(this.level, this);
        }

        if (this.hasWork && canSmelt(level.registryAccess(), recipeholder, inputStack, outputStack, this)) {
            this.getMainNode().ifPresent(grid -> useEnergy(grid, this, ticksSinceLastCall));
            if (this.progress >= this.maxProgress) {
                this.setProgress(0);
                if (smelt(level.registryAccess(), recipeholder, inputStack, outputStack, this) && !canSmelt(level.registryAccess(), recipeholder, inputStack, outputStack, this)) {
                    this.setWorking(false);
                }
            }
        } else {
            this.setProgress(0);
            this.setWorking(false);
        }
        if (oldMaxProgress != this.maxProgress || oldProgress != this.progress || oldHasWork != this.hasWork) {
            this.saveChanges();
        }
        if (this.pushOutResult()) return TickRateModulation.URGENT;
        return canSmelt(level.registryAccess(), recipeholder, inputStack, outputStack, this) ? TickRateModulation.URGENT : (this.hasAutoExportWork() ? TickRateModulation.SLOWER : TickRateModulation.SLEEP);
    }

    private static RecipeHolder<?> getRecipeHolder(Level level, ItemStack inputStack, SmelterBlockEntity smelter) {
        RecipeHolder<?> recipeholder;
        if (!inputStack.isEmpty()) {
            recipeholder = smelter.quickCheck.getRecipeFor(new SingleRecipeInput(inputStack), level).orElse(null);
        } else {
            recipeholder = null;
        }
        return recipeholder;
    }

    private static boolean canSmelt(RegistryAccess registryAccess, @javax.annotation.Nullable RecipeHolder<?> recipe, ItemStack inputStack, ItemStack outputStack, SmelterBlockEntity smelter) {
        if (!inputStack.isEmpty() && recipe != null) {
            ItemStack resultStack = ((AbstractCookingRecipe) recipe.value()).assemble(new SingleRecipeInput(smelter.getInputInv().getStackInSlot(0)), registryAccess);
            if (resultStack.isEmpty()) {
                return false;
            } else {
                if (outputStack.isEmpty()) {
                    return true;
                } else if (!ItemStack.isSameItemSameComponents(outputStack, resultStack)) {
                    return false;
                } else {
                    return outputStack.getCount() + resultStack.getCount() <= STACK_SIZE && outputStack.getCount() + resultStack.getCount() <= outputStack.getMaxStackSize() || outputStack.getCount() + resultStack.getCount() <= resultStack.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    private static boolean smelt(RegistryAccess registryAccess, @javax.annotation.Nullable RecipeHolder<?> recipe, ItemStack inputStack, ItemStack outputStack, SmelterBlockEntity smelter) {
        if (recipe != null && canSmelt(registryAccess, recipe, inputStack, outputStack, smelter)) {
            ItemStack resultStack = ((AbstractCookingRecipe) recipe.value()).assemble(new SingleRecipeInput(inputStack), registryAccess);
            int smeltingCount = smelter.isUpgradedWith(AGSingletons.STACK_SMELTING_CARD) ? getMaxSmeltingCount(inputStack, outputStack, resultStack) : 1;
            if (smeltingCount <= 0 || smeltingCount > inputStack.getCount()) return false;
            if (outputStack.isEmpty()) {
                resultStack.setCount(Math.min(resultStack.getCount() * smeltingCount, Math.min(resultStack.getMaxStackSize(), STACK_SIZE)));
                smelter.outputInv.setItemDirect(0, resultStack.copy());
            } else if (ItemStack.isSameItemSameComponents(outputStack, resultStack)) {
                outputStack.grow(Math.min(resultStack.getCount() * smeltingCount, Math.min(outputStack.getMaxStackSize(), STACK_SIZE) - outputStack.getCount()));
                smelter.outputInv.setItemDirect(0, outputStack);
            } else {
                return false;
            }
            inputStack.shrink(smeltingCount);
            smelter.inputInv.setItemDirect(0, inputStack);
            return true;
        } else {
            return false;
        }
    }

    private static int getMaxSmeltingCount(ItemStack inputStack, ItemStack outputStack, ItemStack resultStack) {
        if (outputStack.isEmpty()) {
            return Math.min(inputStack.getCount(), Math.min(resultStack.getMaxStackSize(), STACK_SIZE) / resultStack.getCount());
        } else if (ItemStack.isSameItemSameComponents(outputStack, resultStack)) {
            return Math.min(inputStack.getCount(), (Math.min(outputStack.getMaxStackSize(), STACK_SIZE) - outputStack.getCount()) / resultStack.getCount());
        } else {
            return 0;
        }
    }

    private static void useEnergy(IGrid grid, SmelterBlockEntity smelter, int ticks) {
        IEnergyService eg = grid.getEnergyService();
        IEnergySource src = smelter;

        final int speedFactor =
                switch (smelter.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
                    case 1 -> 2; // 100 ticks
                    case 2 -> 4; // 50 ticks
                    case 3 -> 8; // 25 ticks
                    case 4 -> 16; // 12 ticks
                    default -> 1; // 200 ticks
                };

        final int progressReq = smelter.maxProgress - smelter.getProgress();
        final float powerRatio = progressReq < speedFactor ? (float) progressReq / speedFactor : 1;
        final int requiredTicks = Mth.ceil((float) smelter.maxProgress / speedFactor);
        final int aeConsumption = Mth.floor(((float) getAePerOperation(smelter.level, smelter) / requiredTicks) * powerRatio * ticks);
        final double powerThreshold = aeConsumption - 0.01;

        double powerReq = smelter.extractAEPower(aeConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);

        if (powerReq <= powerThreshold) {
            src = eg;
            var oldPowerReq = powerReq;
            powerReq = eg.extractAEPower(aeConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (oldPowerReq > powerReq) {
                src = smelter;
                powerReq = oldPowerReq;
            }
        }

        if (powerReq > powerThreshold) {
            src.extractAEPower(aeConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);
            smelter.addProgress(speedFactor);
            smelter.showWarning = false;
        } else if (powerReq != 0) {
            var progressRatio = src == smelter
                    ? powerReq / aeConsumption
                    : (powerReq - 10 * eg.getIdlePowerUsage()) / aeConsumption;
            var factor = Mth.floor(progressRatio * speedFactor);

            if (factor > 1) {
                var extracted = src.extractAEPower(
                        (double) (aeConsumption * factor) / speedFactor,
                        Actionable.MODULATE,
                        PowerMultiplier.CONFIG);
                var actualFactor = (int) Math.floor(extracted / aeConsumption * speedFactor);
                smelter.addProgress(actualFactor);
            }

            smelter.showWarning = true;
        }

    }

    private boolean hasAutoExportWork() {
        return !this.outputInv.getStackInSlot(0).isEmpty() && configManager.getSetting(Settings.AUTO_EXPORT) == YesNo.YES && !this.outputSides.isEmpty();
    }

    private static int getMaxProgress(Level level, SmelterBlockEntity smelter) {
        SingleRecipeInput singlerecipeinput = new SingleRecipeInput(smelter.inputInv.getStackInSlot(0));
        return smelter.quickCheck.getRecipeFor(singlerecipeinput, level).map((recipeHolder) -> recipeHolder.value().getCookingTime()).orElse(200);
    }

    private static int getAePerOperation(Level level, SmelterBlockEntity smelter) {
        return (smelter.isUpgradedWith(AGSingletons.STACK_SMELTING_CARD) ? 64 : 1) * AE_PER_TICK * getMaxProgress(level, smelter);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction side) {
        return this.invExposed;
    }

    public InternalInventory getInputInv() {
        return this.inputInv;
    }

    public InternalInventory getOutputExposed() {
        return this.outputExposed;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }

        return super.getSubInventory(id);
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    public void setWorking(boolean work) {
        if (work != this.hasWork) {
            this.updateBlockState(work);
            this.markForUpdate();
        }

        this.hasWork = work;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getProgress() {
        return this.progress;
    }

    private void addProgress(int delta) {
        this.progress += delta;
    }

    private void setProgress(int progress) {
        this.progress = progress;
    }

    public Set<Direction> getOutputSides() {
        return this.outputSides;
    }

    public void setOutputSide(Direction side, boolean value) {
        if (value) {
            this.outputSides.add(side);
        } else {
            this.outputSides.remove(side);
        }
        this.onOutputSideChanged();
    }

    private boolean pushOutResult() {
        if (!this.hasAutoExportWork() || this.level == null) {
            return false;
        }

        for (Direction dir : outputSides) {
            BlockPos targetPos = this.getBlockPos().relative(dir);
            IItemHandler itemStorage = this.level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, dir.getOpposite());

            boolean movedStacks = false;
            if (itemStorage != null) {
                if (this.outputInv.getStackInSlot(0) != null && !this.outputInv.getStackInSlot(0).isEmpty()) {
                    ItemStack remainingStack = this.outputInv.extractItem(0, 64, false);
                    for (int i = 0; i < itemStorage.getSlots(); i++) {
                        if (remainingStack.getCount() <= 0) break;
                        remainingStack = itemStorage.insertItem(i, remainingStack, false);
                    }
                    this.outputInv.insertItem(0, remainingStack, false);
                    movedStacks = !remainingStack.isEmpty();
                }
            }

            if (movedStacks) return true;

        }
        return false;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }
}
