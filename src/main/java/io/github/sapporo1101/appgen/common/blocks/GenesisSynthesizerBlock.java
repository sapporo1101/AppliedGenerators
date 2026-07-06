package io.github.sapporo1101.appgen.common.blocks;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.sapporo1101.appgen.common.blockentities.GenesisSynthesizerBlockEntity;
import io.github.sapporo1101.appgen.menu.GenesisSynthesizerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class GenesisSynthesizerBlock extends BlockBaseGui<GenesisSynthesizerBlockEntity> {

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public GenesisSynthesizerBlock(Properties props) {
        super(metalProps(props));
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
    public void openGui(GenesisSynthesizerBlockEntity tile, Player p) {
        MenuOpener.open(GenesisSynthesizerMenu.TYPE, p, MenuLocators.forBlockEntity(tile));
    }
}
