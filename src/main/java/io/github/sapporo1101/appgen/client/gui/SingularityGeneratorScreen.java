package io.github.sapporo1101.appgen.client.gui;

import appeng.api.config.YesNo;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import io.github.sapporo1101.appgen.api.AAESettings;
import io.github.sapporo1101.appgen.client.button.AAEServerSettingToggleButton;
import io.github.sapporo1101.appgen.client.button.ActionEPPButton;
import io.github.sapporo1101.appgen.client.button.EAEIcon;
import io.github.sapporo1101.appgen.client.gui.subgui.OutputSideConfig;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.menu.SingularityGeneratorMenu;
import io.github.sapporo1101.appgen.network.AGNetworkHandler;
import io.github.sapporo1101.appgen.network.packet.CAGGenericPacket;
import io.github.sapporo1101.appgen.util.CommaSeparator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SingularityGeneratorScreen extends UpgradeableScreen<SingularityGeneratorMenu> {
    private final ProgressBar pb;
    private final AAEServerSettingToggleButton<YesNo> meExportBtn;
    private final ActionEPPButton outputSideBtn;

    public SingularityGeneratorScreen(SingularityGeneratorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb = new ProgressBar(this.menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", pb);
        this.meExportBtn = new AAEServerSettingToggleButton<>(AAESettings.ME_EXPORT, YesNo.NO);
        this.addToLeftToolbar(meExportBtn);
        this.outputSideBtn = new ActionEPPButton(_ -> this.openOutputConfig(), EAEIcon.OUTPUT_SIDES);
        this.outputSideBtn.setMessage(Component.translatable("gui.appgen.set_output_sides.open"));
        this.addToLeftToolbar(this.outputSideBtn);
    }

    private void openOutputConfig() {
        if (this.getMenu().getHost() != null) {
            switchToScreen(new OutputSideConfig<>(
                    this,
                    new ItemStack(AGSingletons.SINGULARITY_GENERATOR_1K),
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
                "gui.appgen.singularity_generator.progress",
                CommaSeparator.FORMATTER.format(this.menu.generatableFE),
                CommaSeparator.FORMATTER.format(this.menu.getHost().getFEPerSingularity()),
                CommaSeparator.FORMATTER.format(Math.round(this.menu.lastGeneratePerTick))
        ));
        this.meExportBtn.set(this.getMenu().getMeExport());
        this.outputSideBtn.setVisibility(this.meExportBtn.getCurrentValue() == YesNo.NO);
    }
}