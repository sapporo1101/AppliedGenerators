package io.github.sapporo1101.appgen.xmod.emi;

import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(EMIGenesisSynthesizerRecipe.CATEGORY);
        registry.addWorkstation(EMIGenesisSynthesizerRecipe.CATEGORY, EmiStack.of(AGSingletons.GENESIS_SYNTHESIZER));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(AGSingletons.SMELTER));
        adaptRecipeType(registry, GenesisSynthesizerRecipe.TYPE, EMIGenesisSynthesizerRecipe::new);
    }

    @SuppressWarnings("SameParameterValue")
    private static <C extends RecipeInput, T extends Recipe<C>> void adaptRecipeType(
            EmiRegistry registry, RecipeType<T> recipeType, Function<RecipeHolder<T>, ? extends EmiRecipe> adapter) {
        registry.getRecipeManager().getAllRecipesFor(recipeType).stream()
                .map(adapter)
                .forEach(registry::addRecipe);
    }

    public static EmiIngredient stackOf(IngredientStack.Item stack) {
        return !stack.isEmpty() ? EmiIngredient.of(stack.getIngredient(), stack.getAmount()) : EmiStack.EMPTY;
    }

    public static EmiIngredient stackOf(IngredientStack.Fluid stack) {
        FluidIngredient ingredient = stack.getIngredient();
        List<EmiIngredient> list = new ArrayList<>();
        FluidStack[] stacks = ingredient.getStacks();

        for (FluidStack fluid : stacks) {
            list.add(EmiStack.of(fluid.getFluid(), stack.getAmount()));
        }

        return EmiIngredient.of(list, stack.getAmount());
    }
}
