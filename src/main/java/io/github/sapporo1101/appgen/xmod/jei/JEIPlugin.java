package io.github.sapporo1101.appgen.xmod.jei;

import appeng.client.AppEngClient;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final Identifier ID = AppliedGenerators.id("jei_plugin");

    @Override
    public @NotNull Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        var helpers = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new JEIGenesisSynthesizerCategory(helpers));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        var recipes = this.getRecipes(GenesisSynthesizerRecipe.TYPE);
        registry.addRecipes(JEIGenesisSynthesizerCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        registry.addCraftingStation(JEIGenesisSynthesizerCategory.RECIPE_TYPE, AGSingletons.GENESIS_SYNTHESIZER);
        registry.addCraftingStation(RecipeTypes.SMELTING, AGSingletons.SMELTER);
    }

    public static List<ItemStack> stackOf(IngredientStack.Item stack) {
        if (!stack.isEmpty()) {
            return stack.getIngredient().getValues().stream()
                    .map(item -> new ItemStack(item, stack.getAmount()))
                    .toList();
        }
        return List.of();
    }

    public static List<FluidStack> stackOf(IngredientStack.Fluid stack) {
        FluidIngredient ingredient = stack.getIngredient();
        return ingredient.fluids().stream()
                .map(fluid -> new FluidStack(fluid, stack.getAmount()))
                .toList();
    }

    /**
     * @noinspection SameParameterValue
     */
    private <I extends RecipeInput, T extends Recipe<@NotNull I>> List<RecipeHolder<@NotNull T>> getRecipes(RecipeType<@NotNull T> type) {
        var recipes = AppEngClient.instance().getRecipeMapForType(Minecraft.getInstance().level, type);
        return List.copyOf(recipes.byType(type));
    }
}
