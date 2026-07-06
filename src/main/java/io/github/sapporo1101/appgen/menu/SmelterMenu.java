package io.github.sapporo1101.appgen.menu;

import appeng.api.config.Settings;
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
import io.github.sapporo1101.appgen.common.blockentities.SmelterBlockEntity;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SmelterMenu extends UpgradeableMenu<SmelterBlockEntity> implements IProgressProvider, IActionHolder {

    @GuiSync(3)
    public int maxProgress;

    @GuiSync(4)
    public int progress;

    @GuiSync(7)
    public boolean showWarning = false;

    @GuiSync(8)
    public YesNo autoExport = YesNo.NO;

    @GuiSync(9)
    public DirectionSet outputSides = new DirectionSet(List.of());

    private final ActionMap actions = ActionMap.create();

    public static final MenuType<@NotNull SmelterMenu> TYPE = MenuTypeBuilder
            .create(SmelterMenu::new, SmelterBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("smelter"));

    public SmelterMenu(int id, Inventory playerInventory, SmelterBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        this.addSlot(new AppEngSlot(host.getInputInv(), 0), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new AppEngSlot(host.getOutputExposed(), 0), SlotSemantics.MACHINE_OUTPUT);
        this.actions.put("set_side", o -> this.setOutputSide(o.get(Direction.class), o.getBoolean()));
    }

    private void setOutputSide(Direction side, boolean value) {
        this.getHost().setOutputSide(side, value);
        this.getHost().saveChanges();
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.autoExport = this.getHost().getConfigManager().getSetting(Settings.AUTO_EXPORT);
        this.outputSides.reload(this.getHost().getOutputSidesCopy());
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            this.progress = this.getHost().getProgress();
            this.maxProgress = this.getHost().getMaxProgress();
            this.showWarning = this.getHost().showWarning;
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int) Math.ceil((double) this.progress / this.maxProgress * this.getMaxProgress());
    }

    @Override
    public int getMaxProgress() {
        return 10;
    }

    public YesNo getAutoExport() {
        return autoExport;
    }

    public List<Direction> getOutputSides() {
        return outputSides.asList();
    }

    @NotNull
    @Override
    public ActionMap getActionMap() {
        return this.actions;
    }
}
