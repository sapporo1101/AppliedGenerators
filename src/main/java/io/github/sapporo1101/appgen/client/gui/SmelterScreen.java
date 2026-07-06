package io.github.sapporo1101.appgen.client.gui;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.Tooltips;
import io.github.sapporo1101.appgen.client.button.ActionEPPButton;
import io.github.sapporo1101.appgen.client.button.EAEIcon;
import io.github.sapporo1101.appgen.client.gui.subgui.OutputSideConfig;
import io.github.sapporo1101.appgen.client.gui.widget.AlertWidget;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.menu.SmelterMenu;
import io.github.sapporo1101.appgen.network.AGNetworkHandler;
import io.github.sapporo1101.appgen.network.packet.CAGGenericPacket;
import io.github.sapporo1101.appgen.util.CommaSeparator;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SmelterScreen extends UpgradeableScreen<SmelterMenu> {

    private final ProgressBar pb;
    private final SettingToggleButton<YesNo> autoExportBtn;
    private final ActionEPPButton outputSideBtn;
    private final AlertWidget powerAlert;

    public SmelterScreen(SmelterMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb = new ProgressBar(this.menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", pb);
        this.autoExportBtn = new ServerSettingToggleButton<>(Settings.AUTO_EXPORT, YesNo.NO);
        this.addToLeftToolbar(autoExportBtn);
        this.outputSideBtn = new ActionEPPButton(_ -> this.openOutputConfig(), EAEIcon.OUTPUT_SIDES);
        this.outputSideBtn.setMessage(Component.translatable("gui.appgen.set_output_sides.open"));
        this.addToLeftToolbar(this.outputSideBtn);
        this.powerAlert = new AlertWidget(style.getImage("powerAlert"));
        this.powerAlert.setTooltip(Tooltip.create(Tooltips.of(Component.translatable("gui.appgen.insufficient_power").withStyle(Tooltips.RED), Component.literal("\n").append(Component.translatable("gui.appgen.insufficient_power.details")).withStyle(Tooltips.NORMAL_TOOLTIP_TEXT))));
        this.widgets.add("powerAlert", this.powerAlert);
    }

    private void openOutputConfig() {
        if (this.getMenu().getHost() != null) {
            switchToScreen(new OutputSideConfig<>(
                    this,
                    new ItemStack(AGSingletons.SMELTER),
                    this.getMenu().getHost(),
                    this.getMenu().getOutputSides(),
                    (side, value) -> AGNetworkHandler.INSTANCE.sendToServer(new CAGGenericPacket("set_side", side, value)))
            );
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.pb.setFullMsg(Component.translatable(
                "gui.appgen.smelter.progress",
                CommaSeparator.FORMATTER.format(this.menu.progress),
                CommaSeparator.FORMATTER.format(this.menu.maxProgress)
        ));
        this.autoExportBtn.set(getMenu().getAutoExport());
        this.outputSideBtn.setVisibility(this.autoExportBtn.getCurrentValue() == YesNo.YES);
        this.powerAlert.visible = this.getMenu().showWarning;
    }
}
