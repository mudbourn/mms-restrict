package info.mudbourn.mmsrestrict;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.List;

/** Shared restriction state and the queries other mods may call. */
public final class RestrictApi {

    private static volatile RestrictConfig config = new RestrictConfig(List.of(), 2, SweepMode.DELETE, 30);
    private static volatile RestrictedSet set = RestrictedSet.EMPTY;

    private RestrictApi() {
    }

    public static void reloadConfig() {
        config = RestrictConfig.load();
    }

    // Re-reads the config file, keeping the current config when it cannot be parsed.
    public static void tryReloadConfig() throws IOException {
        config = RestrictConfig.reload();
    }

    public static void refresh() {
        set = RestrictedSet.build(config.rules());
    }

    public static void clear() {
        set = RestrictedSet.EMPTY;
    }

    public static RestrictConfig config() {
        return config;
    }

    public static RestrictedSet set() {
        return set;
    }

    public static List<Rule> rules(Layer layer, ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        return set.rules(layer, BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isRestricted(Layer layer, ItemStack stack) {
        return !rules(layer, stack).isEmpty();
    }

    /** Whether the stack is restricted on the layer; counts one hit per matching rule on the stat. */
    public static boolean deny(Layer layer, ItemStack stack, Stat stat) {
        List<Rule> hits = rules(layer, stack);
        for (Rule rule : hits) {
            rule.add(stat, 1);
        }
        return !hits.isEmpty();
    }

    public static boolean isBypassed(Player player) {
        PermissionLevel level = PermissionLevel.byId(config.bypassLevel());
        return player.permissions().hasPermission(new Permission.HasCommandLevel(level));
    }

    /** Whether a worldgen rule denies the structure in the level. */
    public static boolean isStructureDenied(ServerLevel level, Identifier structure) {
        return isStructureDenied(level.dimension(), structure);
    }

    public static boolean isStructureDenied(ResourceKey<Level> dimension, Identifier structure) {
        boolean denied = false;
        for (Rule rule : config.rules()) {
            if (rule.has(Layer.WORLDGEN) && rule.inDimension(dimension.identifier()) && rule.matchesId(structure)) {
                rule.add(Stat.STRUCTURES, 1);
                denied = true;
            }
        }
        return denied;
    }
}
