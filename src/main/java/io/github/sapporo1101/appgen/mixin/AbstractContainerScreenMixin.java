package io.github.sapporo1101.appgen.mixin;

import io.github.sapporo1101.appgen.client.gui.PatternBufferScreen;
import io.github.sapporo1101.appgen.menu.slots.PatternBufferInvSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Shadow
    @Final
    protected Set<Slot> quickCraftSlots;

    @Shadow
    public abstract T getMenu();

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if ((AbstractContainerScreen<?>) (Object) this instanceof PatternBufferScreen patternBufferScreen) {
            // restrict item replacement in storage slots
            if (slot instanceof PatternBufferInvSlot) {
                if (containerInput == ContainerInput.PICKUP) {
                    ItemStack storageItem = slot.getItem();
                    ItemStack carriedItem = this.getMenu().getCarried();
                    if (!storageItem.isEmpty() && !carriedItem.isEmpty()) {
                        if (!ItemStack.isSameItemSameComponents(storageItem, carriedItem)) {
                            ci.cancel();
                            return;
                        }
                    }
                }
            }

            // restrict hold click to insert items into storage slots
            if (containerInput == ContainerInput.QUICK_CRAFT) {
                for (Slot quickCraftSlot : quickCraftSlots) {
                    if (quickCraftSlot instanceof PatternBufferInvSlot) {
                        ci.cancel();
                        return;
                    }
                }
            }

            // restrict pattern slot
            if (slot == patternBufferScreen.getMenu().patternSlot && patternBufferScreen.getMenu().getHost().isPatternSlotLocked()) {
                ci.cancel();
            }
        }
    }
}
