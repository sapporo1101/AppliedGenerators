package io.github.sapporo1101.appgen.recipe;

import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GenesisSynthesizerRecipeSerializer {
    private GenesisSynthesizerRecipeSerializer() {
        // NO-OP
    }

    public static RecipeSerializer<@NotNull GenesisSynthesizerRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    public static final MapCodec<GenesisSynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                    ItemStackTemplate.CODEC.optionalFieldOf("item_output").forGetter((ir) -> Optional.ofNullable(ir.itemOutput)),
                    FluidStackTemplate.CODEC.optionalFieldOf("fluid_output").forGetter((ir) -> Optional.ofNullable(ir.fluidOutput)),
                    IngredientStack.ITEM_CODEC.listOf().fieldOf("item_inputs").forGetter((ir) -> ir.itemInputs),
                    IngredientStack.FLUID_CODEC.fieldOf("fluid_input").forGetter((ir) -> ir.fluid),
                    Codec.LONG.fieldOf("energy_input").forGetter((ir) -> ir.energy))
            .apply(builder, (itemOutput, fluidOutput, itemInputs, fluidInput, energy) ->
                    new GenesisSynthesizerRecipe(
                            itemOutput.orElse(null),
                            fluidOutput.orElse(null),
                            itemInputs,
                            fluidInput,
                            energy
                    )));

    public static final StreamCodec<RegistryFriendlyByteBuf, GenesisSynthesizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC)
                            .map(opt -> opt.orElse(null), Optional::ofNullable),
                    (r) -> r.itemOutput,
                    ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC)
                            .map(opt -> opt.orElse(null), Optional::ofNullable),
                    (r) -> r.fluidOutput,
                    IngredientStack.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()),
                    (r) -> r.itemInputs,
                    IngredientStack.FLUID_STREAM_CODEC,
                    (r) -> r.fluid,
                    ByteBufCodecs.LONG,
                    (r) -> r.energy,
                    GenesisSynthesizerRecipe::new);

    public final static RecipeSerializer<@NotNull GenesisSynthesizerRecipe> INSTANCE = create();
}
