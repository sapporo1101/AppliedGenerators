package io.github.sapporo1101.appgen.common.blockentities.interfaces;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Set;

public interface IItemExtractor {

    boolean sendStack();

    default boolean sendItem(InternalInventory outputInv, Set<Direction> outputSides, BlockPos pos, Level level) {
        if (level == null) return false;

        for (Direction dir : outputSides) {
            BlockPos targetPos = pos.relative(dir);
            IItemHandler itemStorage = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, dir.getOpposite());

            if (itemStorage != null) {
                if (outputInv.getStackInSlot(0) != null && !outputInv.getStackInSlot(0).isEmpty()) {
                    ItemStack sendingStack = outputInv.extractItem(0, 64, false);
                    int sending = sendingStack.getCount();
                    for (int i = 0; i < itemStorage.getSlots(); i++) {
                        if (sendingStack.getCount() <= 0) break;
                        sendingStack = itemStorage.insertItem(i, sendingStack, false);
                    }
                    outputInv.insertItem(0, sendingStack, false);
                    return sendingStack.getCount() < sending;
                }
            }
        }
        return false;
    }

    default boolean sendFluid(GenericStackInv fluidInv, int slot, Set<Direction> outputSides, BlockPos pos, Level level) {
        if (level == null) return false;

        for (Direction dir : outputSides) {
            BlockPos targetPos = pos.relative(dir);
            IFluidHandler fluidStorage = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, dir.getOpposite());

            if (fluidStorage != null) {
                GenericStack outFluid = fluidInv.getStack(slot);
                AEKey fluidKey = outFluid != null ? outFluid.what() : null;
                if (outFluid != null && fluidKey != null) {
                    long extracted = fluidInv.extract(slot, fluidKey, outFluid.amount(), Actionable.MODULATE);
                    int inserted = fluidStorage.fill(((AEFluidKey) fluidKey).toStack((int) extracted), IFluidHandler.FluidAction.EXECUTE);
                    fluidInv.insert(slot, fluidKey, (extracted - inserted), Actionable.MODULATE);

                    if (fluidInv.getAmount(slot) == 0) {
                        fluidInv.setStack(slot, null);
                    }
                    return inserted > 0;
                }
            }
        }
        return false;
    }
}
