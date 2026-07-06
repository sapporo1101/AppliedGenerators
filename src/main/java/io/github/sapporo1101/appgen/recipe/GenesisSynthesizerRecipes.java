package io.github.sapporo1101.appgen.recipe;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.RecipeAccess;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GenesisSynthesizerRecipes {
    private GenesisSynthesizerRecipes() {
    }

    public static Iterable<RecipeHolder<@NotNull GenesisSynthesizerRecipe>> getRecipes(Level level) {
        return RecipeAccess.byType(level, GenesisSynthesizerRecipe.TYPE);
    }

    @Nullable
    public static GenesisSynthesizerRecipe findRecipe(Level level, List<ItemStack> inputs, GenericStack fluid) {
        List<ItemStack> machineInputs = new ArrayList<>();
        for (var stack : inputs)
            if (!stack.isEmpty()) {
                machineInputs.add(stack);
            }

        for (var holder : getRecipes(level)) {
            var recipe = holder.value();

            var validInputs = recipe.getValidInputs();

            boolean failed = false;
            for (var input : validInputs) {
                boolean found = false;
                for (var machineInput : machineInputs) {
                    if (input.checkType(machineInput)) {
                        if (((IngredientStack.Item) input).getIngredient().test(machineInput)
                                && input.getAmount() <= machineInput.getCount()) {
                            found = true;
                            break;
                        }
                    }
                }

                if (input instanceof IngredientStack.Fluid fluidIn) {
                    if (fluid != null && fluid.what() instanceof AEFluidKey key) {
                        FluidStack fluidStack = key.toStack((int) fluid.amount());
                        if (fluidIn.getIngredient().test(fluidStack) && input.getAmount() <= fluid.amount()) {
                            found = true;
                        }
                    }
                }

                if (!found) {
                    failed = true;
                    break;
                }
            }
            if (failed) {
                continue;
            }
            return recipe;
        }
        return null;
    }
}
