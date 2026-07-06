package io.github.sapporo1101.appgen.common.blocks.networking;

import appeng.api.config.PowerUnit;
import appeng.block.AEBaseBlockItem;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import io.github.sapporo1101.appgen.api.AGComponents;
import io.github.sapporo1101.appgen.common.blocks.CreativeFluxCellBlock;
import io.github.sapporo1101.appgen.common.blocks.FluxCellBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class FluxCellBlockItem extends AEBaseBlockItem {

    public FluxCellBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void addCheckedInformation(ItemStack stack, TooltipContext context, Consumer<Component> toolTip,
                                      TooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, context, toolTip, advancedTooltips);
        double storedEnergy = this.getStoredFE(stack);
        double maxEnergy = this.getMaxFE();
        toolTip.accept(
                Tooltips.of(
                        Tooltips.of(GuiText.StoredEnergy),
                        Tooltips.of(": "),
                        Tooltips.ofNumber(storedEnergy, maxEnergy),
                        Tooltips.of(" "),
                        Tooltips.of(PowerUnit.FE),
                        Tooltips.of(" ("),
                        Tooltips.ofPercent(storedEnergy / maxEnergy),
                        Tooltips.of(")")
                )
        );
    }

    private double getStoredFE(ItemStack stack) {
        return stack.getOrDefault(AGComponents.STORED_ENERGY, 0.0);
    }

    private double getMaxFE() {
        Block block = this.getBlock();
        int slotCount = 36;
        return switch (block) {
            case FluxCellBlock.Standard ignored -> FluxCellBlock.Standard.MAX_CAPACITY * slotCount;
            case FluxCellBlock.Dense ignored -> FluxCellBlock.Dense.MAX_CAPACITY * slotCount;
            case CreativeFluxCellBlock ignored -> CreativeFluxCellBlock.MAX_CAPACITY * slotCount;
            default -> 0.0;
        };
    }
}
