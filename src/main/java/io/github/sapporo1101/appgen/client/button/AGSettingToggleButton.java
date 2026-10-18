package io.github.sapporo1101.appgen.client.button;

import appeng.api.config.Setting;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.core.localization.ButtonToolTips;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.api.KeepSignalMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class AGSettingToggleButton<T extends Enum<T>> extends AGIconButton {
    private static Map<EnumPair<?>, ButtonAppearance> appearances;
    private final Setting<T> buttonSetting;
    private final IHandler<AGSettingToggleButton<T>> onPress;
    private T currentValue;

    @FunctionalInterface
    public interface IHandler<T extends AGSettingToggleButton<?>> {
        void handle(T button, boolean backwards);
    }

    public AGSettingToggleButton(
            Setting<T> setting, T val, IHandler<AGSettingToggleButton<T>> onPress) {
        this(setting, val, t -> true, onPress);
    }

    public AGSettingToggleButton(
            Setting<T> setting,
            T val,
            Predicate<T> isValidValue,
            IHandler<AGSettingToggleButton<T>> onPress) {
        super(AGSettingToggleButton::onPress);
        this.onPress = onPress;

        // Build a list of values (in order) that are valid w.r.t. the given predicate
        EnumSet<T> validValues = EnumSet.allOf(val.getDeclaringClass());
        validValues.removeIf(isValidValue.negate());
        validValues.removeIf(s -> !setting.getValues().contains(s));

        this.buttonSetting = setting;
        this.currentValue = val;

        if (appearances == null) {

            appearances = new HashMap<>();
            registerApp(AGIcon.ME_EXPORT_ON, AGSettings.ME_EXPORT, YesNo.YES, Component.translatable("gui.appgen.me_export"), Component.translatable("gui.appgen.me_export.on"));
            registerApp(AGIcon.ME_EXPORT_OFF, AGSettings.ME_EXPORT, YesNo.NO, Component.translatable("gui.appgen.me_export"), Component.translatable("gui.appgen.me_export.off"));
            registerApp(AGIcon.KEEP_SIGNAL_ON, AGSettings.KEEP_SIGNAL, KeepSignalMode.ON, Component.translatable("gui.appgen.keep_signal"), Component.translatable("gui.appgen.keep_signal.on"));
            registerApp(AGIcon.KEEP_SIGNAL_OFF, AGSettings.KEEP_SIGNAL, KeepSignalMode.OFF, Component.translatable("gui.appgen.keep_signal"), Component.translatable("gui.appgen.keep_signal.off"));
            registerApp(AGIcon.KEEP_SIGNAL_LOCKED, AGSettings.KEEP_SIGNAL, KeepSignalMode.LOCKED, Component.translatable("gui.appgen.keep_signal"), Component.translatable("gui.appgen.keep_signal.locked"));
        }
    }

    private static void onPress(Button btn) {
        if (btn instanceof AGSettingToggleButton) {
            ((AGSettingToggleButton<?>) btn).triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = false;
        // This isn't great, but we don't get any information about right-clicks
        // otherwise
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AEBaseScreen) {
            backwards = ((AEBaseScreen<?>) currentScreen).isHandlingRightClick();
        }
        onPress.handle(this, backwards);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Enum<T>> void registerApp(
            AGIcon icon, Setting<T> setting, T val, Component... tooltipLines) {
        var lines = new ArrayList<Component>();
        Collections.addAll(lines, tooltipLines);
        appearances.put(
                new EnumPair<>(setting, val),
                new ButtonAppearance(icon, null, lines));
    }

    @Nullable
    private AGSettingToggleButton.ButtonAppearance getAppearance() {
        if (this.buttonSetting != null && this.currentValue != null) {
            return appearances.get(new EnumPair<>(this.buttonSetting, this.currentValue));
        }
        return null;
    }

    @Override
    protected AGIcon getIcon() {
        var app = getAppearance();
        if (app != null && app.icon != null) {
            return app.icon;
        }
        return AGIcon.TOOLBAR_BUTTON_BACKGROUND;
    }

    @Override
    protected Item getItemOverlay() {
        var app = getAppearance();
        if (app != null && app.item != null) {
            return app.item;
        }
        return null;
    }

    public Setting<T> getSetting() {
        return this.buttonSetting;
    }

    public T getCurrentValue() {
        return this.currentValue;
    }

    public void set(T e) {
        if (this.currentValue != e) {
            this.currentValue = e;
        }
    }

    @Override
    public List<Component> getTooltipMessage() {

        if (this.buttonSetting == null || this.currentValue == null) {
            return Collections.emptyList();
        }

        var buttonAppearance =
                appearances.get(new EnumPair<>(this.buttonSetting, this.currentValue));
        if (buttonAppearance == null) {
            return Collections.singletonList(ButtonToolTips.NoSuchMessage.text());
        }

        return buttonAppearance.tooltipLines;
    }

    private record EnumPair<T extends Enum<T>>(Setting<T> setting, T value) {

        @Override
        public int hashCode() {
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final EnumPair<?> other = (EnumPair<?>) obj;
            return other.setting == this.setting && other.value == this.value;
        }
    }

    private record ButtonAppearance(@Nullable AGIcon icon, @Nullable Item item, List<Component> tooltipLines) {
    }
}
