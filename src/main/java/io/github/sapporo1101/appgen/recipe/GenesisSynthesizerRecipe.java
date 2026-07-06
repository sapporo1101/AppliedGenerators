package io.github.sapporo1101.appgen.recipe;

import appeng.recipes.MechanicsRecipe;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import io.github.sapporo1101.appgen.AppliedGenerators;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenesisSynthesizerRecipe extends MechanicsRecipe<RecipeInput> {

    public static final Identifier ID = AppliedGenerators.id("synthesizing");
    public static final RecipeType<@NotNull GenesisSynthesizerRecipe> TYPE = RecipeType.simple(ID);

    protected final List<IngredientStack.Item> itemInputs;
    protected final IngredientStack.Fluid fluid;
    public final ItemStackTemplate itemOutput;
    public final FluidStackTemplate fluidOutput;
    protected final long energy;

    public GenesisSynthesizerRecipe(ItemStackTemplate itemOutput, FluidStackTemplate fluidOutput, List<IngredientStack.Item> itemInputs, IngredientStack.Fluid fluidInput, long energy) {
        this.itemInputs = itemInputs;
        this.fluid = fluidInput;
        this.itemOutput = itemOutput;
        this.fluidOutput = fluidOutput;
        this.energy = energy;
    }

    public boolean isItemOutput() {
        return this.itemOutput != null;
    }

    public ItemStack getResultItem() {
        if (this.isItemOutput()) return Objects.requireNonNull(this.itemOutput).create();
        return ItemStack.EMPTY;
    }

    public FluidStack getResultFluid() {
        if (!this.isItemOutput()) return Objects.requireNonNull(this.fluidOutput).create();
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<? extends @NotNull Recipe<@NotNull RecipeInput>> getSerializer() {
        return GenesisSynthesizerRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends @NotNull Recipe<@NotNull RecipeInput>> getType() {
        return TYPE;
    }

    public List<IngredientStack.Item> getItemInputs() {
        return itemInputs;
    }

    public List<IngredientStack<?, ?>> getValidInputs() {
        List<IngredientStack<?, ?>> validInputs = new ArrayList<>();

        for (var input : this.itemInputs) {
            if (!input.isEmpty()) {
                validInputs.add(input.sample());
            }
        }

        if (this.fluid != null && !this.fluid.isEmpty()) {
            validInputs.add(this.fluid.sample());
        }
        return validInputs;
    }

    @Nullable
    public IngredientStack.Fluid getFluid() {
        return this.fluid;
    }

    public long getEnergy() {
        return this.energy;
    }
}