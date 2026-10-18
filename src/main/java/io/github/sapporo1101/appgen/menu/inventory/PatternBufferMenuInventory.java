package io.github.sapporo1101.appgen.menu.inventory;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.ConfigMenuInventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PatternBufferMenuInventory extends ConfigMenuInventory {

    public PatternBufferMenuInventory(GenericStackInv inv) {
        super(inv);
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slotIndex) {
        GenericStack stack = this.getDelegate().getStack(slotIndex);
        if (stack != null && stack.what() instanceof AEItemKey itemKey && stack.amount() > 0) {
            return itemKey.toStack((int) Math.min(Integer.MAX_VALUE, stack.amount()));
        }
        return super.getStackInSlot(slotIndex);
    }
}
