package io.github.sapporo1101.appgen.client.button;

import appeng.api.config.Setting;
import appeng.core.network.ServerboundPacket;
import io.github.sapporo1101.appgen.network.packet.AAEConfigButtonPacket;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class AAEServerSettingToggleButton<T extends Enum<T>> extends AAESettingToggleButton<T> {
    public AAEServerSettingToggleButton(Setting<T> setting, T val) {
        super(setting, val, AAEServerSettingToggleButton::sendToServer);
    }

    private static <T extends Enum<T>> void sendToServer(AAESettingToggleButton<T> button, boolean backwards) {
        ServerboundPacket message = new AAEConfigButtonPacket(button.getSetting(), backwards);
        ClientPacketDistributor.sendToServer(message);
    }
}
