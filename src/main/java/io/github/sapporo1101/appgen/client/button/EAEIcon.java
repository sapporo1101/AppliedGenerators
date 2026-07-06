package io.github.sapporo1101.appgen.client.button;

import appeng.client.gui.style.Blitter;
import net.minecraft.resources.Identifier;

public class EAEIcon {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("extendedae", "textures/guis/nicons.png");
    public static final Blitter OUTPUT_SIDES;

    static {
        OUTPUT_SIDES = Blitter.texture(TEXTURE, 64, 64).src(0, 16, 16, 16);
    }
}
