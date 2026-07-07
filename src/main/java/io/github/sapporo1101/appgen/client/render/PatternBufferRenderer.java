package io.github.sapporo1101.appgen.client.render;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatternBufferRenderer implements BlockEntityRenderer<@NotNull PatternBufferBlockEntity, @NotNull PatternBufferRenderState> {
    private final ItemModelResolver itemModelResolver;

    public PatternBufferRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NotNull PatternBufferRenderState createRenderState() {
        return new PatternBufferRenderState();
    }

    @Override
    public void extractRenderState(@NotNull PatternBufferBlockEntity be, @NotNull PatternBufferRenderState state, float partialTicks, @NotNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);

        state.item.clear();

        AEKey key = be.getPrimaryOutputKey();
        if (key instanceof AEItemKey itemKey) {
            ItemStack stack = new ItemStack(itemKey.getItem());

            this.itemModelResolver.updateForTopItem(
                    state.item,
                    stack,
                    ItemDisplayContext.FIXED,
                    be.getLevel(),
                    null,
                    // This is the random seed
                    (int) be.getBlockPos().asLong());
            state.blockItem = (stack.getItem() instanceof BlockItem);
        }
    }

    @Override
    public void submit(PatternBufferRenderState state, PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
