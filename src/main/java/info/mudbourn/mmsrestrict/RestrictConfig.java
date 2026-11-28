package info.mudbourn.mmsrestrict;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import info.mudbourn.mmsrestrict.essential.EssentialSection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// The parsed config/mms-restrict.json.
public record RestrictConfig(
    List<Rule> rules,
    int bypassLevel,
    SweepMode sweepMode,
    int sweepIntervalSeconds,
    EssentialSection essential
) {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final String DEFAULT_RESOURCE = "/mms_restrict/default_config.json";
    private static final Set<Layer> DEFAULT_LAYERS = EnumSet.of(Layer.OBTAIN, Layer.USE, Layer.SWEEP, Layer.HIDE);

    // Reads the config file, writing the bundled default first when it is missing.
    public static RestrictConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("mms-restrict.json");
        try {
            if (!Files.exists(path)) {
                Files.write(path, bundled().getBytes(StandardCharsets.UTF_8));
                LOG.info("Wrote default config to {}", path);
            }
            try (Reader reader = Files.newBufferedReader(path)) {
                return parse(reader);
            }
        } catch (IOException | RuntimeException e) {
            LOG.error("Could not read {}, using the bundled default", path, e);
            return parse(new StringReader(bundled()));
        }
    }

    // Re-reads the config file; a failure throws and leaves the caller's config untouched.
    public static RestrictConfig reload() throws IOException {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("mms-restrict.json");
        try (Reader reader = Files.newBufferedReader(path)) {
            return parse(reader);
        } catch (RuntimeException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static String bundled() {
        try (InputStream in = RestrictConfig.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Missing bundled default config", e);
        }
    }

    private static RestrictConfig parse(Reader reader) {
        JsonReader json = new JsonReader(reader);
        json.setStrictness(Strictness.LENIENT);
        JsonObject root = JsonParser.parseReader(json).getAsJsonObject();

        JsonObject bypass = GsonHelper.getAsJsonObject(root, "bypass", new JsonObject());
        int level = Math.clamp(GsonHelper.getAsInt(bypass, "permission_level", 2), 0, 4);
        SweepMode mode = parseMode(GsonHelper.getAsString(root, "sweep_mode", "delete"), SweepMode.DELETE);
        int interval = Math.max(1, GsonHelper.getAsInt(root, "sweep_interval_seconds", 30));

        List<Rule> rules = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(root, "rules", new JsonArray());
        for (int i = 0; i < array.size(); i++) {
            rules.add(parseRule(array.get(i).getAsJsonObject(), i + 1));
        }
        return new RestrictConfig(List.copyOf(rules), level, mode, interval, EssentialSection.parse(root));
    }

    private static Rule parseRule(JsonObject json, int index) {
        String name = GsonHelper.getAsString(json, "name", "rule-" + index);

        List<Match> matches = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "match", new JsonArray())) {
            Match match = Match.parse(element.getAsString());
            if (match == null) {
                LOG.warn("Rule '{}': invalid match '{}'", name, element.getAsString());
            } else {
                matches.add(match);
            }
        }

        // Recipe ids removed whatever they make; '*' matches any run of characters.
        List<java.util.regex.Pattern> recipes = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "recipes", new JsonArray())) {
            String raw = element.getAsString();
            recipes.add(java.util.regex.Pattern.compile(java.util.Arrays.stream(raw.split("\\*", -1))
                .map(java.util.regex.Pattern::quote).collect(java.util.stream.Collectors.joining(".*"))));
        }

        Set<Layer> layers = EnumSet.noneOf(Layer.class);
        if (json.has("layers")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "layers")) {
                Layer layer = Layer.parse(element.getAsString());
                if (layer == null) {
                    LOG.warn("Rule '{}': unknown layer '{}'", name, element.getAsString());
                } else {
                    layers.add(layer);
                }
            }
        } else {
            layers.addAll(DEFAULT_LAYERS);
        }

        Set<Identifier> dimensions = new HashSet<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "dimensions", new JsonArray())) {
            Identifier dimension = Identifier.tryParse(element.getAsString());
            if (dimension == null) {
                LOG.warn("Rule '{}': invalid dimension '{}'", name, element.getAsString());
            } else {
                dimensions.add(dimension);
            }
        }

        SweepMode sweepMode = json.has("sweep_mode") ? parseMode(GsonHelper.getAsString(json, "sweep_mode"), null) : null;
        return new Rule(name, matches, recipes, layers, dimensions, GsonHelper.getAsString(json, "reason", "restricted"), sweepMode, parseRefunds(name, json));
    }

    private static Map<Identifier, Refund> parseRefunds(String ruleName, JsonObject json) {
        Map<Identifier, Refund> refunds = new HashMap<>();
        JsonObject object = GsonHelper.getAsJsonObject(json, "refunds", new JsonObject());
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Identifier swept = Identifier.tryParse(entry.getKey());
            JsonObject body = entry.getValue().isJsonObject() ? entry.getValue().getAsJsonObject() : new JsonObject();
            Identifier item = Identifier.tryParse(GsonHelper.getAsString(body, "item", ""));
            if (swept == null || item == null) {
                LOG.warn("Rule '{}': invalid refund '{}'", ruleName, entry.getKey());
            } else {
                refunds.put(swept, new Refund(item, Math.max(1, GsonHelper.getAsInt(body, "count", 1))));
            }
        }
        return refunds;
    }

    private static SweepMode parseMode(String name, SweepMode fallback) {
        SweepMode mode = SweepMode.parse(name);
        if (mode == null) {
            LOG.warn("Unknown sweep_mode '{}'", name);
            return fallback;
        }
        return mode;
    }
}
