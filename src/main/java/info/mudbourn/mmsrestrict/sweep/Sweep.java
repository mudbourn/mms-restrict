package info.mudbourn.mmsrestrict.sweep;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.Rule;
import info.mudbourn.mmsrestrict.Stat;
import info.mudbourn.mmsrestrict.SweepMode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Sweep {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final boolean TRINKETS = FabricLoader.getInstance().isModLoaded("trinkets");

    private static int ticks;

    private Sweep() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sweep(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++ticks >= RestrictApi.config().sweepIntervalSeconds() * 20) {
                ticks = 0;
                server.getPlayerList().getPlayers().forEach(Sweep::sweep);
            }
        });
    }

    // Sweeps the inventory, ender chest, craft grid, cursor stack and trinket slots of one player.
    public static void sweep(ServerPlayer player) {
        if (RestrictApi.set().ids(Layer.SWEEP).isEmpty() || RestrictApi.isBypassed(player)) {
            return;
        }
        sweepContainer(player, "inventory", player.getInventory());
        sweepContainer(player, "ender chest", player.getEnderChestInventory());
        sweepContainer(player, "craft grid", player.inventoryMenu.getCraftSlots());
        ItemStack carried = player.containerMenu.getCarried();
        if (sweepStack(player, "cursor", carried) == Result.REMOVE) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        if (TRINKETS) {
            TrinketsSweep.sweep(player);
        }
    }

    static void sweepContainer(ServerPlayer player, String where, Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (sweepStack(player, where, container.getItem(slot)) == Result.REMOVE) {
                container.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private enum Result {
        KEEP,
        CHANGED,
        REMOVE
    }

    // Sweeps one stack and the stacks nested in its container and bundle components.
    private static Result sweepStack(ServerPlayer player, String where, ItemStack stack) {
        if (stack.isEmpty()) {
            return Result.KEEP;
        }
        List<Rule> rules = RestrictApi.rules(Layer.SWEEP, stack);
        if (!rules.isEmpty()) {
            SweepMode mode = mode(rules.getFirst());
            rules.forEach(rule -> rule.add(Stat.SWEPT, stack.getCount()));
            LOG.info(
                "Sweep ({}): {} x{} from {} of {} (rule {})",
                mode == SweepMode.DELETE ? "delete" : "log only",
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                stack.getCount(),
                where,
                player.getPlainTextName(),
                rules.getFirst().name()
            );
            return mode == SweepMode.DELETE ? Result.REMOVE : Result.KEEP;
        }
        boolean changed = false;
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            List<ItemStack> items = contents.itemCopies().collect(Collectors.toCollection(ArrayList::new));
            if (sweepNested(player, where, items)) {
                stack.set(DataComponents.CONTAINER, contents.copyWithContents(items.stream()));
                changed = true;
            }
        }
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            List<ItemStack> items = bundle.itemCopies().collect(Collectors.toCollection(ArrayList::new));
            if (sweepNested(player, where, items)) {
                stack.set(DataComponents.BUNDLE_CONTENTS, bundle.copyWithContents(items.stream().filter(item -> !item.isEmpty())));
                changed = true;
            }
        }
        return changed ? Result.CHANGED : Result.KEEP;
    }

    // Sweeps each nested stack, replacing removed ones with empty; true when anything changed.
    private static boolean sweepNested(ServerPlayer player, String where, List<ItemStack> items) {
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            Result result = sweepStack(player, where + " (nested)", items.get(i));
            if (result == Result.REMOVE) {
                items.set(i, ItemStack.EMPTY);
            }
            changed |= result != Result.KEEP;
        }
        return changed;
    }

    private static SweepMode mode(Rule rule) {
        return rule.sweepMode() != null ? rule.sweepMode() : RestrictApi.config().sweepMode();
    }
}
