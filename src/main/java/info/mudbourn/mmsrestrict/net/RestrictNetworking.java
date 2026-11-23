package info.mudbourn.mmsrestrict.net;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class RestrictNetworking {

    private RestrictNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(RestrictedPayload.TYPE, RestrictedPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.getPlayer()));
    }

    public static void broadcast(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(RestrictNetworking::send);
    }

    private static void send(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, RestrictedPayload.TYPE)) {
            List<Identifier> ids = List.copyOf(RestrictApi.set().ids(Layer.HIDE));
            ServerPlayNetworking.send(player, new RestrictedPayload(ids));
        }
    }
}
