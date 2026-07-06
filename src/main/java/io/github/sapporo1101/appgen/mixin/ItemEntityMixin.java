package io.github.sapporo1101.appgen.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sapporo1101.appgen.util.LavaTransformLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemEntity.class, priority = 500)
public abstract class ItemEntityMixin extends Entity {
    @SuppressWarnings("unused")
    public ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private boolean appgen$lavaImmune;

    @Unique
    private int appgen$lavaTicks;

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean lavaImmune(boolean original) {
        return original || appgen$lavaImmune;
    }

    @SuppressWarnings({"resource", "UnreachableCode"})
    @Inject(method = "tick", at = @At("RETURN"))
    private void lavaTimeout(CallbackInfo ci) {
        //noinspection DataFlowIssue
        var self = (ItemEntity) (Object) this;

        if (LavaTransformLogic.canTransformInLava(self)) {
            var x = Mth.floor(getX());
            var y = Mth.floor((getBoundingBox().minY + getBoundingBox().maxY) / 2);
            var z = Mth.floor(getZ());
            var state = level().getFluidState(new BlockPos(x, y, z));

            if (state.is(FluidTags.LAVA)) {
                appgen$lavaImmune = appgen$lavaTicks <= 200;

                if (appgen$lavaTicks++ > 200 && LavaTransformLogic.allIngredientsPresent(self)) {
                    appgen$lavaTicks = 0;
                }
            }
        }
    }
}