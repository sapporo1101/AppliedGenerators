package io.github.sapporo1101.appgen.common.blocks;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.core.AEConfig;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.sapporo1101.appgen.common.blockentities.SmelterBlockEntity;
import io.github.sapporo1101.appgen.menu.SmelterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class SmelterBlock extends BlockBaseGui<SmelterBlockEntity> {

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public SmelterBlock(Properties props) {
        super(metalProps(props).lightLevel(state -> state.getValue(WORKING) ? 13 : 0));
        this.registerDefaultState(this.defaultBlockState().setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, @NotNull BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    public void openGui(SmelterBlockEntity tile, Player p) {
        MenuOpener.open(SmelterMenu.TYPE, p, MenuLocators.forBlockEntity(tile));
    }

    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!AEConfig.instance().isEnableEffects()) return;

        var be = this.getBlockEntity(level, pos);
        if (be == null) return;

        if (state.getValue(WORKING)) {
            double d0 = pos.getX() + 0.5F;
            double d1 = pos.getY();
            double d2 = pos.getZ() + 0.5F;
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(d0, d1, d2, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = be.getFront();
            Direction.Axis direction$axis = direction.getAxis();
            double d3 = random.nextDouble() * 0.6 - 0.3;
            double d4 = direction$axis == Direction.Axis.X ? direction.getStepX() * 0.52 : d3;
            double d5 = random.nextDouble() * 9.0F / 16.0F;
            double d6 = direction$axis == Direction.Axis.Z ? direction.getStepZ() * 0.52 : d3;
            level.addParticle(ParticleTypes.SMOKE, d0 + d4, d1 + d5, d2 + d6, 0.0F, 0.0F, 0.0F);
        }

    }
}
