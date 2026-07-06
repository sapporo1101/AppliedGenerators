package io.github.sapporo1101.appgen.menu;

import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import com.glodblock.github.glodium.network.packet.sync.ActionMap;
import com.glodblock.github.glodium.network.packet.sync.IActionHolder;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.api.AAESettings;
import io.github.sapporo1101.appgen.common.blockentities.SingularityGeneratorBlockEntity;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SingularityGeneratorMenu extends UpgradeableMenu<SingularityGeneratorBlockEntity> implements IProgressProvider, IActionHolder {

    @GuiSync(3)
    public long generatableFE = 0;

    @GuiSync(4)
    public double lastGeneratePerTick = 0;

    @GuiSync(8)
    public YesNo meExport = YesNo.YES;

    @GuiSync(9)
    public DirectionSet outputSides = new DirectionSet(List.of());

    private final ActionMap actions = ActionMap.create();

    public static final MenuType<@NotNull SingularityGeneratorMenu> TYPE = MenuTypeBuilder
            .create(SingularityGeneratorMenu::new, SingularityGeneratorBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("singularity_generator"));

    public SingularityGeneratorMenu(int id, Inventory playerInventory, SingularityGeneratorBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        this.addSlot(new AppEngSlot(host.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
        this.actions.put("set_side", o -> this.setOutputSide(o.get(Direction.class), o.getBoolean()));
    }

    private void setOutputSide(Direction side, boolean value) {
        if (value) {
            this.getHost().getOutputSides().add(side);
        } else {
            this.getHost().getOutputSides().remove(side);
        }
        this.getHost().saveChanges();
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.meExport = this.getHost().getConfigManager().getSetting(AAESettings.ME_EXPORT);
        this.outputSides.reload(this.getHost().getOutputSides());
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            this.generatableFE = this.getHost().getGeneratableFE();
            this.lastGeneratePerTick = this.getHost().getLastGeneratePerTick();
        }
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int) Math.ceil((double) this.generatableFE / this.getHost().getFEPerSingularity() * this.getMaxProgress());
    }

    @Override
    public int getMaxProgress() {
        return 10;
    }

    public YesNo getMeExport() {
        return this.meExport;
    }

    public List<Direction> getOutputSides() {
        return outputSides.asList();
    }

    @Override
    public @NotNull ActionMap getActionMap() {
        return this.actions;
    }
}
