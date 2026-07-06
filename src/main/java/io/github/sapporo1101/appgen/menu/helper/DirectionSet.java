package io.github.sapporo1101.appgen.menu.helper;

import appeng.menu.guisync.PacketWritable;
import com.glodblock.github.glodium.util.GlodCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record DirectionSet(Set<Direction> backend) implements PacketWritable {

    public static final Codec<DirectionSet> CODEC = Codec
            .list(Direction.CODEC)
            .xmap(DirectionSet::new, DirectionSet::asList);
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull DirectionSet> STREAM_CODEC = GlodCodecs
            .list(Direction.STREAM_CODEC)
            .map(DirectionSet::new, DirectionSet::asList);

    public DirectionSet() {
        this(EnumSet.allOf(Direction.class));
    }

    public DirectionSet(Collection<Direction> init) {
        this(init.isEmpty() ? EnumSet.noneOf(Direction.class) : EnumSet.copyOf(init));
    }

    public DirectionSet(RegistryFriendlyByteBuf buf) {
        this(fromBytes(buf));
    }

    public void reload(Collection<Direction> sides) {
        this.backend.clear();
        this.backend.addAll(sides);
    }

    public Set<Direction> asSet() {
        return this.backend;
    }

    public List<Direction> asList() {
        return new ArrayList<>(this.backend);
    }

    public void load(ValueInput input, String name) {
        input.childrenList(name).ifPresent(list -> {
            this.backend.clear();
            for (var side : list) {
                side.getString("side").ifPresent(s -> this.backend.add(Direction.byName(s)));
            }
        });
    }

    public void save(ValueOutput output, String name) {
        var list = output.childrenList(name);
        for (var side : this.backend) {
            list.addChild().putString("side", side.getName());
        }
    }

    private static List<Direction> fromBytes(RegistryFriendlyByteBuf buf) {
        List<Direction> fields = new ArrayList<>();
        int size = buf.readByte();
        while (size > 0) {
            size--;
            fields.add(Direction.from3DDataValue(buf.readByte()));
        }
        return fields;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        buf.writeByte(this.backend.size());
        for (var side : this.backend) {
            buf.writeByte(side.get3DDataValue());
        }
    }

    public void clear() {
        this.backend.clear();
    }

}