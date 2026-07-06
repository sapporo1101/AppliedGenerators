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
import me.shedaniel.rei.api.common.registry.display.DisplayConsumer;
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

        DisplayConsumer.RecipeManagerConsumer.RecipeFillerBuilder<GenesisSynthesizerRecipe, REIGenesisSynthesizerDisplay> builder =
                ((DisplayConsumer.RecipeManagerConsumer) registry).beginRecipeFiller(GenesisSynthesizerRecipe.class);

        builder.filterType(GenesisSynthesizerRecipe.TYPE)
                .fill(REIGenesisSynthesizerDisplay::new);
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
            var stacks = stack.getIngredient().getValues();
            var result = EntryIngredient.builder(stacks.size());
            for (var ing : stacks) {
                result.add(EntryStacks.of(ing.value(), stack.getAmount()));
            }
            return result.build();
        }
        return EntryIngredient.empty();
    }

    public static EntryIngredient stackOf(IngredientStack.Fluid stack, float tankSize) {
        if (!stack.isEmpty()) {
            var stacks = stack.getIngredient().fluids();
            var result = EntryIngredient.builder(stacks.size());
            for (var ing : stacks) {
                EntryStack<FluidStack> f = EntryStacks.of(ing.value(), stack.getAmount());
                ClientEntryStacks.setFluidRenderRatio(f, (float) stack.getAmount() / tankSize);
                result.add(f);
            }
            return result.build();
        }
        return EntryIngredient.empty();
    }
}
