package io.github.sapporo1101.appgen.xmod.rei;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipeSerializer;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class REIGenesisSynthesizerDisplay implements Display {
    public static final CategoryIdentifier<REIGenesisSynthesizerDisplay> ID =
            CategoryIdentifier.of(AppliedGenerators.id("rei_genesis_synthesizer"));
    private final RecipeHolder<@NotNull GenesisSynthesizerRecipe> holder;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;
    private final List<EntryIngredient> combined;
    private final EntryIngredient fluid;
    private final long energy;

    public static final DisplaySerializer<REIGenesisSynthesizerDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("recipe_id").forGetter(display -> display.holder.id().identifier()),
                    GenesisSynthesizerRecipeSerializer.CODEC.fieldOf("recipe").forGetter(display -> display.holder.value())
            ).apply(instance, (recipeId, recipe) -> new REIGenesisSynthesizerDisplay(new RecipeHolder<>(
                    ResourceKey.create(Registries.RECIPE, recipeId), recipe
            )))),

            StreamCodec.composite(
                    Identifier.STREAM_CODEC,
                    display -> display.holder.id().identifier(),
                    GenesisSynthesizerRecipeSerializer.STREAM_CODEC,
                    display -> display.holder.value(),
                    (recipeId, recipe) -> new REIGenesisSynthesizerDisplay(new RecipeHolder<>(
                            ResourceKey.create(Registries.RECIPE, recipeId), recipe
                    ))
            )
    );

    public REIGenesisSynthesizerDisplay(RecipeHolder<@NotNull GenesisSynthesizerRecipe> holder) {
        this.holder = holder;
        var recipe = holder.value();
        this.inputs = recipe.getItemInputs().stream()
                .map(REIPlugin::stackOf)
                .filter(o -> !o.isEmpty())
                .toList();
        this.fluid = recipe.getFluid() != null ? REIPlugin.stackOf(recipe.getFluid(), 16000) : EntryIngredient.empty();

        var fluid = EntryStacks.of(
                recipe.getResultFluid().getFluid(), recipe.getResultFluid().getAmount());
        ClientEntryStacks.setFluidRenderRatio(fluid, recipe.getResultFluid().getAmount() / 16000f);
        this.outputs = ImmutableList.of(
                EntryIngredients.of(recipe.getResultItem()),
                EntryIngredient.builder().add(fluid).build());

        this.combined = new ArrayList<>(this.inputs);
        if (!this.fluid.isEmpty()) {
            this.combined.addLast(this.fluid);
        }
        this.energy = recipe.getEnergy();
    }

    public List<EntryIngredient> getInputItems() {
        return this.inputs;
    }

    public EntryIngredient getInputFluid() {
        return this.fluid;
    }

    public long getEnergy() {
        return this.energy;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return this.combined;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return this.outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(holder.id().identifier());
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
