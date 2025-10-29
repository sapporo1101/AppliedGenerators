package io.github.sapporo1101.appgen.xmod.rei;

import appeng.integration.modules.itemlists.CompatLayerHelper;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import dev.architectury.fluid.FluidStack;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;

@REIPluginClient
public class REIPlugin implements REIClientPlugin {

    @Override
    public String getPluginProviderName() {
        return "Applied Generators";
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        registry.registerRecipeFiller(GenesisSynthesizerRecipe.class, GenesisSynthesizerRecipe.TYPE, REIGenesisSynthesizerDisplay::new);
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        registry.add(new REIGenesisSynthesizerCategory());
        registry.addWorkstations(REIGenesisSynthesizerDisplay.ID, EntryStacks.of(AGSingletons.GENESIS_SYNTHESIZER));
        registry.addWorkstations(BuiltinPlugin.SMELTING, EntryStacks.of(AGSingletons.SMELTER));
    }

    public static EntryIngredient stackOf(IngredientStack.Item stack) {
        if (!stack.isEmpty()) {
            var stacks = stack.getIngredient().getItems();
            var result = EntryIngredient.builder(stacks.length);
            for (var ing : stacks) {
                if (!ing.isEmpty()) {
                    result.add(EntryStacks.of(ing.copyWithCount(stack.getAmount())));
                }
            }
            return result.build();
        }
        return EntryIngredient.empty();
    }

    public static EntryIngredient stackOf(IngredientStack.Fluid stack, float tankSize) {
        if (!stack.isEmpty()) {
            var stacks = stack.getIngredient().getStacks();
            var result = EntryIngredient.builder(stacks.length);
            for (var ing : stacks) {
                if (!ing.isEmpty()) {
                    EntryStack<FluidStack> f = EntryStacks.of(ing.getFluid(), stack.getAmount());
                    ClientEntryStacks.setFluidRenderRatio(f, (float) stack.getAmount() / tankSize);
                    result.add(f);
                }
            }
            return result.build();
        }
        return EntryIngredient.empty();
    }
}
