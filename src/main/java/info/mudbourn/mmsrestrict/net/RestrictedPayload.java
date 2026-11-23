package info.mudbourn.mmsrestrict.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

// Server to client: the item ids the hide layer removes from creative tabs and recipe viewers.
public record RestrictedPayload(List<Identifier> items) implements CustomPacketPayload {

    public static final Type<RestrictedPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mms_restrict", "hidden"));
    public static final StreamCodec<ByteBuf, RestrictedPayload> CODEC = Identifier.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .map(RestrictedPayload::new, RestrictedPayload::items);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
