package info.mudbourn.mmsrestrict.essential;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Decides which Essential features stay on; free of Minecraft classes so it can run before the game loads.
public final class EssentialRestrict {

    public static final String MOD_ID = "essential";
    public static final String MIXIN_CONFIG = "mms_restrict.essential.client.mixins.json";

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final String ESSENTIAL_PACKAGE = "gg.essential.";
    private static final String EMOTE_WHEEL_SCREEN = "gg.essential.gui.emotes.EmoteWheel";
    private static final String WARDROBE_SCREEN = "gg.essential.gui.wardrobe.Wardrobe";
    private static final Set<String> BLOCKED_SUBSCRIBERS = Set.of(
        "gg.essential.handlers.PauseMenuDisplay",
        "gg.essential.handlers.OptionsScreenOverlay",
        "gg.essential.handlers.ReAuthChecker",
        "gg.essential.handlers.ZoomHandler",
        "gg.essential.handlers.discord.DiscordIntegration"
    );

    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();
    private static volatile EssentialSection section;

    private EssentialRestrict() {
    }

    public static EssentialSection section() {
        EssentialSection current = section;
        if (current == null) {
            current = read();
            section = current;
        }
        return current;
    }

    public static boolean isActive() {
        return section().enabled() && FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    // Keybinds are matched by Essential's own key id.
    public static boolean allowsKey(String keyId) {
        EssentialSection allow = section();
        if (keyId == null) {
            return false;
        }
        if (keyId.equals("EMOTE_WHEEL")) {
            return allow.allows(EssentialSection.EMOTE_WHEEL);
        }
        if (keyId.equals("COSMETIC_STUDIO")) {
            return allow.allows(EssentialSection.EMOTE_SELECTION);
        }
        if (keyId.startsWith("EMOTE_SLOT_")) {
            return allow.allows(EssentialSection.EMOTE_SLOTS);
        }
        return false;
    }

    // Anything outside the gg.essential package is not ours to judge.
    public static boolean allowsScreen(String className) {
        if (!className.startsWith(ESSENTIAL_PACKAGE)) {
            return true;
        }
        return isAllowedEssentialScreen(className);
    }

    public static boolean isAllowedEssentialScreen(String className) {
        EssentialSection allow = section();
        if (className.equals(EMOTE_WHEEL_SCREEN)) {
            return allow.allows(EssentialSection.EMOTE_WHEEL);
        }
        return className.equals(WARDROBE_SCREEN) && allow.allows(EssentialSection.EMOTE_SELECTION);
    }

    public static boolean blocksSubscriber(Object listener) {
        return listener != null && BLOCKED_SUBSCRIBERS.contains(listener.getClass().getName());
    }

    // Logs the first time a feature is blocked so a silent target is easy to spot.
    public static void blocked(String feature) {
        if (LOGGED.add(feature)) {
            LOG.info("Essential feature blocked: {}", feature);
        }
    }

    private static EssentialSection read() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("mms-restrict.json");
        try {
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    return parse(reader);
                }
            }
            try (Reader reader = new InputStreamReader(
                EssentialRestrict.class.getResourceAsStream("/mms_restrict/default_config.json"),
                StandardCharsets.UTF_8
            )) {
                return parse(reader);
            }
        } catch (IOException | RuntimeException e) {
            LOG.error("Could not read the essential section of {}, Essential restrictions stay off", path, e);
            return EssentialSection.OFF;
        }
    }

    private static EssentialSection parse(Reader reader) {
        JsonReader json = new JsonReader(reader);
        json.setStrictness(Strictness.LENIENT);
        JsonObject root = JsonParser.parseReader(json).getAsJsonObject();
        return EssentialSection.parse(root);
    }
}
