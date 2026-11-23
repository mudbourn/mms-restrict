package info.mudbourn.mmsrestrict.worldgen;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.Rule;
import info.mudbourn.mmsrestrict.Stat;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class Worldgen {

    private Worldgen() {
    }

    // Registers feature and spawn removal for the worldgen rules loaded at startup.
    public static void register(List<Rule> allRules) {
        List<Rule> rules = allRules.stream().filter(rule -> rule.has(Layer.WORLDGEN)).toList();
        if (rules.isEmpty()) {
            return;
        }
        BiomeModifications.create(Identifier.fromNamespaceAndPath("mms_restrict", "restrict"))
            .add(ModificationPhase.REMOVALS, context -> true, (selection, modification) -> {
                for (Rule rule : rules) {
                    if (appliesTo(rule, selection)) {
                        removeFeatures(rule, modification);
                        removeSpawns(rule, modification);
                    }
                }
            });
    }

    private static boolean appliesTo(Rule rule, BiomeSelectionContext selection) {
        if (rule.dimensions().isEmpty()) {
            return true;
        }
        for (Identifier dimension : rule.dimensions()) {
            if (selection.canGenerateIn(ResourceKey.create(Registries.LEVEL_STEM, dimension))) {
                return true;
            }
        }
        return false;
    }

    private static void removeFeatures(Rule rule, BiomeModificationContext modification) {
        BiomeModificationContext.GenerationSettingsContext generation = modification.getGenerationSettings();
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            for (Holder<PlacedFeature> holder : List.copyOf(generation.getFeatures(step))) {
                holder.unwrapKey().ifPresent(key -> {
                    if (rule.matchesId(key.identifier()) && generation.removeFeature(step, key)) {
                        rule.add(Stat.FEATURES, 1);
                    }
                });
            }
        }
    }

    private static void removeSpawns(Rule rule, BiomeModificationContext modification) {
        AtomicInteger removed = new AtomicInteger();
        modification.getMobSpawnSettings().removeSpawns((category, data) -> {
            boolean match = rule.matchesId(BuiltInRegistries.ENTITY_TYPE.getKey(data.type()));
            if (match) {
                removed.incrementAndGet();
            }
            return match;
        });
        rule.add(Stat.SPAWNS, removed.get());
    }
}
