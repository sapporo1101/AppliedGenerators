package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKeyType;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.SettingsFrom;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.xmod.ExternalTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class FluxCellBaseBlockEntity extends AEBaseBlockEntity implements BlockEntityTicker<FluxCellBaseBlockEntity> {
    protected final GenericStackInv feInv;
    protected final Set<Direction> outputSides = EnumSet.noneOf(Direction.class);

    public FluxCellBaseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.feInv = this.createInv();
        this.feInv.setCapacity(AEKeyType.items(), 0);
        this.feInv.setCapacity(AEKeyType.fluids(), 0);
        if (ExternalTypes.GAS != null) this.feInv.setCapacity(ExternalTypes.GAS, 0);
        if (ExternalTypes.MANA != null) this.feInv.setCapacity(ExternalTypes.MANA, 0);
        if (ExternalTypes.FLUX != null) this.feInv.setCapacity(ExternalTypes.FLUX, this.getFluxCapacity());
        if (ExternalTypes.SOURCE != null) this.feInv.setCapacity(ExternalTypes.SOURCE, 0);
    }

    protected GenericStackInv createInv() {
        return new GenericStackInv(this::setChanged, 36);
    }

    protected abstract long getFluxCapacity();

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
        this.feInv.writeToChildTag(data, "fe_inv", registries);
        var sides = new ListTag();
        for (var side : this.outputSides) {
            sides.add(StringTag.valueOf(side.getName()));
        }
        data.put("output_side", sides);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.feInv.readFromChildTag(data, "fe_inv", registries);
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
    public void clearContent() {
        super.clearContent();
        this.feInv.clear();
    }

    public Set<Direction> getOutputSides() {
        return this.outputSides;
    }

    public GenericStackInv getGenericInv() {
        return this.feInv;
    }

    public long getStoredFE() {
        long total = 0;
        for (int i = 0; i < this.feInv.size(); i++) {
            total += this.feInv.getAmount(i);
        }
        return total;
    }

    @Override
    public void tick(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull FluxCellBaseBlockEntity be) {
        if (!this.feInv.isEmpty() && !this.outputSides.isEmpty()) this.sendFEToAdjacentBlock();
    }

    private void sendFEToAdjacentBlock() {
        if (this.level == null) return;

        for (Direction dir : this.outputSides) {
            if (this.feInv.isEmpty()) break;
            BlockPos targetPos = this.getBlockPos().relative(dir);
            IEnergyStorage storage = this.level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, dir.getOpposite());
            if (storage != null && storage.canReceive()) {
                int canInsert = storage.receiveEnergy(Integer.MAX_VALUE, true);
                if (canInsert <= 0) continue;
                int extracted = Math.toIntExact(this.feInv.extract(FluxKey.of(EnergyType.FE), canInsert, Actionable.MODULATE, null));
                storage.receiveEnergy(extracted, false);
            }
        }
    }

    public IEnergyStorage getEnergyStorage(Direction dir) {
        return new FluxCellEnergyStorage(this.feInv, this.outputSides, dir);
    }

    private record FluxCellEnergyStorage(
            GenericStackInv inv,
            Set<Direction> outputSides,
            Direction dir
    ) implements IEnergyStorage {

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            if (!canReceive()) return 0;
            if (simulate) {
                // Simulate the insertion of energy
                return Math.toIntExact(this.inv.insert(FluxKey.of(EnergyType.FE), toReceive, Actionable.SIMULATE, null));
            } else {
                // Actually insert energy
                return Math.toIntExact(this.inv.insert(FluxKey.of(EnergyType.FE), toReceive, Actionable.MODULATE, null));
            }
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            if (simulate) {
                // Simulate the extraction of energy
                return Math.toIntExact(this.inv.extract(FluxKey.of(EnergyType.FE), toExtract, Actionable.SIMULATE, null));
            } else {
                // Actually extract energy
                return Math.toIntExact(this.inv.extract(FluxKey.of(EnergyType.FE), toExtract, Actionable.MODULATE, null));
            }
        }

        @Override
        public int getEnergyStored() {
            int total = 0;
            for (int i = 0; i < this.inv.size(); i++) {
                total += (int) this.inv.getAmount(i);
            }
            return total;
        }

        @Override
        public int getMaxEnergyStored() {
            return Math.toIntExact(Math.min(this.inv.getCapacity(ExternalTypes.FLUX) * this.inv.size(), Integer.MAX_VALUE));
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            if (this.dir == null) return this.outputSides.isEmpty();
            return !this.outputSides.contains(this.dir);
        }
    }
}
