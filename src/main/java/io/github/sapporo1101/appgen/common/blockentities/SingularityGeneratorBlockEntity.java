package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.config.YesNo;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
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
import com.glodblock.github.glodium.util.GlodUtil;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blockentities.interfaces.IEnergyExtractor;
import io.github.sapporo1101.appgen.common.blocks.SingularityGeneratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class SingularityGeneratorBlockEntity extends AENetworkedInvBlockEntity implements IGridTickable, IUpgradeableObject, IConfigurableObject, IEnergyExtractor {
    public static final MaterialItem SINGULARITY = AEItems.SINGULARITY.asItem();

    private final InternalInventory inv = new AppEngInternalInventory(this, 1, 64, new AEItemDefinitionFilter(AEItems.SINGULARITY));
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new SingularitySlotFilter());
    private final IUpgradeInventory upgrades;
    private final IConfigManager configManager;
    private final MachineSource source = new MachineSource(this);
    private final Set<Direction> outputSides = EnumSet.noneOf(Direction.class);

    private long generatableFE;
    private double lastGeneratePerTick = 0;
    public boolean isOn;

    public SingularityGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState, SingularityGeneratorBlock<?> block) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0F).setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, this);
        this.upgrades = UpgradeInventories.forMachine(block, 4, this::upgradeSetChanged);
        this.configManager = IConfigManager.builder(this::onConfigChanged).registerSetting(AGSettings.ME_EXPORT, YesNo.YES).build();

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

    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        this.configManager.writeToNBT(data, registries);
        data.putLong("generatableFE", this.getGeneratableFE());
        var sides = new ListTag();
        for (var side : this.outputSides) {
            sides.add(StringTag.valueOf(side.getName()));
        }
        data.put("output_side", sides);
    }

    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.setGeneratableFE(data.getLong("generatableFE"));
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
            long sent = this.configManager.getSetting(AGSettings.ME_EXPORT) == YesNo.YES ? this.sendFEToNetwork(newFE) : this.sendFEToAdjacentBlock(newFE);
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
        long inserted = this.sendFEToNetwork(amount, this.getGridNode(), this.source);
        this.setGeneratableFE(Math.max(0, this.getGeneratableFE() - inserted));
        return inserted;
    }

    private long sendFEToAdjacentBlock(long amount) {
        return this.sendFEToAdjacentBlock(amount, this.outputSides, this.getBlockPos(), this.level);
    }

    public double getLastGeneratePerTick() {
        return lastGeneratePerTick;
    }

    public Set<Direction> getOutputSides() {
        return this.outputSides;
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

        public SG1k(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG1k.class, SG1k::new, AGSingletons.SINGULARITY_GENERATOR_1K), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_1K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 100;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 10_000_000;
        }
    }

    public static class SG4k extends SingularityGeneratorBlockEntity {

        public SG4k(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG4k.class, SG4k::new, AGSingletons.SINGULARITY_GENERATOR_4K), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_4K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 400;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 20_000_000;
        }
    }

    public static class SG16k extends SingularityGeneratorBlockEntity {

        public SG16k(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG16k.class, SG16k::new, AGSingletons.SINGULARITY_GENERATOR_16K), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_16K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 1_600;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 40_000_000;
        }
    }

    public static class SG64k extends SingularityGeneratorBlockEntity {

        public SG64k(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG64k.class, SG64k::new, AGSingletons.SINGULARITY_GENERATOR_64K), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_64K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 6_400;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 80_000_000;
        }
    }

    public static class SG256k extends SingularityGeneratorBlockEntity {

        public SG256k(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG256k.class, SG256k::new, AGSingletons.SINGULARITY_GENERATOR_256K), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_256K);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 25_600;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 160_000_000;
        }
    }

    public static class SG1m extends SingularityGeneratorBlockEntity {

        public SG1m(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG1m.class, SG1m::new, AGSingletons.SINGULARITY_GENERATOR_1M), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_1M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 102_400;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 320_000_000;
        }
    }

    public static class SG4m extends SingularityGeneratorBlockEntity {

        public SG4m(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG4m.class, SG4m::new, AGSingletons.SINGULARITY_GENERATOR_4M), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_4M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 409_600;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 640_000_000;
        }
    }

    public static class SG16m extends SingularityGeneratorBlockEntity {

        public SG16m(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG16m.class, SG16m::new, AGSingletons.SINGULARITY_GENERATOR_16M), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_16M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 1_638_400;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 1_280_000_000;
        }
    }

    public static class SG64m extends SingularityGeneratorBlockEntity {

        public SG64m(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG64m.class, SG64m::new, AGSingletons.SINGULARITY_GENERATOR_64M), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_64M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 6_553_600;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 2_560_000_000L;
        }
    }

    public static class SG256m extends SingularityGeneratorBlockEntity {

        public SG256m(BlockPos pos, BlockState blockState) {
            super(GlodUtil.getTileType(SG256m.class, SG256m::new, AGSingletons.SINGULARITY_GENERATOR_256M), pos, blockState, AGSingletons.SINGULARITY_GENERATOR_256M);
        }

        @Override
        long getBaseGeneratePerTick() {
            return 26_214_400;
        }

        @Override
        long getBaseFEPerSingularity() {
            return 5_120_000_000L;
        }
    }
}