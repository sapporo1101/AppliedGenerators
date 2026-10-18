package io.github.sapporo1101.appgen.menu.slots;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.slot.AppEngSlot;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatternBufferInvSlot extends AppEngSlot {

    private final PatternBufferBlockEntity.PatternInput patternInput;
    private final int storageIndex;

    public PatternBufferInvSlot(InternalInventory inv, int storageIndex, PatternBufferBlockEntity.PatternInput patternInput) {
        super(inv, 0);
        this.storageIndex = storageIndex;
        this.patternInput = patternInput;
    }

    public int getStorageIndex() {
        return this.storageIndex;
    }

    @Override
    public @NotNull ItemStack safeInsert(@NotNull ItemStack inputStack, int inputAmount) {
        int inserting = Math.toIntExact(
                PatternBufferBlockEntity.canInsert(
                        this.storageIndex,
                        keyOf(inputStack),
                        inputAmount,
                        this.patternInput,
                        this.getInventory().getStackInSlot(0).getCount()
                )
        );
        if (inserting <= 0) return inputStack;
        return super.safeInsert(inputStack, inserting);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        AEKey key = keyOf(stack);
        if (key != null) {
            long capacity = PatternBufferBlockEntity.canInsert(this.storageIndex, key, Integer.MAX_VALUE,
                    this.patternInput, 0);
            if (capacity > 0) {
                return (int) Math.min(Integer.MAX_VALUE, capacity);
            }
        }
        return super.getMaxStackSize(stack);
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        ItemStack current = getItem();
        if (!current.isEmpty() && !GenericStack.isWrapped(current)) {
            amount = Math.min(amount, current.getMaxStackSize());
        }
        return super.remove(amount);
    }

    @Nullable
    private static AEKey keyOf(ItemStack stack) {
        GenericStack unwrapped = GenericStack.unwrapItemStack(stack);
        if (unwrapped != null) {
            return unwrapped.what();
        }
        return AEItemKey.of(stack);
    }
}
