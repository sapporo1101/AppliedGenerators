package io.github.sapporo1101.appgen.client.button;

import appeng.client.gui.style.Blitter;
import io.github.sapporo1101.appgen.AppliedGenerators;
import net.minecraft.resources.Identifier;

public enum AGIcon {
    ME_EXPORT_ON(0, 0),
    ME_EXPORT_OFF(16, 0),
    OUTPUT_SIDES(32, 0),
    KEEP_SIGNAL_ON(48, 0),
    KEEP_SIGNAL_OFF(64, 0),
    KEEP_SIGNAL_LOCKED(80, 0),

    TOOLBAR_BUTTON_BACKGROUND(176, 128, 18, 20);

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public static final Identifier TEXTURE = AppliedGenerators.id("textures/guis/states.png");
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    AGIcon(int x, int y) {
        this(x, y, 16, 16);
    }

    AGIcon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT).src(x, y, width, height);
    }
}
