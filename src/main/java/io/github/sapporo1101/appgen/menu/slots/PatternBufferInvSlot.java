package io.github.sapporo1101.appgen.menu.slots;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.menu.slot.AppEngSlot;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PatternBufferInvSlot extends AppEngSlot {

    private final PatternBufferBlockEntity.PatternInput patternInput;

    public PatternBufferInvSlot(InternalInventory inv, int invSlot, PatternBufferBlockEntity.PatternInput patternInput) {
        super(inv, invSlot);
        this.patternInput = patternInput;
    }

    @Override
    public @NotNull ItemStack safeInsert(@NotNull ItemStack inputStack, int inputAmount) {
        int inserting = Math.toIntExact(
                PatternBufferBlockEntity.canInsert(
                        this.getSlotIndex(),
                        AEItemKey.of(inputStack),
                        inputAmount,
                        this.patternInput,
                        this.getInventory().getStackInSlot(this.getSlotIndex()).getCount()
                )
        );
        if (inserting <= 0) return inputStack;
        return super.safeInsert(inputStack, inserting);
    }
}
