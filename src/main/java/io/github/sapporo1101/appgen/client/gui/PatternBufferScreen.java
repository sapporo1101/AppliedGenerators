package io.github.sapporo1101.appgen.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.definitions.AEItems;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.api.KeepSignalMode;
import io.github.sapporo1101.appgen.client.button.AGServerSettingToggleButton;
import io.github.sapporo1101.appgen.menu.PatternBufferMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PatternBufferScreen extends UpgradeableScreen<PatternBufferMenu> {
    private final AGServerSettingToggleButton<KeepSignalMode> keepSignalButton;

    public PatternBufferScreen(PatternBufferMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.keepSignalButton = new AGServerSettingToggleButton<>(AGSettings.KEEP_SIGNAL, KeepSignalMode.OFF);
        this.addToLeftToolbar(keepSignalButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.keepSignalButton.set(this.getMenu().getKeepSignal());
        this.keepSignalButton.setVisibility(this.getMenu().hasUpgrade(AEItems.REDSTONE_CARD));
    }
}
