package io.github.sapporo1101.appgen.xmod.rei;

import appeng.core.AppEng;
import appeng.util.ReadableNumberConverter;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blockentities.GenesisSynthesizerBlockEntity;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class REIGenesisSynthesizerCategory implements DisplayCategory<REIGenesisSynthesizerDisplay> {

    private static final int PADDING = 4;

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AGSingletons.GENESIS_SYNTHESIZER);
    }

    @Override
    public Component getTitle() {
        return AGSingletons.GENESIS_SYNTHESIZER.get().getName();
    }

    @Override
    public CategoryIdentifier<REIGenesisSynthesizerDisplay> getCategoryIdentifier() {
        return REIGenesisSynthesizerDisplay.ID;
    }

    @Override
    public List<Widget> setupDisplay(REIGenesisSynthesizerDisplay recipeDisplay, Rectangle bounds) {
        Identifier background = AppEng.makeId("textures/guis/genesis_synthesizer.png");
        Identifier aeIcon = AppEng.makeId("textures/xei/xei_icons.png");

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createTexturedWidget(background, bounds.x + PADDING, bounds.y + PADDING, 4, 13, 168, 80));

        var energyLabel = Widgets.createLabel(
                new Point(bounds.x + bounds.width / 2 + 4 + PADDING, bounds.y + 70 + PADDING),
                Component.translatable("emi.text.appgen.genesis_synthesizer.energy", ReadableNumberConverter.format(recipeDisplay.getEnergy(), 4)));
        widgets.add(energyLabel);
        var energyLabelX = energyLabel.getBounds().getX();
        var energyLabelY = 72 + (energyLabel.getBounds().getHeight() - PADDING) / 2 + bounds.y + PADDING;
        widgets.add(Widgets.createTexturedWidget(aeIcon, energyLabelX - 16, energyLabelY - 9, 0, 0, 16, 16));

        int index = 0;

        int crystalAmount = 0;
        for (EntryIngredient in : recipeDisplay.getInputItems()) {
            // if ingredient is charged ember crystal, set it to another position
            if (in.contains(EntryStacks.of(AGSingletons.EMBER_CRYSTAL_CHARGED))) {
                widgets.add(Widgets.createSlot(new Point(bounds.x + 69 + 1 + PADDING, bounds.y + 10 + 1 + PADDING))
                        .disableBackground()
                        .markInput()
                        .entries(in));
                EntryStack<ItemStack> stack = in.getFirst().cheatsAs();
                crystalAmount = stack.getValue().getCount();
                continue;
            }
            var x = 5 + index % 3 * 18 + 1;
            var y = 10 + index / 3 * 18 + 1;
            if (!in.isEmpty()) {
                widgets.add(Widgets.createSlot(new Point(bounds.x + x + PADDING, bounds.y + y + PADDING))
                        .disableBackground()
                        .markInput()
                        .entries(in));
            }
            index++;
        }

        int crystalHeight = 18 * crystalAmount / GenesisSynthesizerBlockEntity.MAX_CRYSTAL_TANK;
        widgets.add(Widgets.createTexturedWidget(background, bounds.x + 89 + PADDING, bounds.y + 10 + Math.max(18 - crystalHeight, 0) + PADDING, 182, Math.max(18 - crystalHeight, 0), 6, 18));

        if (!recipeDisplay.getInputFluid().isEmpty()) {
            widgets.add(Widgets.createSlot(new Rectangle(bounds.x + 60 + PADDING, bounds.y + 46 + PADDING, 18, 18))
                    .disableBackground()
                    .markInput()
                    .entries(recipeDisplay.getInputFluid()));
        }

        var output = recipeDisplay.getOutputEntries();
        if (!output.getFirst().isEmpty()) {
            widgets.add(Widgets.createSlot(new Point(bounds.x + 113 + 1 + PADDING, bounds.y + 28 + 1 + PADDING))
                    .disableBackground()
                    .markOutput()
                    .entries(output.getFirst()));
        }
        if (!output.get(1).isEmpty()) {
            widgets.add(Widgets.createSlot(new Rectangle(bounds.x + 147 + PADDING, bounds.y + 28 + PADDING, 18, 18))
                    .disableBackground()
                    .markOutput()
                    .entries(output.get(1)));
        }
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 80 + 2 * PADDING;
    }

    @Override
    public int getDisplayWidth(REIGenesisSynthesizerDisplay display) {
        return 168 + 2 * PADDING;
    }
}
