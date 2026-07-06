package io.github.sapporo1101.appgen.common.blockentities;

import io.github.sapporo1101.appgen.common.blocks.FluxCellBlock;
import io.github.sapporo1101.appgen.xmod.ExternalTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static io.github.sapporo1101.appgen.common.blocks.FluxCellBlock.FE_STORAGE;
import static io.github.sapporo1101.appgen.common.blocks.FluxCellBlock.MAX_FULLNESS;

public abstract class FluxCellBlockEntity extends FluxCellBaseBlockEntity {

    public FluxCellBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public int getFullness() {
        long fluxAmount = 0;
        for (int i = 0; i < this.feInv.size(); i++) {
            fluxAmount += this.feInv.getAmount(i);
        }
        long maxFlux = this.feInv.getCapacity(ExternalTypes.FLUX) * this.feInv.size();
        if (maxFlux <= 0) return 1;
        double fluxPercentage = (double) fluxAmount / maxFlux;
        final int fullness;
        if (fluxPercentage < 0.25) {
            fullness = 0;
        } else if (fluxPercentage < 0.5) {
            fullness = 1;
        } else if (fluxPercentage < 0.75) {
            fullness = 2;
        } else if (fluxPercentage < 0.99) {
            fullness = 3;
        } else {
            fullness = MAX_FULLNESS;
        }
        return fullness;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level == null) return;
        this.level.setBlockAndUpdate(this.getBlockPos(), this.level.getBlockState(this.getBlockPos()).setValue(FE_STORAGE, this.getFullness()));
    }

    public static class Standard extends FluxCellBlockEntity {
        public Standard(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState);
        }

        @Override
        protected long getFluxCapacity() {
            return FluxCellBlock.Standard.MAX_CAPACITY;
        }
    }

    public static class Dense extends FluxCellBlockEntity {
        public Dense(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
            super(blockEntityType, pos, blockState);
        }

        @Override
        protected long getFluxCapacity() {
            return FluxCellBlock.Dense.MAX_CAPACITY;
        }
    }
}