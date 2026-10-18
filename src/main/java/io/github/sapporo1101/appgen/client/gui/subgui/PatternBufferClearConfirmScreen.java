package io.github.sapporo1101.appgen.client.gui.subgui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.SlotSemantics;
import io.github.sapporo1101.appgen.menu.PatternBufferMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PatternBufferClearConfirmScreen
        extends AESubScreen<PatternBufferMenu, AEBaseScreen<PatternBufferMenu>> {

    private final int slot;

    public PatternBufferClearConfirmScreen(AEBaseScreen<PatternBufferMenu> parent, int slot, boolean clearAll) {
        super(parent, "/screens/pattern_buffer_clear.json");
        this.slot = slot;

        this.widgets.add("back", new TabButton(Icon.BACK,
                Component.translatable("gui.appgen.pattern_buffer.clear.cancel"), b -> this.returnToParent()));

        this.widgets.addButton("clear", Component.translatable("gui.appgen.pattern_buffer.clear.confirm"),
                this::confirm);

        this.setTextContent("dialog_title", clearAll
                ? Component.translatable("gui.appgen.pattern_buffer.clear.title.all",
                Component.translatable("gui.appgen.pattern_buffer.clear.marker.all")
                        .withStyle(ChatFormatting.RED))
                : Component.translatable("gui.appgen.pattern_buffer.clear.title.slot"));
        this.setTextContent("clear_info", clearAll
                ? Component.translatable("gui.appgen.pattern_buffer.clear.info.all",
                Component.translatable("gui.appgen.pattern_buffer.clear.marker.all")
                        .withStyle(ChatFormatting.RED))
                : Component.translatable("gui.appgen.pattern_buffer.clear.info.slot",
                Component.translatable("gui.appgen.pattern_buffer.clear.marker.slot")
                        .withStyle(ChatFormatting.RED)));
    }

    private void confirm() {
        this.getParent().getMenu().requestClear(this.slot);
        this.returnToParent();
    }

    @Override
    public void init() {
        super.init();
        this.setSlotsHidden(SlotSemantics.STORAGE, true);
        this.setSlotsHidden(SlotSemantics.ENCODED_PATTERN, true);
    }
}
