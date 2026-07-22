package io.github.sapporo1101.appgen.client.render;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PatternBufferRenderer implements BlockEntityRenderer<PatternBufferBlockEntity> {

    public PatternBufferRenderer() {
        // NO-OP
    }

    @Override
    public void render(@NotNull PatternBufferBlockEntity patternBuffer, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn) {

        AEKey key = patternBuffer.getPrimaryOutputKey();
        if (key instanceof AEItemKey itemKey) {
            ItemStack stack = new ItemStack(itemKey.getItem());

            Minecraft minecraft = Minecraft.getInstance();
            ItemRenderer itemRenderer = minecraft.getItemRenderer();
            poseStack.pushPose();

            if (!(stack.getItem() instanceof BlockItem)) {
                poseStack.translate(0.5, 0.3, 0.5);
            } else {
                poseStack.translate(0.5, 0.22, 0.5);
            }

            poseStack.scale(1.5F, 1.5F, 1.5F);

            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn,
                    OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, patternBuffer.getLevel(), 0);
            poseStack.popPose();
        }
    }
}
