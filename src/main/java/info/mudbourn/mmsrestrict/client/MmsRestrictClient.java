package info.mudbourn.mmsrestrict.client;

import info.mudbourn.mmsrestrict.net.RestrictedPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

public class MmsRestrictClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RestrictedPayload.TYPE, ClientRestricted::accept);
        ClientPlayConnectionEvents.INIT.register((handler, client) -> ClientRestricted.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientRestricted.reset());
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) -> {
            output.getDisplayStacks().removeIf(ClientRestricted::isHidden);
            output.getSearchTabStacks().removeIf(ClientRestricted::isHidden);
        });
    }
}
