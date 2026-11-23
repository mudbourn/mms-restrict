package info.mudbourn.mmsrestrict;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Frozen item ids per layer, each with the rules that put it there.
public final class RestrictedSet {

    public static final RestrictedSet EMPTY = new RestrictedSet(new EnumMap<>(Layer.class), new IdentityHashMap<>());

    private final Map<Layer, Map<Identifier, List<Rule>>> items;
    private final Map<Rule, Integer> itemCounts;

    private RestrictedSet(Map<Layer, Map<Identifier, List<Rule>>> items, Map<Rule, Integer> itemCounts) {
        this.items = items;
        this.itemCounts = itemCounts;
    }

    // Resolves every item rule against the item registry and its bound tags.
    public static RestrictedSet build(List<Rule> rules) {
        Map<Layer, Map<Identifier, List<Rule>>> building = new EnumMap<>(Layer.class);
        Map<Rule, Integer> counts = new IdentityHashMap<>();
        List<Rule> itemRules = rules.stream().filter(Rule::hasItemLayer).toList();

        for (Holder.Reference<Item> holder : (Iterable<Holder.Reference<Item>>) BuiltInRegistries.ITEM.listElements()::iterator) {
            Identifier id = holder.key().identifier();
            for (Rule rule : itemRules) {
                if (!matches(rule, id, holder)) {
                    continue;
                }
                counts.merge(rule, 1, Integer::sum);
                for (Layer layer : rule.layers()) {
                    if (layer.isItemLayer()) {
                        building.computeIfAbsent(layer, key -> new HashMap<>())
                            .computeIfAbsent(id, key -> new ArrayList<>())
                            .add(rule);
                    }
                }
            }
        }

        Map<Layer, Map<Identifier, List<Rule>>> frozen = new EnumMap<>(Layer.class);
        building.forEach((layer, map) -> {
            Map<Identifier, List<Rule>> copy = new HashMap<>();
            map.forEach((id, list) -> copy.put(id, List.copyOf(list)));
            frozen.put(layer, Map.copyOf(copy));
        });
        return new RestrictedSet(frozen, counts);
    }

    private static boolean matches(Rule rule, Identifier id, Holder<Item> holder) {
        for (Match match : rule.matches()) {
            if (match.kind() == Match.Kind.TAG ? holder.is(match.tag()) : match.matchesId(id)) {
                return true;
            }
        }
        return false;
    }

    public List<Rule> rules(Layer layer, Identifier id) {
        Map<Identifier, List<Rule>> map = items.get(layer);
        return map == null ? List.of() : map.getOrDefault(id, List.of());
    }

    public Set<Identifier> ids(Layer layer) {
        Map<Identifier, List<Rule>> map = items.get(layer);
        return map == null ? Set.of() : map.keySet();
    }

    public int itemCount(Rule rule) {
        return itemCounts.getOrDefault(rule, 0);
    }
}
