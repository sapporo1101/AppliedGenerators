package io.github.sapporo1101.appgen.recipe;

import appeng.api.stacks.GenericStack;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class GenesisSynthesizerRecipeSerializer implements RecipeSerializer<GenesisSynthesizerRecipe> {

    public static final GenesisSynthesizerRecipeSerializer INSTANCE = new GenesisSynthesizerRecipeSerializer();

    private GenesisSynthesizerRecipeSerializer() {
        // NO-OP
    }

    public static final MapCodec<GenesisSynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                    GenericStack.CODEC.fieldOf("output").forGetter((ir) -> ir.output),
                    IngredientStack.ITEM_CODEC.listOf().fieldOf("item_inputs").forGetter((ir) -> ir.itemInputs),
                    IngredientStack.FLUID_CODEC.fieldOf("fluid_input").forGetter((ir) -> ir.fluidInput),
                    Codec.LONG.fieldOf("energy_input").forGetter((ir) -> ir.energy))
            .apply(builder, GenesisSynthesizerRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenesisSynthesizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    GenericStack.STREAM_CODEC,
                    (r) -> r.output,
                    IngredientStack.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()),
                    (r) -> r.itemInputs,
                    IngredientStack.FLUID_STREAM_CODEC,
                    (r) -> r.fluidInput,
                    ByteBufCodecs.VAR_LONG,
                    (r) -> r.energy,
                    GenesisSynthesizerRecipe::new);

    @Override
    public @NotNull MapCodec<GenesisSynthesizerRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GenesisSynthesizerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
