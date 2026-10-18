package io.github.sapporo1101.appgen.recipe;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import io.github.sapporo1101.appgen.AppliedGenerators;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class GenesisSynthesizerRecipe implements Recipe<RecipeInput> {

    public static final ResourceLocation ID = AppliedGenerators.id("synthesizing");
    public static final RecipeType<GenesisSynthesizerRecipe> TYPE = RecipeType.simple(ResourceLocation.parse(ID.toString()));

    protected final List<IngredientStack.Item> itemInputs;
    protected final IngredientStack.Fluid fluidInput;
    public final GenericStack output;

    protected final long energy;

    public GenesisSynthesizerRecipe(
            GenericStack output, List<IngredientStack.Item> itemInputs, IngredientStack.Fluid fluidInput, long energy) {
        this.itemInputs = itemInputs;
        this.output = output;
        this.fluidInput = fluidInput;
        this.energy = energy;
    }

    @Override
    public boolean matches(@NotNull RecipeInput recipeInput, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput inv, HolderLookup.@NotNull Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return getResultItem();
    }

    public boolean isItemOutput() {
        return this.output.what() instanceof AEItemKey;
    }

    public ItemStack getResultItem() {
        if (this.output.what() instanceof AEItemKey key) {
            return key.toStack((int) this.output.amount());
        }
        return ItemStack.EMPTY;
    }

    public FluidStack getResultFluid() {
        if (this.output.what() instanceof AEFluidKey key) {
            return key.toStack((int) this.output.amount());
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GenesisSynthesizerRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
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

        if (this.fluidInput != null && !this.fluidInput.isEmpty()) {
            validInputs.add(this.fluidInput.sample());
        }
        return validInputs;
    }

    @Nullable
    public IngredientStack.Fluid getFluidInput() {
        return this.fluidInput;
    }

    public long getEnergy() {
        return this.energy;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
