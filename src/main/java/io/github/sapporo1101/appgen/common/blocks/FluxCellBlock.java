package io.github.sapporo1101.appgen.common.blocks;

import io.github.sapporo1101.appgen.common.blockentities.FluxCellBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class FluxCellBlock<U extends FluxCellBlockEntity> extends FluxCellBaseBlock<U> {
    public static final int MAX_FULLNESS = 4;
    public static final IntegerProperty FE_STORAGE = IntegerProperty.create("fullness", 0, MAX_FULLNESS);

    public FluxCellBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.defaultBlockState().setValue(FE_STORAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, @NotNull BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FE_STORAGE);
    }

    public static class Standard extends FluxCellBlock<FluxCellBlockEntity.Standard> {
        public static final long MAX_CAPACITY = 1048576;

        public Standard(Properties props) {
            super(props);
        }
    }

    public static class Dense extends FluxCellBlock<FluxCellBlockEntity.Dense> {
        public static final long MAX_CAPACITY = 16777216;

        public Dense(Properties props) {
            super(props);
        }
    }
}
