package io.github.sapporo1101.appgen.recipe;

import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class GenesisSynthesizerRecipeBuilder {
    private final List<IngredientStack.Item> itemInputs = new ArrayList<>();
    private IngredientStack.Fluid fluidInput = null;
    private final long energy;
    private final ItemStackTemplate itemOutput;
    private final FluidStackTemplate fluidOutput;
    private final HolderGetter<Item> itemGetter;
    private final HolderGetter<Fluid> fluidGetter;

    public GenesisSynthesizerRecipeBuilder(ItemStackTemplate itemOutput, FluidStackTemplate fluidOutput, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        this.itemOutput = itemOutput;
        this.fluidOutput = fluidOutput;
        this.energy = energy;
        this.itemGetter = itemGetter;
        this.fluidGetter = fluidGetter;
    }

    public static GenesisSynthesizerRecipeBuilder synthesize(ItemStackTemplate itemStackOutput, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        return new GenesisSynthesizerRecipeBuilder(Objects.requireNonNull(itemStackOutput), null, energy, itemGetter, fluidGetter);
    }

    public static GenesisSynthesizerRecipeBuilder synthesize(ItemLike itemLikeOutput, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        return synthesize(new ItemStackTemplate(itemLikeOutput.asItem()), energy, itemGetter, fluidGetter);
    }

    public static GenesisSynthesizerRecipeBuilder synthesize(ItemLike itemLikeOutput, int count, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        return synthesize(new ItemStackTemplate(itemLikeOutput.asItem(), count), energy, itemGetter, fluidGetter);
    }

    public static GenesisSynthesizerRecipeBuilder synthesize(FluidStackTemplate fluidStackOutput, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        return new GenesisSynthesizerRecipeBuilder(null, Objects.requireNonNull(fluidStackOutput), energy, itemGetter, fluidGetter);
    }

    public static GenesisSynthesizerRecipeBuilder synthesize(Fluid fluidOutput, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        return synthesize(new FluidStackTemplate(fluidOutput, 1000), energy, itemGetter, fluidGetter);
    }

    public static GenesisSynthesizerRecipeBuilder synthesize(Fluid fluidOutput, int count, long energy, HolderGetter<Item> itemGetter, HolderGetter<Fluid> fluidGetter) {
        return synthesize(new FluidStackTemplate(fluidOutput, count), energy, itemGetter, fluidGetter);
    }

    public GenesisSynthesizerRecipeBuilder fluid(FluidStack fluid) {
        this.fluidInput = IngredientStack.of(fluid);
        return this;
    }

    public GenesisSynthesizerRecipeBuilder fluid(Fluid fluid, int amount) {
        this.fluidInput = IngredientStack.of(new FluidStackTemplate(fluid, amount));
        return this;
    }

    public GenesisSynthesizerRecipeBuilder fluid(TagKey<Fluid> tag, int amount) {
        this.fluidInput = IngredientStack.of(FluidIngredient.of(this.fluidGetter.getOrThrow(tag)), amount);
        return this;
    }

    public GenesisSynthesizerRecipeBuilder input(ItemStack item) {
        this.itemInputs.add(IngredientStack.of(item));
        return this;
    }

    public GenesisSynthesizerRecipeBuilder input(ItemLike item) {
        this.itemInputs.add(IngredientStack.of(new ItemStackTemplate(item.asItem())));
        return this;
    }

    public GenesisSynthesizerRecipeBuilder input(ItemLike item, int count) {
        this.itemInputs.add(IngredientStack.of(new ItemStackTemplate(item.asItem(), count)));
        return this;
    }

    public GenesisSynthesizerRecipeBuilder input(TagKey<Item> tag) {
        this.itemInputs.add(IngredientStack.of(Ingredient.of(this.itemGetter.getOrThrow(tag)), 1));
        return this;
    }

    public GenesisSynthesizerRecipeBuilder input(TagKey<Item> tag, int count) {
        this.itemInputs.add(IngredientStack.of(Ingredient.of(this.itemGetter.getOrThrow(tag)), count));
        return this;
    }

    public void save(RecipeOutput consumer, Identifier id) {
        var recipe = new GenesisSynthesizerRecipe(this.itemOutput, this.fluidOutput, this.itemInputs, this.fluidInput, this.energy);
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }
}
