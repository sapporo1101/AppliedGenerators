package io.github.sapporo1101.appgen.client.button;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;
import appeng.util.Icon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class EPPButton extends IconButton {
    public EPPButton(Button.OnPress onPress) {
        super(onPress);
    }

    abstract Blitter getBlitterIcon();

    protected final Icon getIcon() {
        return null;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            Item item = this.getItemOverlay();
            Blitter blitter = this.getBlitterIcon();
            if (this.isHalfSize()) {
                this.width = 8;
                this.height = 8;
            }

            int yOffset = this.isHovered() ? 1 : 0;
            if (this.isHalfSize()) {
                if (!this.isDisableBackground()) {
                    Blitter.icon(Icon.TOOLBAR_BUTTON_BACKGROUND).dest(this.getX(), this.getY()).blit(guiGraphics);
                }

                if (item != null) {
                    guiGraphics.item(new ItemStack(item), this.getX(), this.getY(), 0);
                } else if (blitter != null) {
                    if (!this.active) {
                        blitter.opacity(0.5F);
                    }

                    blitter.dest(this.getX(), this.getY()).blit(guiGraphics);
                }
            } else {
                if (!this.isDisableBackground()) {
                    Icon bgIcon = this.isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : (this.isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND);
                    Blitter.icon(bgIcon).dest(this.getX() - 1, this.getY() + yOffset, 18, 20).blit(guiGraphics);
                }

                if (item != null) {
                    guiGraphics.item(new ItemStack(item), this.getX(), this.getY() + 1 + yOffset, 0);
                } else if (blitter != null) {
                    blitter.dest(this.getX(), this.getY() + 1 + yOffset).blit(guiGraphics);
                }
            }
        }

    }
}