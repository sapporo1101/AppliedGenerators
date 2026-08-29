package io.github.sapporo1101.appgen.mixin;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.helpers.ResourceConversion;
import appeng.helpers.externalstorage.GenericStackInvHandler;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GenericStackInvHandler.class)
public abstract class GenericStackInvHandlerMixin<V extends Resource> implements ResourceHandler<V> {
    @Shadow
    @Final
    @SuppressWarnings("UnstableApiUsage")
    private GenericInternalInventory inv;

    @Shadow
    @Final
    private ResourceConversion<V> conversion;

    @ModifyVariable(method = "insert(ILnet/neoforged/neoforge/transfer/resource/Resource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I", at = @At("STORE"), name = "inserted")
    private int onInsert(int inserted, @Local(name = "index", argsOnly = true) int index, @Local(name = "resource", argsOnly = true) V resource, @Local(name = "amount", argsOnly = true) int amount) {
        int inserting = 0;
        if (this.inv instanceof PatternBufferBlockEntity.PatternBufferInv storageInv) {
            PatternBufferBlockEntity.PatternInput patternInput = storageInv.getPatternInput();
            inserting = Math.toIntExact(PatternBufferBlockEntity.canInsert(index, conversion.getKey(resource), amount, patternInput, (int) storageInv.getAmount(index)));

            PatternBufferBlockEntity be = storageInv.getHost();
            if (be.isInsertionLocked()) inserting = 0;
        }
        return Math.min(inserted, inserting);
    }

    @Inject(method = "extract(ILnet/neoforged/neoforge/transfer/resource/Resource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I", at = @At("HEAD"), cancellable = true)
    private void onExtract(int index, V resource, int maxAmount, TransactionContext transaction, CallbackInfoReturnable<Integer> cir) {
        if (this.inv instanceof PatternBufferBlockEntity.PatternBufferInv storageInv) {
            PatternBufferBlockEntity be = storageInv.getHost();
            if (be.isExtractionLocked()) cir.setReturnValue(0);
        }
    }
}
