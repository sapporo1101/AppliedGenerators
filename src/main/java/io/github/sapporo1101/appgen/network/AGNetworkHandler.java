package io.github.sapporo1101.appgen.network;

import appeng.core.network.ServerboundPacket;
import com.glodblock.github.glodium.network.NetworkHandler;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.network.packet.AAEConfigButtonPacket;
import io.github.sapporo1101.appgen.network.packet.CAGGenericPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public class AGNetworkHandler extends NetworkHandler {

    public static final AGNetworkHandler INSTANCE = new AGNetworkHandler();

    public AGNetworkHandler() {
        super(AppliedGenerators.MODID);
        registerPacket(CAGGenericPacket::new);
    }

    @Override
    public void onRegister(RegisterPayloadHandlersEvent event) {
        super.onRegister(event);

        serverbound(this.registrar, AAEConfigButtonPacket.TYPE, AAEConfigButtonPacket.STREAM_CODEC);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends ServerboundPacket> void serverbound(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<@NotNull T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec
    ) {
        registrar.playToServer(type, codec, ServerboundPacket::handleOnServer);
    }
}
