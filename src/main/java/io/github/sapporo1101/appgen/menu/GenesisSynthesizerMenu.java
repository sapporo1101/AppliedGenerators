package io.github.sapporo1101.appgen.menu;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import com.glodblock.github.glodium.network.packet.sync.ActionMap;
import com.glodblock.github.glodium.network.packet.sync.IActionHolder;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.blockentities.GenesisSynthesizerBlockEntity;
import io.github.sapporo1101.appgen.menu.helper.DirectionSet;
import io.github.sapporo1101.appgen.menu.interfaces.ISubProgressProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GenesisSynthesizerMenu extends UpgradeableMenu<GenesisSynthesizerBlockEntity> implements IProgressProvider, ISubProgressProvider, IActionHolder {

    public static final SlotSemantic AG_TANK_INPUT = SlotSemantics.register("AG_TANK_INPUT", false);
    public static final SlotSemantic AG_TANK_OUTPUT = SlotSemantics.register("AG_TANK_OUTPUT", false);

    @GuiSync(2)
    public int crystalCount = -1;

    @GuiSync(3)
    public int processingTime = -1;

    @GuiSync(7)
    public boolean showWarning = false;

    @GuiSync(8)
    public YesNo autoExport = YesNo.NO;

    @GuiSync(9)
    public DirectionSet outputSides = new DirectionSet(List.of());

    private final ActionMap actions = ActionMap.create();

    public static final MenuType<@NotNull GenesisSynthesizerMenu> TYPE = MenuTypeBuilder
            .create(GenesisSynthesizerMenu::new, GenesisSynthesizerBlockEntity.class)
            .buildUnregistered(AppliedGenerators.id("genesis_synthesizer"));

    public GenesisSynthesizerMenu(int id, Inventory inventory, GenesisSynthesizerBlockEntity host) {
        super(TYPE, id, inventory, host);
        for (int index = 0; index < host.getInputInv().size(); index++) {
            this.addSlot(new AppEngSlot(host.getInputInv(), index), SlotSemantics.MACHINE_INPUT);
        }
        this.addSlot(new AppEngSlot(host.getCrystalInv(), 0), SlotSemantics.STORAGE);
        for (int index = 0; index < host.getOutputExposed().size(); index++) {
            this.addSlot(new AppEngSlot(host.getOutputExposed(), index), SlotSemantics.MACHINE_OUTPUT);
        }
        this.addSlot(new AppEngSlot(new ConfigMenuInventory(host.getTank()), 0), AG_TANK_INPUT);
        this.addSlot(new AppEngSlot(new ConfigMenuInventory(host.getTank()), 1), AG_TANK_OUTPUT);
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
            this.processingTime = this.getHost().getProgress();
            this.crystalCount = this.getHost().getCrystalCount();
            this.showWarning = this.getHost().showWarning();
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public int getCurrentProgress() {
        return this.processingTime;
    }

    @Override
    public int getMaxProgress() {
        return GenesisSynthesizerBlockEntity.MAX_PROGRESS;
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

    @Override
    public int getCurrentSubProgress() {
        return this.crystalCount;
    }

    @Override
    public int getMaxSubProgress() {
        return GenesisSynthesizerBlockEntity.MAX_CRYSTAL_TANK;
    }
}
