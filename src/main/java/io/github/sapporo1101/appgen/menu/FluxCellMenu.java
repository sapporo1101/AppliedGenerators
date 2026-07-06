package io.github.sapporo1101.appgen.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import com.glodblock.github.glodium.network.packet.sync.ActionMap;
import com.glodblock.github.glodium.network.packet.sync.IActionHolder;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.blockentities.FluxCellBaseBlockEntity;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluxCellMenu extends AEBaseMenu implements IActionHolder {

    @GuiSync(9)
    public DirectionSet outputSides = new DirectionSet(List.of());

    private final FluxCellBaseBlockEntity host;
    private final ActionMap actions = ActionMap.create();

    public static final MenuType<@NotNull FluxCellMenu> TYPE = MenuTypeBuilder
            .create(FluxCellMenu::new, FluxCellBaseBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("flux_cell"));

    public FluxCellMenu(int id, Inventory playerInventory, FluxCellBaseBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        for (int index = 0; index < host.getGenericInv().size(); index++) {
            this.addSlot(new AppEngSlot(new ConfigMenuInventory(host.getGenericInv()), index), SlotSemantics.STORAGE);
        }
        this.createPlayerInventorySlots(playerInventory);
        this.actions.put("set_side", o -> this.setOutputSide(o.get(Direction.class), o.getBoolean()));
    }

    private void setOutputSide(Direction side, boolean value) {
        if (value) {
            this.host.getOutputSides().add(side);
        } else {
            this.host.getOutputSides().remove(side);
        }
        this.getHost().saveChanges();
    }

    @Override
    public void broadcastChanges() {
        this.outputSides.reload(this.host.getOutputSides());
        super.broadcastChanges();
    }

    public List<Direction> getOutputSides() {
        return outputSides.asList();
    }

    public FluxCellBaseBlockEntity getHost() {
        return this.host;
    }

    @Override
    public @NotNull ActionMap getActionMap() {
        return this.actions;
    }
}