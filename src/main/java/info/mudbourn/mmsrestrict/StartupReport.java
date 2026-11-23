package info.mudbourn.mmsrestrict;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

// Logs one line per rule with match counts, and a warning for every match that resolves to nothing.
public final class StartupReport {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");

    private StartupReport() {
    }

    public static void log(RegistryAccess access) {
        RestrictConfig config = RestrictApi.config();
        RestrictedSet set = RestrictApi.set();
        Set<Identifier> structures = access.lookupOrThrow(Registries.STRUCTURE).keySet();
        Set<Identifier> features = access.lookupOrThrow(Registries.PLACED_FEATURE).keySet();
        Set<Identifier> entities = BuiltInRegistries.ENTITY_TYPE.keySet();
        Set<Identifier> items = BuiltInRegistries.ITEM.keySet();

        LOG.info("Loaded {} rules, sweep every {}s, bypass at permission level {}", config.rules().size(), config.sweepIntervalSeconds(), config.bypassLevel());
        for (Rule rule : config.rules()) {
            boolean worldgen = rule.has(Layer.WORLDGEN);
            LOG.info(
                "Rule '{}' layers={} items={} structures={} features={} entity_types={} ({})",
                rule.name(),
                rule.layers().stream().map(Layer::label).sorted().collect(Collectors.joining(",")),
                set.itemCount(rule),
                worldgen ? count(rule, structures) : 0,
                worldgen ? count(rule, features) : 0,
                worldgen ? count(rule, entities) : 0,
                rule.reason()
            );
            for (Match match : rule.matches()) {
                if (!resolves(rule, match, items, structures, features, entities)) {
                    LOG.warn("Rule '{}': '{}' does not resolve to anything", rule.name(), match.raw());
                }
            }
        }
    }

    private static long count(Rule rule, Set<Identifier> ids) {
        return ids.stream().filter(rule::matchesId).count();
    }

    private static boolean resolves(Rule rule, Match match, Set<Identifier> items, Set<Identifier> structures, Set<Identifier> features, Set<Identifier> entities) {
        if (match.kind() == Match.Kind.TAG) {
            return BuiltInRegistries.ITEM.listElements().anyMatch(holder -> holder.is(match.tag()));
        }
        boolean itemLayer = rule.hasItemLayer();
        boolean worldgen = rule.has(Layer.WORLDGEN);
        return (itemLayer && anyMatch(match, items))
            || (worldgen && (anyMatch(match, structures) || anyMatch(match, features) || anyMatch(match, entities)));
    }

    private static boolean anyMatch(Match match, Set<Identifier> ids) {
        return match.kind() == Match.Kind.ID ? ids.contains(match.id()) : ids.stream().anyMatch(match::matchesId);
    }
}
