package io.github.sapporo1101.appgen.client.gui.widget;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.localization.GuiText;
import io.github.sapporo1101.appgen.menu.interfaces.ISubProgressProvider;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubProgressBar extends AbstractWidget implements ITooltip {

    private final ISubProgressProvider source;
    private final Blitter blitter;
    private final Direction layout;
    private final Rect2i sourceRect;
    private final Component titleName;
    private Component fullMsg;

    public SubProgressBar(ISubProgressProvider source, Blitter blitter, Direction dir) {
        this(source, blitter, dir, null);
    }

    public SubProgressBar(ISubProgressProvider source, Blitter blitter,
                          Direction dir, Component title) {
        super(0, 0, blitter.getSrcWidth(), blitter.getSrcHeight(), Component.empty());
        this.source = source;
        this.blitter = blitter.copy();
        this.layout = dir;
        this.titleName = title;
        this.sourceRect = new Rect2i(
                blitter.getSrcX(),
                blitter.getSrcY(),
                blitter.getSrcWidth(),
                blitter.getSrcHeight());
    }

    @Override
    public void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int max = this.source.getMaxSubProgress();
            int current = this.source.getCurrentSubProgress();

            if (current > max) {
                current = max;
            }

            int srcX = sourceRect.getX();
            int srcY = sourceRect.getY();
            int srcW = sourceRect.getWidth();
            int srcH = sourceRect.getHeight();
            int destX = getX();
            int destY = getY();

            if (this.layout == Direction.VERTICAL) {
                int diff = this.height - (max > 0 ? this.height * current / max : 0);
                destY += diff;
                srcY += diff;
                srcH -= diff;
            } else {
                int diff = this.width - (max > 0 ? this.width * current / max : 0);
                srcX += diff;
                srcW -= diff;
            }

            blitter.src(srcX, srcY, srcW, srcH).dest(destX, destY).blit(guiGraphics);
        }
    }

    public void setFullMsg(Component msg) {
        this.fullMsg = msg;
    }

    @Override
    public List<Component> getTooltipMessage() {
        if (this.fullMsg != null) {
            return Collections.singletonList(this.fullMsg);
        }

        Component result = this.titleName != null ? this.titleName : Component.empty();
        return Arrays.asList(
                result,
                Component.literal(this.source.getCurrentSubProgress() + " ")
                        .append(GuiText.Of.text().copy().append(" " + this.source.getMaxSubProgress())));
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX() - 2, getY() - 2, width + 4, height + 4);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
    }

    public enum Direction {
        HORIZONTAL, VERTICAL
    }
}
