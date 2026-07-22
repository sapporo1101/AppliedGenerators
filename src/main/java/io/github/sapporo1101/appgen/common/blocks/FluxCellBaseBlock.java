package io.github.sapporo1101.appgen.common.blocks;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import io.github.sapporo1101.appgen.api.AGComponents;
import io.github.sapporo1101.appgen.common.blockentities.FluxCellBaseBlockEntity;
import io.github.sapporo1101.appgen.menu.FluxCellMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class FluxCellBaseBlock<U extends FluxCellBaseBlockEntity> extends BlockBaseGui<U> {

    public FluxCellBaseBlock() {
        super(glassProps());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (level1, pos1, state1, be) -> ((FluxCellBaseBlockEntity) be).tick(level1, pos1, state1, (FluxCellBaseBlockEntity) be);
    }

    @Override
    public void openGui(FluxCellBaseBlockEntity tile, Player p) {
        MenuOpener.open(FluxCellMenu.TYPE, p, MenuLocators.forBlockEntity(tile));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Entity player = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);

        boolean isWrench = false;
        if (tool != null) {
            isWrench = InteractionUtil.canWrenchDisassemble(tool);
        }

        if (player instanceof Player p && p.isCreative() && !isWrench) {
            return Collections.emptyList();
        }

        // For survival players, or creative with wrench, create the drop with NBT.
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof FluxCellBaseBlockEntity fluxBe) {
            ItemStack drop = new ItemStack(fluxBe.getBlockState().getBlock());
            Level level = fluxBe.getLevel();
            if (level == null) return Collections.emptyList();
            if (!fluxBe.getGenericInv().isEmpty()) {
                CompoundTag blockEntityTag = fluxBe.saveWithId(fluxBe.getLevel().registryAccess());
                blockEntityTag.remove("output_side");
                drop.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(blockEntityTag));

                drop.set(AGComponents.STORED_ENERGY, (double) fluxBe.getStoredFE());
            }
            return Collections.singletonList(drop);
        }

        return super.getDrops(state, builder);
    }
}