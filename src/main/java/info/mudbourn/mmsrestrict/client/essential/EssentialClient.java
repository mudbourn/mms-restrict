package info.mudbourn.mmsrestrict.client.essential;

import gg.essential.config.EssentialConfig;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Client-side upkeep for the Essential restrictions: Leawind camera cleanup and the own nametag setting.
public final class EssentialClient {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");

    private static boolean ownNametagHidden;

    private EssentialClient() {
    }

    public static void init() {
        FabricLoader loader = FabricLoader.getInstance();
        boolean leawind = info.mudbourn.mmsrestrict.essential.EssentialMixinPlugin.LEAWIND_COMPAT && loader.isModLoaded("leawind_third_person") && loader.isModLoaded("perspective_api");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (leawind) {
                LeawindEmoteCamera.tick(client.player != null);
            }
            if (!ownNametagHidden && client.player != null) {
                hideOwnNametag(client);
            }
        });
        LOG.info("Essential client upkeep ready, Leawind camera compat {}", leawind ? "on" : "off");
    }

    private static void hideOwnNametag(Minecraft client) {
        ownNametagHidden = true;
        try {
            EssentialConfig.INSTANCE.getShowOwnNametag().set(Boolean.FALSE);
            EssentialRestrict.blocked("own nametag");
        } catch (Throwable error) {
            LOG.warn("Could not switch off Essential's own nametag", error);
        }
    }
}
