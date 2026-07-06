package io.github.sapporo1101.appgen.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class PatternBufferRenderState extends BlockEntityRenderState {
    public ItemStackRenderState item = new ItemStackRenderState();
    boolean blockItem;
}
