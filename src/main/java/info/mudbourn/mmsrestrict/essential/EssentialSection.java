package info.mudbourn.mmsrestrict.essential;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// The "essential" section of config/mms-restrict.json: an allowlist over the Essential mod.
public record EssentialSection(boolean enabled, Set<String> allow) {

    public static final String EMOTE_WHEEL = "emote_wheel";
    public static final String EMOTE_SELECTION = "emote_selection";
    public static final String EMOTE_SLOTS = "emote_slots";

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final Set<String> KNOWN = Set.of(EMOTE_WHEEL, EMOTE_SELECTION, EMOTE_SLOTS);
    private static final List<String> DEFAULT_ALLOW = List.of(EMOTE_WHEEL, EMOTE_SELECTION);

    public static final EssentialSection OFF = new EssentialSection(false, Set.of());

    public boolean allows(String feature) {
        return allow.contains(feature);
    }

    // A missing section means the Essential restrictions are off.
    public static EssentialSection parse(JsonObject root) {
        JsonElement element = root.get("essential");
        if (element == null || !element.isJsonObject()) {
            return OFF;
        }
        JsonObject json = element.getAsJsonObject();
        boolean enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();

        Set<String> allow = new HashSet<>();
        if (json.has("allow")) {
            for (JsonElement entry : json.getAsJsonArray("allow")) {
                String name = entry.getAsString().toLowerCase(Locale.ROOT);
                if (KNOWN.contains(name)) {
                    allow.add(name);
                } else {
                    LOG.warn("Unknown essential.allow entry '{}'", name);
                }
            }
        } else {
            allow.addAll(DEFAULT_ALLOW);
        }
        return new EssentialSection(enabled, Set.copyOf(allow));
    }
}
