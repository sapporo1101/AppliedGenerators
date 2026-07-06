package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.config.Actionable;
import appeng.api.config.YesNo;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.items.materials.MaterialItem;
import appeng.me.helpers.MachineSource;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemDefinitionFilter;
import appeng.util.inv.filter.IAEItemFilter;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import io.github.sapporo1101.appgen.api.AAESettings;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blocks.SingularityGeneratorBlock;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class SingularityGeneratorBlockEntity extends AENetworkedInvBlockEntity implements IGridTickable, IUpgradeableObject, IConfigurableObject {
    public static final @NotNull AEKey FE_KEY = FluxKey.of(EnergyType.FE);
    public static final MaterialItem SINGULARITY = AEItems.SINGULARITY.asItem();

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1, 64, new AEItemDefinitionFilter(AEItems.SINGULARITY));
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new SingularitySlotFilter());
    private final IUpgradeInventory upgrades;
    private final IConfigManager configManager;
    private final MachineSource source = new MachineSource(this);
    private final DirectionSet outputSides = new DirectionSet(List.of());

    private long generatableFE;
    private double lastGeneratePerTick = 0;
    public boolean isOn;

    public <T extends SingularityGeneratorBlock<?>> SingularityGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState, DeferredBlock<@NotNull T> block) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0F).setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, this);
        this.upgrades = UpgradeInventories.forMachine(block, 4, this::upgradeSetChanged);
        this.configManager = IConfigManager.builder(this::onConfigChanged).registerSetting(AAESettings.ME_EXPORT, YesNo.YES).build();

        this.generatableFE = 0;
    }

    abstract long getBaseGeneratePerTick();

    abstract long getBaseFEPerSingularity();

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        var sides = input.get(AGSingletons.DIRECTION_SET);
        if (sides != null) {
            this.outputSides.reload(sides.asList());
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder output, @Nullable Player player) {
        super.exportSettings(mode, output, player);
        if (mode == SettingsFrom.MEMORY_CARD) {
            output.set(AGSingletons.DIRECTION_SET, this.outputSides);
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.upgrades.writeToNBT(output, "upgrades");
        this.configManager.writeToNBT(output);
        this.outputSides.save(output, "output_side");
        output.putLong("generatableFE", this.getGeneratableFE());
    }

    public void loadTag(ValueInput input) {
        super.loadTag(input);
        this.upgrades.readFromNBT(input, "upgrades");
        this.configManager.readFromNBT(input);
        this.outputSides.load(input, "output_side");
        this.setGeneratableFE(input.getLongOr("generatableFE", 0));
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(Identifier id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }

        return super.getSubInventory(id);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction facing) {
        return this.invExt;
    }

    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.updateBlockEntity(this.shouldUpdateIsOn());
        if (this.getGeneratableFE() <= 0 && this.canEatFuel()) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
        }
    }

    private void upgradeSetChanged() {
        this.saveChanges();
    }

    private void onConfigChanged() {
        this.getMainNode().ifPresent((grid, node) -> {
            if (this.getGeneratableFE() > 0 || this.canEatFuel()) {
                grid.getTickManager().wakeDevice(node);
            } else {
                grid.getTickManager().sleepDevice(node);
            }
        });
        this.saveChanges();
    }

    public boolean canEatFuel() {
        ItemStack stack = this.inv.getStackInSlot(0);
        if (!stack.isEmpty() && stack.is(SINGULARITY)) {
            return stack.getCount() > 0;
        }
        return false;
    }

    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (int index = 0; index < this.inv.size(); index++) {
            ItemStack stack = this.inv.getStackInSlot(index);
            if (!stack.isEmpty()) drops.add(stack);
        }
        for (ItemStack upgrade : this.upgrades) drops.add(upgrade);
    }

    public void clearContent() {
        super.clearContent();
        this.inv.clear();
        this.upgrades.clear();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public TickingRequest getTickingRequest(IGridNode node) {
        if (this.getGeneratableFE() <= 0) {
            this.charge();
        }

        return new TickingRequest(TickRates.VibrationChamber, this.getGeneratableFE() <= 0);
    }

    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.getGeneratableFE() <= 0) {
            this.charge();
            this.lastGeneratePerTick = 0;
            if (this.getGeneratableFE() > 0) {
                return TickRateModulation.URGENT;
            } else {
                return TickRateModulation.SLEEP;
            }
        } else {
            long newFE = Math.min((long) ticksSinceLastCall * this.getGeneratePerTick(), this.getGeneratableFE());
            long sent = this.configManager.getSetting(AAESettings.ME_EXPORT) == YesNo.YES ? this.sendFEToNetwork(newFE) : this.sendFEToAdjacentBlock(newFE);
            this.lastGeneratePerTick = sent / (double) ticksSinceLastCall;
            return sent > 0 ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        }
    }

    private void charge() {
        ItemStack stack = this.inv.getStackInSlot(0);
        if (!stack.isEmpty() && stack.is(SINGULARITY)) {
            if (stack.getCount() > 0) {
                this.setGeneratableFE(this.getGeneratableFE() + this.getFEPerSingularity());
                if (stack.getCount() <= 1) {
                    this.inv.setItemDirect(0, ItemStack.EMPTY);
                } else {
                    stack.shrink(1);
                    this.inv.setItemDirect(0, stack);
                }

                this.saveChanges();
            }
        }

        if (this.getGeneratableFE() > 0) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
        }

        this.updateBlockEntity(this.shouldUpdateIsOn());
    }

    public boolean shouldUpdateIsOn() {
        return !this.isOn && (this.getGeneratableFE() > 0 || this.canEatFuel()) || this.isOn && this.getGeneratableFE() <= 0 && !this.canEatFuel();
    }

    public void updateBlockEntity(boolean condition) {
        if (!condition) return;
        this.markForUpdate();
        if (this.hasLevel()) {
            Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
        }
    }

    public long getGeneratableFE() {
        return this.generatableFE;
    }

    private void setGeneratableFE(long generatableFE) {
        this.generatableFE = generatableFE;
    }

    public long getGeneratePerTick() {
        if (this.upgrades == null) {
            return this.getBaseGeneratePerTick();
        }
        double upgradeMultiplier = 1 + this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD) * 0.5;
        return (long) (this.getBaseGeneratePerTick() * upgradeMultiplier);
    }

    public long getFEPerSingularity() {
        if (this.upgrades == null) {
            return this.getBaseFEPerSingularity();
        }
        double upgradeMultiplier = 1 + this.upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD) * 0.5;
        return (long) (getBaseFEPerSingularity() * upgradeMultiplier);
    }

    public long sendFEToNetwork(long amount) {
        if (this.getGridNode() == null) return 0;

        IGrid grid = this.getGridNode().getGrid();
        IStorageService storage = grid.getStorageService();

        long inserted = storage.getInventory().insert(FE_KEY, amount, Actionable.MODULATE, this.source);
        this.setGeneratableFE(Math.max(0, this.getGeneratableFE() - inserted));

        return inserted;
    }

    private long sendFEToAdjacentBlock(long amount) {
        if (this.level == null) return 0;

        long sending = amount;
        sides:
        for (Direction dir : this.getOutputSides()) {
            if (sending <= 0) break;
            BlockPos targetPos = this.getBlockPos().relative(dir);
            EnergyHandler storage = this.level.getCapability(Capabilities.Energy.BLOCK, targetPos, dir.getOpposite());
            if (storage != null) {
                while (sending > 0) {
                    int canInsert;
                    try (Transaction simulation = Transaction.openRoot()) {
                        int batch = (int) Math.min(sending, Integer.MAX_VALUE);
                        canInsert = storage.insert(batch, simulation);
                    }

                    if (canInsert <= 0) continue sides;

                    try (Transaction transaction = Transaction.openRoot()) {
                        int inserted = storage.insert(canInsert, transaction);
                        if (inserted > 0) {
                            transaction.commit();
                            sending -= inserted;
                        }
                    }
                }

            }
        }
        return amount - sending;
    }

    public double getLastGeneratePerTick() {
        return lastGeneratePerTick;
    }

    public Set<Direction> getOutputSides() {
        return this.outputSides.asSet();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    private static class SingularitySlotFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return !inv.getStackInSlot(slot).is(SINGULARITY);
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return stack.is(SINGULARITY);
        }
    }

    public static class SG1k extends SingularityGeneratorBlockEntity {

        public SG1k(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_1K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 200;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 1_000_000;
        }
    }

    public static class SG4k extends SingularityGeneratorBlockEntity {

        public SG4k(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_4K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 800;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 4_000_000;
        }
    }

    public static class SG16k extends SingularityGeneratorBlockEntity {

        public SG16k(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_16K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 3_200;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 16_000_000;
        }
    }

    public static class SG64k extends SingularityGeneratorBlockEntity {

        public SG64k(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_64K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 12_800;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 64_000_000;
        }
    }

    public static class SG256k extends SingularityGeneratorBlockEntity {

        public SG256k(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_256K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 51_200;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 256_000_000;
        }
    }

    public static class SG1m extends SingularityGeneratorBlockEntity {

        public SG1m(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_1M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 204_800;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 1_024_000_000;
        }
    }

    public static class SG4m extends SingularityGeneratorBlockEntity {

        public SG4m(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_4M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 819_200;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 4_096_000_000L;
        }
    }

    public static class SG16m extends SingularityGeneratorBlockEntity {

        public SG16m(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_16M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 3_276_800;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 16_384_000_000L;
        }
    }

    public static class SG64m extends SingularityGeneratorBlockEntity {

        public SG64m(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_64M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 13_107_200;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 65_536_000_000L;
        }
    }

    public static class SG256m extends SingularityGeneratorBlockEntity {

        public SG256m(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState, AGSingletons.SINGULARITY_GENERATOR_256M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 52_428_800;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 262_144_000_000L;
        }
    }
}