package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKeyType;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.SettingsFrom;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import io.github.sapporo1101.appgen.xmod.ExternalTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class FluxCellBaseBlockEntity extends AEBaseBlockEntity implements BlockEntityTicker<@NotNull FluxCellBaseBlockEntity> {
    protected final GenericStackInv feInv;
    protected final DirectionSet outputSides = new DirectionSet(List.of());

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
        this.feInv.writeToChildTag(output, "fe_inv");
        this.outputSides.save(output, "output_side");
    }

    @Override
    public void loadTag(ValueInput input) {
        super.loadTag(input);
        this.feInv.readFromChildTag(input, "fe_inv");
        this.outputSides.load(input, "output_side");
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.feInv.clear();
    }

    public Set<Direction> getOutputSides() {
        return this.outputSides.asSet();
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
        if (!this.feInv.isEmpty() && !this.getOutputSides().isEmpty()) this.sendFEToAdjacentBlock();
    }

    private void sendFEToAdjacentBlock() {
        if (this.level == null) return;

        for (Direction dir : this.getOutputSides()) {
            if (this.feInv.isEmpty()) break;
            BlockPos targetPos = this.getBlockPos().relative(dir);
            EnergyHandler storage = this.level.getCapability(Capabilities.Energy.BLOCK, targetPos, dir.getOpposite());
            if (storage != null) {
                int canInsert;
                try (Transaction simulation = Transaction.openRoot()) {
                    int sending = Math.toIntExact(this.feInv.extract(FluxKey.of(EnergyType.FE), Integer.MAX_VALUE, Actionable.SIMULATE, null));
                    canInsert = storage.insert(sending, simulation);
                }

                if (canInsert <= 0) continue;

                try (Transaction transaction = Transaction.openRoot()) {
                    int extracted = Math.toIntExact(this.feInv.extract(FluxKey.of(EnergyType.FE), canInsert, Actionable.MODULATE, null));
                    int inserted = storage.insert(extracted, transaction);
                    if (inserted > 0) transaction.commit();
                }
            }
        }
    }

    public EnergyHandler getEnergyStorage(Direction ignoredDir) {
        return new FluxCellEnergyHandler(this.feInv);
    }

    private record FluxCellEnergyHandler(
            GenericStackInv inv
    ) implements EnergyHandler {
        @Override
        public int insert(int amount, @NotNull TransactionContext transaction) {
            this.inv.updateSnapshots(transaction);
            int canInsert = Math.toIntExact(this.inv.insert(FluxKey.of(EnergyType.FE), amount, Actionable.SIMULATE, null));
            if (canInsert > 0) {
                this.inv.insert(FluxKey.of(EnergyType.FE), canInsert, Actionable.MODULATE, null);
            }
            return canInsert;
        }

        @Override
        public int extract(int amount, @NotNull TransactionContext transaction) {
            this.inv.updateSnapshots(transaction);
            int canExtract = Math.toIntExact(this.inv.extract(FluxKey.of(EnergyType.FE), amount, Actionable.SIMULATE, null));
            if (canExtract > 0) {
                this.inv.extract(FluxKey.of(EnergyType.FE), canExtract, Actionable.MODULATE, null);
            }
            return canExtract;
        }

        @Override
        public long getAmountAsLong() {
            long total = 0;
            for (int i = 0; i < this.inv.size(); i++) {
                total += this.inv.getAmount(i);
            }
            return total;
        }

        @Override
        public long getCapacityAsLong() {
            return this.inv.getCapacity(ExternalTypes.FLUX) * this.inv.size();
        }
    }

}
