package io.github.sapporo1101.appgen.client.button;

import appeng.api.config.Setting;
import appeng.core.network.ServerboundPacket;
import io.github.sapporo1101.appgen.network.packet.AGConfigButtonPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class AGServerSettingToggleButton<T extends Enum<T>> extends AGSettingToggleButton<T> {
    public AGServerSettingToggleButton(Setting<T> setting, T val) {
        super(setting, val, AGServerSettingToggleButton::sendToServer);
    }

    private static <T extends Enum<T>> void sendToServer(AGSettingToggleButton<T> button, boolean backwards) {
        ServerboundPacket message = new AGConfigButtonPacket(button.getSetting(), backwards);
        PacketDistributor.sendToServer(message);
    }
}
