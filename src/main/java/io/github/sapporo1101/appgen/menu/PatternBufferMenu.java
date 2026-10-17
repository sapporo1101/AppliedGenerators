package io.github.sapporo1101.appgen.menu;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
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
import io.github.sapporo1101.appgen.menu.inventory.PatternBufferMenuInventory;
import io.github.sapporo1101.appgen.menu.slots.PatternBufferInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class PatternBufferMenu extends UpgradeableMenu<PatternBufferBlockEntity> {

    @GuiSync(2)
    public KeepSignalMode keepSignal = KeepSignalMode.OFF;

    public static final MenuType<@NotNull PatternBufferMenu> TYPE = MenuTypeBuilder
            .create(PatternBufferMenu::new, PatternBufferBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("pattern_buffer"));

    public final RestrictedInputSlot patternSlot;

    public PatternBufferMenu(int id, Inventory playerInventory, PatternBufferBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        for (int index = 0; index < host.getStorageInv().size(); index++) {
            this.addSlot(new PatternBufferInvSlot(new PatternBufferMenuInventory(host.getStorageInv()), index, host.getPatternInput()), SlotSemantics.STORAGE);
        }
        this.patternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, new ConfigMenuInventory(host.getPatternInv()), 0);
        this.addSlot(patternSlot, SlotSemantics.ENCODED_PATTERN);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.keepSignal = this.getHost().getConfigManager().getSetting(AGSettings.KEEP_SIGNAL);
    }

    public KeepSignalMode getKeepSignal() {
        return this.keepSignal;
    }

    @Override
    public void swapSlotContents(int slotIdA, int slotIdB) {
        Slot slotA = this.slots.get(slotIdA);
        Slot slotB = this.slots.get(slotIdB);
        if (!canSwapSlots(slotA, slotB) || !canSwapSlots(slotB, slotA)) return;
        super.swapSlotContents(slotIdA, slotIdB);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canSwapSlots(Slot fromSlot, Slot toSlot) {
        if (toSlot instanceof PatternBufferInvSlot) {
            AEKey fromKey = getAEKeyFromSlot(fromSlot);
            int fromCount = fromSlot.getItem().getCount();
            return PatternBufferBlockEntity.canInsert(toSlot.getSlotIndex(), fromKey, fromCount, this.getHost().getPatternInput(), 0) == fromCount;
        } else if (toSlot == this.patternSlot) {
            return !this.getHost().isPatternSlotLocked();
        }
        return true;
    }

    private AEKey getAEKeyFromSlot(Slot slot) {
        if (slot instanceof AppEngSlot aeSlot && aeSlot.getInventory() instanceof PatternBufferMenuInventory inv) {
            return inv.getDelegate().getKey(aeSlot.getSlotIndex());
        }
        return AEItemKey.of(slot.getItem());
    }
}
