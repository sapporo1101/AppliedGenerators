package io.github.sapporo1101.appgen.xmod.emi;

import appeng.core.AppEng;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blockentities.GenesisSynthesizerBlockEntity;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import io.github.sapporo1101.appgen.util.CommaSeparator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class EMIGenesisSynthesizerRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY;
    private final GenesisSynthesizerRecipe recipe;

    public EMIGenesisSynthesizerRecipe(RecipeHolder<GenesisSynthesizerRecipe> holder) {
        super(CATEGORY, holder.id(), 168, 80);
        this.recipe = holder.value();

        for (IngredientStack.Item in : this.recipe.getInputs()) {
            if (!in.isEmpty()) {
                this.inputs.add(EMIPlugin.stackOf(in));
            }
        }

        if (this.recipe.getFluid() != null) {
            this.inputs.add(EMIPlugin.stackOf(this.recipe.getFluid()));
        }

        this.outputs.add(EmiStack.of(this.recipe.getResultItem()));
        this.outputs.add(EmiStack.of(this.recipe.getResultFluid().getFluid(), this.recipe.getResultFluid().getAmount()));
    }

    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation background = AppEng.makeId("textures/guis/genesis_synthesizer.png");
        ResourceLocation fe = AppliedGenerators.id("textures/gui/emi_ae.png");
        widgets.addTexture(background, 0, 0, 168, 80, 4, 13);
        widgets.addAnimatedTexture(background, 136, 28, 6, 18, 176, 0, 2000, false, true, false);
        // noinspection DataFlowIssue
        TextWidget energyLabel = widgets.addText(Component.translatable("emi.text.appgen.genesis_synthesizer.energy", CommaSeparator.FORMATTER.format(this.recipe.getEnergy())), this.width / 2 + 4, 70, ChatFormatting.DARK_GRAY.getColor(), false).horizontalAlign(TextWidget.Alignment.CENTER);
        int energyLabelX = energyLabel.getBounds().x();
        int energyLabelY = 72 + energyLabel.getBounds().height() / 2;
        widgets.addTexture(fe, energyLabelX - 16, energyLabelY - 8, 10, 12, 0, 0, 10, 12, 32, 32);
        int index = 0;

        IngredientStack.Item crystal = null;
        for (IngredientStack.Item in : this.recipe.getInputs()) {
            // if ingredient is charged ember crystal, set it to another position
            if (in.getIngredient().test(new ItemStack(AGSingletons.EMBER_CRYSTAL_CHARGED))) {
                crystal = in;
                continue;
            }
            int x = 5 + index % 3 * 18;
            int y = 10 + index / 3 * 18;
            if (!in.isEmpty()) widgets.addSlot(EMIPlugin.stackOf(in), x, y).drawBack(false);

            index++;
        }
        int crystalHeight = 18 * (crystal != null ? crystal.getAmount() : 0) / GenesisSynthesizerBlockEntity.MAX_CRYSTAL_TANK;
        widgets.addTexture(background, 89, 10 + Math.max(18 - crystalHeight, 0), 6, 18, 182, Math.max(18 - crystalHeight, 0));
        if (crystal != null) widgets.addSlot(EMIPlugin.stackOf(crystal), 69, 10).drawBack(false);

        if (this.recipe.getFluid() != null) {
            widgets.addTank(EMIPlugin.stackOf(this.recipe.getFluid()), 60, 46, 18, 18, 16000).drawBack(false);
        }

        if (this.recipe.isItemOutput()) {
            widgets.addSlot(EmiStack.of(this.recipe.getResultItem()), 113, 28).recipeContext(this).drawBack(false);
        } else {
            widgets.addTank(EmiStack.of(this.recipe.getResultFluid().getFluid()), 147, 28, 18, 18, 16000).recipeContext(this).drawBack(false);
        }

    }

    static {
        CATEGORY = new AGRecipeCategory("synthesizing", EmiStack.of(AGSingletons.GENESIS_SYNTHESIZER), Component.translatable("emi.category.appgen.genesis_synthesizer"));
    }
}
