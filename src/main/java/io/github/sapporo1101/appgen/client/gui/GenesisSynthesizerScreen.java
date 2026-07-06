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
import io.github.sapporo1101.appgen.client.gui.widget.SubProgressBar;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.menu.GenesisSynthesizerMenu;
import io.github.sapporo1101.appgen.network.AGNetworkHandler;
import io.github.sapporo1101.appgen.network.packet.CAGGenericPacket;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GenesisSynthesizerScreen extends UpgradeableScreen<GenesisSynthesizerMenu> {

    private final ProgressBar pb;
    private final SettingToggleButton<YesNo> autoExportBtn;
    private final ActionEPPButton outputSideBtn;
    private final SubProgressBar crystalBar;
    private final AlertWidget powerAlert;

    public GenesisSynthesizerScreen(GenesisSynthesizerMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb = new ProgressBar(this.menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", this.pb);
        this.crystalBar = new SubProgressBar(this.menu, style.getImage("crystalBar"), SubProgressBar.Direction.VERTICAL);
        widgets.add("crystalBar", this.crystalBar);
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
                    new ItemStack(AGSingletons.GENESIS_SYNTHESIZER),
                    this.getMenu().getHost(),
                    this.getMenu().getOutputSides(),
                    (side, value) -> AGNetworkHandler.INSTANCE.sendToServer(new CAGGenericPacket("set_side", side, value)))
            );
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        int progress = this.menu.getCurrentProgress() * 100 / this.menu.getMaxProgress();
        this.pb.setFullMsg(Component.literal(progress + "%"));
        this.crystalBar.setFullMsg(Component.translatable("gui.appgen.genesis_synthesizer.crystal_info", this.menu.getCurrentSubProgress(), this.menu.getMaxSubProgress()));
        this.autoExportBtn.set(getMenu().getAutoExport());
        this.outputSideBtn.setVisibility(this.autoExportBtn.getCurrentValue() == YesNo.YES);
        this.powerAlert.visible = this.getMenu().showWarning;
    }
}
