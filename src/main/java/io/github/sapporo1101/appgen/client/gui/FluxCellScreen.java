package io.github.sapporo1101.appgen.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.sapporo1101.appgen.client.button.ActionEPPButton;
import io.github.sapporo1101.appgen.client.button.EAEIcon;
import io.github.sapporo1101.appgen.client.gui.subgui.OutputSideConfig;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.menu.FluxCellMenu;
import io.github.sapporo1101.appgen.network.AGNetworkHandler;
import io.github.sapporo1101.appgen.network.packet.CAGGenericPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FluxCellScreen extends AEBaseScreen<FluxCellMenu> {

    public FluxCellScreen(FluxCellMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        ActionEPPButton outputSideBtn = new ActionEPPButton(_ -> this.openOutputConfig(), EAEIcon.OUTPUT_SIDES);
        outputSideBtn.setMessage(Component.translatable("gui.appgen.set_output_sides.open"));
        this.addToLeftToolbar(outputSideBtn);
    }

    private void openOutputConfig() {
        if (this.getMenu().getHost() != null) {
            switchToScreen(new OutputSideConfig<>(
                    this,
                    new ItemStack(AGSingletons.FLUX_CELL),
                    this.getMenu().getHost(),
                    this.getMenu().getOutputSides(),
                    (side, value) -> AGNetworkHandler.INSTANCE.sendToServer(new CAGGenericPacket("set_side", side, value)))
            );
        }
    }
}
