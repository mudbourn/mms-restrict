package info.mudbourn.mmsrestrict.obtain;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.Rule;
import info.mudbourn.mmsrestrict.Stat;
import info.mudbourn.mmsrestrict.mixin.recipes.RecipeMapInvoker;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RecipeFilter {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final Set<RecipeType<?>> WARNED = ConcurrentHashMap.newKeySet();

    private RecipeFilter() {
    }

    // Rebuilds the map without recipes whose result is restricted.
    public static RecipeMap filter(RecipeMap map, HolderLookup.Provider provider) {
        if (RestrictApi.set().ids(Layer.OBTAIN).isEmpty() && RestrictApi.config().rules().stream().noneMatch(Rule::hasRecipes)) {
            return map;
        }
        ContextMap context = context(provider);
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = ImmutableMap.builder();
        for (RecipeHolder<?> holder : map.values()) {
            List<Rule> hits = hits(holder, context);
            if (hits.isEmpty()) {
                byType.put(holder.value().getType(), holder);
                byKey.put(holder.id(), holder);
            } else {
                hits.forEach(rule -> rule.add(Stat.RECIPES, 1));
            }
        }
        ImmutableMap<ResourceKey<Recipe<?>>, RecipeHolder<?>> kept = byKey.build();
        RecipeMap filtered = RecipeMapInvoker.create(byType.build(), kept);
        copyAddedMaps(map, filtered, kept.keySet());
        return filtered;
    }

    // Other mods add fields to RecipeMap that only its factory fills, such as Fabric's bySyncedSerializer, so carry them over without the removed recipes.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyAddedMaps(RecipeMap from, RecipeMap to, Set<ResourceKey<Recipe<?>>> kept) {
        for (Field field : RecipeMap.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                if (field.get(to) != null || !(field.get(from) instanceof Map<?, ?> source)) continue;
                Map copy = new IdentityHashMap<>();
                source.forEach((key, value) -> copy.put(key, value instanceof List<?> list
                        ? new ArrayList<>(list.stream().filter(entry -> !(entry instanceof RecipeHolder<?> holder) || kept.contains(holder.id())).toList())
                        : value));
                field.set(to, copy);
            } catch (ReflectiveOperationException | RuntimeException e) {
                LOG.warn("Couldn't copy RecipeMap field {}", field.getName(), e);
            }
        }
    }

    public static Collection<RecipeHolder<?>> filterLearnable(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider provider) {
        ContextMap context = context(provider);
        return recipes.stream().filter(holder -> hits(holder, context).isEmpty()).toList();
    }

    private static ContextMap context(HolderLookup.Provider provider) {
        RegistryAccess access = provider instanceof RegistryAccess registries ? registries : RegistryAccess.EMPTY;
        return ContextMap.builder().set(SlotDisplayContext.REGISTRIES, access).build();
    }

    private static List<Rule> hits(RecipeHolder<?> holder, ContextMap context) {
        List<Rule> hits = new ArrayList<>();
        for (Rule rule : RestrictApi.config().rules()) {
            if (rule.matchesRecipe(holder.id().identifier())) {
                hits.add(rule);
            }
        }
        RecipeType<?> type = holder.value().getType();
        try {
            List<RecipeDisplay> displays = holder.value().display();
            if (displays.isEmpty()) {
                warnOnce(type, "yields no displays", null);
            }
            for (RecipeDisplay display : displays) {
                for (ItemStack stack : display.result().resolveForStacks(context)) {
                    hits.addAll(RestrictApi.rules(Layer.OBTAIN, stack));
                }
            }
        } catch (RuntimeException e) {
            warnOnce(type, "threw while resolving displays", e);
            return List.of();
        }
        return hits.stream().distinct().toList();
    }

    private static void warnOnce(RecipeType<?> type, String problem, RuntimeException cause) {
        if (WARNED.add(type)) {
            LOG.warn("Recipe type {} {}, its recipes are not filtered", type, problem, cause);
        }
    }
}
