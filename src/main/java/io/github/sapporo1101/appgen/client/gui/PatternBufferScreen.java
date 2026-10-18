package io.github.sapporo1101.appgen.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.definitions.AEItems;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.api.KeepSignalMode;
import io.github.sapporo1101.appgen.client.button.AGServerSettingToggleButton;
import io.github.sapporo1101.appgen.client.gui.subgui.PatternBufferClearConfirmScreen;
import io.github.sapporo1101.appgen.menu.PatternBufferMenu;
import io.github.sapporo1101.appgen.menu.slots.PatternBufferInvSlot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PatternBufferScreen extends UpgradeableScreen<PatternBufferMenu> {
    private static final int MIDDLE_BUTTON = 2;

    private final AGServerSettingToggleButton<KeepSignalMode> keepSignalButton;

    public PatternBufferScreen(PatternBufferMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.keepSignalButton = new AGServerSettingToggleButton<>(AGSettings.KEEP_SIGNAL, KeepSignalMode.OFF);
        this.addToLeftToolbar(keepSignalButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == MIDDLE_BUTTON) {
            var slot = this.getSlotUnderMouse();
            if (slot instanceof PatternBufferInvSlot && slot.hasItem()) {
                var clearAll = Screen.hasShiftDown();
                this.switchToScreen(new PatternBufferClearConfirmScreen(
                        this,
                        clearAll ? PatternBufferMenu.CLEAR_ALL : slot.getSlotIndex(),
                        clearAll));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.keepSignalButton.set(this.getMenu().getKeepSignal());
        this.keepSignalButton.setVisibility(this.getMenu().hasUpgrade(AEItems.REDSTONE_CARD));

    }
}
