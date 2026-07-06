package io.github.sapporo1101.appgen.client.gui.widget;

import appeng.client.gui.style.Blitter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class AlertWidget extends AbstractWidget {
    private final Blitter powerAlert;

    public AlertWidget(Blitter powerAlert) {
        super(0, 0, 18, 18, Component.empty());
        this.powerAlert = powerAlert;
    }

    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        this.powerAlert.dest(this.getX(), this.getY()).blit(guiGraphics);
    }

    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }
}
