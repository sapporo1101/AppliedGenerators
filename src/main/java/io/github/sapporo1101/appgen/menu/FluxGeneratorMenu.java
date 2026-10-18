package io.github.sapporo1101.appgen.menu;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import com.glodblock.github.glodium.network.packet.sync.ActionMap;
import com.glodblock.github.glodium.network.packet.sync.IActionHolder;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.common.blockentities.FluxGeneratorBlockEntity;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FluxGeneratorMenu extends UpgradeableMenu<FluxGeneratorBlockEntity> implements IActionHolder {

    @GuiSync(3)
    public boolean isOn = false;

    @GuiSync(4)
    public double lastGeneratePerTick = 0;

    @GuiSync(8)
    public YesNo meExport = YesNo.YES;

    @GuiSync(9)
    public DirectionSet outputSides = new DirectionSet(new ArrayList<>());

    private final ActionMap actions = ActionMap.create();

    public static final MenuType<FluxGeneratorMenu> TYPE = MenuTypeBuilder
            .create(FluxGeneratorMenu::new, FluxGeneratorBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("flux_generator"));

    public FluxGeneratorMenu(int id, Inventory playerInventory, FluxGeneratorBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        this.actions.put("set_side", o -> this.setOutputSide(o.get(0), o.get(1)));
    }

    private void setOutputSide(String name, boolean value) {
        var side = Direction.byName(name);
        if (value) {
            this.getHost().getOutputSides().add(side);
        } else {
            this.getHost().getOutputSides().remove(side);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.meExport = this.getHost().getConfigManager().getSetting(AGSettings.ME_EXPORT);
        this.setRedStoneMode(this.getHost().getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED));
        this.outputSides.clear();
        this.outputSides.addAll(this.getHost().getOutputSides());
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            this.lastGeneratePerTick = this.getHost().getLastGeneratePerTick();
            this.isOn = this.getHost().isOn;
        }
        super.broadcastChanges();
    }

    public YesNo getMeExport() {
        return this.meExport;
    }

    public List<Direction> getOutputSides() {
        return outputSides.sides();
    }

    @Override
    public @NotNull ActionMap getActionMap() {
        return this.actions;
    }
}
