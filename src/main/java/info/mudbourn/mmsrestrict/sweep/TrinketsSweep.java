package info.mudbourn.mmsrestrict.sweep;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.server.level.ServerPlayer;

final class TrinketsSweep {

    private TrinketsSweep() {
    }

    static void sweep(ServerPlayer player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment != null) {
            attachment.getInventories().values().forEach(inventory -> Sweep.sweepContainer(player, "trinket slots", inventory));
        }
    }
}
