package io.github.sapporo1101.appgen.common.blockentities.interfaces;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.helpers.externalstorage.GenericStackInv;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Set;

public interface IItemExtractor {

    boolean sendStack();

    default boolean sendItem(InternalInventory outputInv, Set<Direction> outputSides, BlockPos pos, Level level) {
        if (level == null) return false;
        for (Direction dir : outputSides) {
            BlockPos targetPos = pos.relative(dir);
            ResourceHandler<ItemResource> itemStorage = level.getCapability(Capabilities.Item.BLOCK, targetPos, dir.getOpposite());

            sendItem:
            if (itemStorage != null) {
                int canInsert;
                try (Transaction transaction = Transaction.openRoot()) {
                    ItemStack sendingStack = outputInv.extractItem(0, 64, true);
                    canInsert = itemStorage.insert(ItemResource.of(sendingStack), sendingStack.getCount(), transaction);
                }

                if (canInsert <= 0) break sendItem;

                try (Transaction transaction = Transaction.openRoot()) {
                    ItemStack extractedStack = outputInv.extractItem(0, canInsert, false);
                    int inserted = itemStorage.insert(ItemResource.of(extractedStack), extractedStack.getCount(), transaction);
                    if (inserted > 0) {
                        transaction.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    default boolean sendFluid(GenericStackInv fluidInv, Set<Direction> outputSides, BlockPos pos, Level level) {
        if (level == null) return false;
        for (Direction dir : outputSides) {
            BlockPos targetPos = pos.relative(dir);
            ResourceHandler<FluidResource> fluidStorage = level.getCapability(Capabilities.Fluid.BLOCK, targetPos, dir.getOpposite());

            sendFluid:
            if (fluidStorage != null) {
                var outFluid = fluidInv.getStack(1);
                var fluidKey = outFluid != null ? outFluid.what() : null;
                if (outFluid == null || fluidKey == null) break sendFluid;
                int canInsert;
                try (Transaction transaction = Transaction.openRoot()) {
                    FluidStack sendingStack = ((AEFluidKey) fluidKey).toStack((int) outFluid.amount());
                    canInsert = fluidStorage.insert(FluidResource.of(sendingStack), sendingStack.getAmount(), transaction);
                }

                if (canInsert <= 0) break sendFluid;

                try (Transaction transaction = Transaction.openRoot()) {
                    var extractedAmount = fluidInv.extract(1, fluidKey, canInsert, Actionable.MODULATE);
                    FluidStack extractedStack = ((AEFluidKey) fluidKey).toStack((int) extractedAmount);
                    int inserted = fluidStorage.insert(FluidResource.of(extractedStack), extractedStack.getAmount(), transaction);
                    if (inserted > 0) {
                        transaction.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
