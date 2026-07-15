package io.github.sapporo1101.appgen.menu;

import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigMenuInventory;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.api.KeepSignalMode;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class PatternBufferMenu extends UpgradeableMenu<PatternBufferBlockEntity> {

    @GuiSync(2)
    public KeepSignalMode keepSignal = KeepSignalMode.OFF;

    public static final MenuType<@NotNull PatternBufferMenu> TYPE = MenuTypeBuilder
            .create(PatternBufferMenu::new, PatternBufferBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("pattern_buffer"));

    public PatternBufferMenu(int id, Inventory playerInventory, PatternBufferBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        for (int index = 0; index < host.getStorageInv().size(); index++) {
            this.addSlot(new AppEngSlot(new ConfigMenuInventory(host.getStorageInv().getInv(index)), 0), SlotSemantics.STORAGE);
        }
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, host.getPatternInv(), 0), SlotSemantics.ENCODED_PATTERN);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.keepSignal = this.getHost().getConfigManager().getSetting(AGSettings.KEEP_SIGNAL);
    }

    public KeepSignalMode getKeepSignal() {
        return this.keepSignal;
    }
}
