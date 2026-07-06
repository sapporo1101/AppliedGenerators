package io.github.sapporo1101.appgen.common.blocks;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import io.github.sapporo1101.appgen.menu.PatternBufferMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class PatternBufferBlock extends BlockBaseGui<PatternBufferBlockEntity> {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public PatternBufferBlock(Properties props) {
        super(metalProps(props).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, @NotNull BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public void openGui(PatternBufferBlockEntity tile, Player p) {
        MenuOpener.open(PatternBufferMenu.TYPE, p, MenuLocators.forBlockEntity(tile));
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource r) {
        if (!state.getValue(POWERED)) return;
        for (int i = 0; i < 8; i++) {
            int offsetX = i >> 2 & 1;
            int offsetY = i >> 1 & 1;
            int offsetZ = i & 1;
            level.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 0.5F), pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 0.0, 0.0, 0.0);
        }
    }
}
