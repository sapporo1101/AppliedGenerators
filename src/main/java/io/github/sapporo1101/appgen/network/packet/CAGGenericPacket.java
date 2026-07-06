package io.github.sapporo1101.appgen.network.packet;

import com.glodblock.github.glodium.network.packet.CGenericPacket;
import io.github.sapporo1101.appgen.AppliedGenerators;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CAGGenericPacket extends CGenericPacket {
    public CAGGenericPacket() {
    }

    public CAGGenericPacket(String name, Object... paras) {
        super(name, paras);
    }

    public @NotNull Identifier id() {
        return AppliedGenerators.id("c_generic");
    }
}
