package io.github.sapporo1101.appgen.xmod.jei;

import appeng.core.AppEng;
import appeng.util.ReadableNumberConverter;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blockentities.GenesisSynthesizerBlockEntity;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class JEIGenesisSynthesizerCategory implements IRecipeCategory<RecipeHolder<@NotNull GenesisSynthesizerRecipe>> {
    public static final IRecipeType<@NotNull RecipeHolder<@NotNull GenesisSynthesizerRecipe>> RECIPE_TYPE = IRecipeType.create(GenesisSynthesizerRecipe.TYPE);

    private static final Identifier BACKGROUND = AppEng.makeId("textures/guis/genesis_synthesizer.png");
    private static final Identifier AE_TEXTURE = AppEng.makeId("textures/xei/xei_icons.png");

    private final IDrawable icon;

    private final IDrawable background;

    private final IDrawableAnimated progress;

    private final IDrawableStatic aeIcon;

    public JEIGenesisSynthesizerCategory(IGuiHelper helper) {
        background = helper.createDrawable(BACKGROUND, 5, 15, 168, 75);
        icon = helper.createDrawableItemStack(new ItemStack(AGSingletons.GENESIS_SYNTHESIZER));

        IDrawableStatic progressDrawable = helper.createDrawable(BACKGROUND, 176, 0, 6, 18);
        this.progress =
                helper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM, false);

        aeIcon = helper.createDrawable(AE_TEXTURE, 0, 0, 16, 16);
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public @NotNull IRecipeType<RecipeHolder<@NotNull GenesisSynthesizerRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.appgen.genesis_synthesizer");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<@NotNull GenesisSynthesizerRecipe> recipeHolder, @NotNull IFocusGroup focuses) {
        var index = 0;
        var recipe = recipeHolder.value();
        var inputs = recipe.getItemInputs();
        for (IngredientStack.Item in : inputs) {
            // if ingredient is charged ember crystal, set it to another position
            if (in.getIngredient().test(new ItemStack(AGSingletons.EMBER_CRYSTAL_CHARGED.get()))) {
                builder.addInputSlot(69, 10 - 1).addItemStacks(JEIPlugin.stackOf(in));
                continue;
            }
            int x = 5 + index % 3 * 18;
            int y = 10 + index / 3 * 18 - 1;
            if (!in.isEmpty()) {
                builder.addInputSlot(x, y).addItemStacks(JEIPlugin.stackOf(in));
            }
            index++;
        }

        if (recipe.getFluid() != null) {
            IngredientStack.Fluid fluid = recipe.getFluid();
            IRecipeSlotBuilder slot = builder.addInputSlot(60, 46 - 1).setFluidRenderer(16000, false, 16, 16);
            slot.addIngredients(NeoForgeTypes.FLUID_STACK, JEIPlugin.stackOf(fluid));
        }

        if (recipe.isItemOutput()) {
            builder.addOutputSlot(113, 28 - 1).add(recipe.getResultItem());
        } else {
            IRecipeSlotBuilder slot = builder.addOutputSlot(147, 28 - 1).setFluidRenderer(16000, false, 16, 16);
            slot.add(
                    recipe.getResultFluid().getFluid(), recipe.getResultFluid().getAmount());
        }
    }

    @Override
    public void draw(
            RecipeHolder<@NotNull GenesisSynthesizerRecipe> recipeHolder,
            @NotNull IRecipeSlotsView recipeSlotsView,
            @NotNull GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY) {
        var recipe = recipeHolder.value();
        this.background.draw(guiGraphics);
        this.progress.draw(guiGraphics, 135, 27 - 1);

        int crystalAmount = recipe.getItemInputs().stream().filter(item -> item.getIngredient().test(new ItemStack(AGSingletons.EMBER_CRYSTAL_CHARGED.get()))).toList().getLast().getAmount();
        int crystalHeight = 18 * crystalAmount / GenesisSynthesizerBlockEntity.MAX_CRYSTAL_TANK;
        guiGraphics.blit(BACKGROUND, 88, 9 + Math.max(18 - crystalHeight, 0) - 1, 182, Math.max(18 - crystalHeight, 0), 6, 18, 0, 0);

        Font font = Minecraft.getInstance().font;
        Component text = Component.translatable("emi.text.appgen.genesis_synthesizer.energy", ReadableNumberConverter.format(recipe.getEnergy(), 4));
        FormattedCharSequence formattedcharsequence = text.getVisualOrderText();
        int textX = getWidth() / 2 + 4 - font.width(formattedcharsequence) / 2;
        guiGraphics.text(font, text, textX, 67, 0xFF7E7E7E, false);
        aeIcon.draw(guiGraphics, textX - 16, 65);
    }
}
