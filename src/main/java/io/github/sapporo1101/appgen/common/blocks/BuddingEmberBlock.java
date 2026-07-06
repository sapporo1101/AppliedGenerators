package io.github.sapporo1101.appgen.common.blocks;

import appeng.block.AEBaseBlock;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blocks.interfaces.ISpecialDrop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuddingEmberBlock extends AEBaseBlock implements ISpecialDrop {

    public static final int GROWTH_CHANCE = 3;
    public static final int DECAY_CHANCE = 10;

    public BuddingEmberBlock(Properties props) {
        super(stoneProps(props)
                .strength(3, 8)
                .requiresCorrectToolForDrops()
                .randomTicks()
        );
    }

    @Override
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(GROWTH_CHANCE) != 0) return;
        // Try to grow cluster
        Direction direction = Direction.getRandom(randomSource);
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block newCluster;
        if (canClusterGrowAtState(targetState)) newCluster = AGSingletons.EMBER_BUD_SMALL.get();
        else newCluster = canClusterGrow(targetState, direction);
        if (newCluster == null) return;
        // Grow ember crystal
        BlockState newClusterState = newCluster.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getFluidState().getType() == Fluids.WATER);
        level.setBlockAndUpdate(targetPos, newClusterState);

        if (randomSource.nextInt(DECAY_CHANCE) != 0) return;

        Block newBlock = this.degradeBudding();
        level.setBlockAndUpdate(pos, newBlock.defaultBlockState());
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }

    @Nullable
    public static Block canClusterGrow(BlockState state, Direction side) {
        var cluster = state.getBlock();
        if (cluster instanceof EmberClusterBlock && cluster != AGSingletons.EMBER_CLUSTER.get()) {
            if (state.getValue(AmethystClusterBlock.FACING) == side) {
                if (cluster == AGSingletons.EMBER_BUD_SMALL.get()) return AGSingletons.EMBER_BUD_MEDIUM.get();
                if (cluster == AGSingletons.EMBER_BUD_MEDIUM.get()) return AGSingletons.EMBER_BUD_LARGE.get();
                if (cluster == AGSingletons.EMBER_BUD_LARGE.get()) return AGSingletons.EMBER_CLUSTER.get();
            }
        }
        return null;
    }

    public Block degradeBudding() {
        if (this == AGSingletons.BUDDING_EMBER_FLAWLESS.get()) return AGSingletons.BUDDING_EMBER_FLAWLESS.get();
        if (this == AGSingletons.BUDDING_EMBER_FLAWED.get()) return AGSingletons.BUDDING_EMBER_CHIPPED.get();
        if (this == AGSingletons.BUDDING_EMBER_CHIPPED.get()) return AGSingletons.BUDDING_EMBER_DAMAGED.get();
        return AGSingletons.EMBER_BLOCK.get();
    }

}
