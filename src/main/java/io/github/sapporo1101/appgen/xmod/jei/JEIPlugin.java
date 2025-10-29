package io.github.sapporo1101.appgen.xmod.jei;


import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = AppliedGenerators.id("core");

    public JEIPlugin() {
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        var jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new JEIGenesisSynthesizerCategory(jeiHelpers));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getRecipeManager();
        if (recipeManager != null) {
            registration.addRecipes(
                    JEIGenesisSynthesizerCategory.RECIPE_TYPE,
                    recipeManager.getAllRecipesFor(GenesisSynthesizerRecipe.TYPE).stream().map(RecipeHolder::value).toList()
            );
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AGSingletons.GENESIS_SYNTHESIZER, JEIGenesisSynthesizerCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(AGSingletons.SMELTER, RecipeTypes.SMELTING);
    }

    public static Ingredient stackOf(IngredientStack.Item stack) {
        if (!stack.isEmpty()) {
            return Ingredient.of(Arrays.stream(stack.getIngredient().getItems())
                    .map(oldStack -> oldStack.copyWithCount(stack.getAmount())));
        }
        return IngredientStack.Item.EMPTY.getIngredient();
    }

    public static List<FluidStack> stackOf(IngredientStack.Fluid stack) {
        FluidIngredient ingredient = stack.getIngredient();
        return Arrays.stream(ingredient.getStacks())
                .map(oldStack -> oldStack.copyWithAmount(stack.getAmount()))
                .toList();
    }
}
