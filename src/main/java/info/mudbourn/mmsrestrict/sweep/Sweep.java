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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Sweep {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final boolean TRINKETS = FabricLoader.getInstance().isModLoaded("trinkets");

    private static final Set<String> UNRESOLVED = ConcurrentHashMap.newKeySet();

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
        Result carried = sweepStack(player, "cursor", player.containerMenu.getCarried());
        if (carried.kind() == Kind.REMOVE) {
            player.containerMenu.setCarried(carried.replacement());
        }
        if (TRINKETS) {
            TrinketsSweep.sweep(player);
        }
    }

    static void sweepContainer(ServerPlayer player, String where, Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            Result result = sweepStack(player, where, container.getItem(slot));
            if (result.kind() == Kind.REMOVE) {
                container.setItem(slot, result.replacement());
            }
        }
    }

    private enum Kind {
        KEEP,
        CHANGED,
        REMOVE
    }

    private record Result(Kind kind, ItemStack replacement) {

        static final Result KEEP = new Result(Kind.KEEP, ItemStack.EMPTY);
        static final Result CHANGED = new Result(Kind.CHANGED, ItemStack.EMPTY);
    }

    // Sweeps one stack and the stacks nested in its container and bundle components.
    private static Result sweepStack(ServerPlayer player, String where, ItemStack stack) {
        if (stack.isEmpty()) {
            return Result.KEEP;
        }
        List<Rule> rules = RestrictApi.rules(Layer.SWEEP, stack);
        if (!rules.isEmpty()) {
            return sweepRestricted(player, where, stack, rules);
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

    // Logs the sweep of a restricted stack and, in delete mode, swaps it for its refund when the rule has one.
    private static Result sweepRestricted(ServerPlayer player, String where, ItemStack stack, List<Rule> rules) {
        Rule first = rules.getFirst();
        SweepMode mode = mode(first);
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        rules.forEach(rule -> rule.add(Stat.SWEPT, stack.getCount()));

        Rule refundRule = rules.stream().filter(rule -> rule.refunds().containsKey(id)).findFirst().orElse(null);
        Item refundItem = refundRule == null ? null : resolveRefund(refundRule, id);
        int refundTotal = refundItem == null ? 0 : stack.getCount() * refundRule.refunds().get(id).count();
        String refundText = refundItem == null ? "" : ", refund " + BuiltInRegistries.ITEM.getKey(refundItem) + " x" + refundTotal;

        LOG.info(
            "Sweep ({}): {} x{} from {} of {} (rule {}{})",
            mode == SweepMode.DELETE ? "delete" : "log only",
            id,
            stack.getCount(),
            where,
            player.getPlainTextName(),
            first.name(),
            refundText
        );
        if (mode != SweepMode.DELETE) {
            return Result.KEEP;
        }
        if (refundItem == null) {
            return new Result(Kind.REMOVE, ItemStack.EMPTY);
        }
        refundRule.add(Stat.REFUNDS, refundTotal);
        return new Result(Kind.REMOVE, refund(player, refundItem, refundTotal));
    }

    private static Item resolveRefund(Rule rule, Identifier swept) {
        Identifier target = rule.refunds().get(swept).item();
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(target);
        if (item.isEmpty() && UNRESOLVED.add(rule.name() + "/" + swept)) {
            LOG.warn("Rule '{}': refund '{}' for '{}' is not an item, deleting without refund", rule.name(), target, swept);
        }
        return item.orElse(null);
    }

    // Returns the stack for the swept slot; whatever exceeds one stack goes to the inventory, then the ground.
    private static ItemStack refund(ServerPlayer player, Item item, int total) {
        ItemStack slotStack = new ItemStack(item, 1);
        int max = slotStack.getMaxStackSize();
        slotStack.setCount(Math.min(total, max));
        int left = total - slotStack.getCount();
        while (left > 0) {
            ItemStack extra = new ItemStack(item, Math.min(left, max));
            left -= extra.getCount();
            player.getInventory().add(extra);
            if (!extra.isEmpty()) {
                player.spawnAtLocation(player.level(), extra);
            }
        }
        return slotStack;
    }

    // Sweeps each nested stack, replacing removed ones with their refund or empty; true when anything changed.
    private static boolean sweepNested(ServerPlayer player, String where, List<ItemStack> items) {
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            Result result = sweepStack(player, where + " (nested)", items.get(i));
            if (result.kind() == Kind.REMOVE) {
                items.set(i, result.replacement());
            }
            changed |= result.kind() != Kind.KEEP;
        }
        return changed;
    }

    private static SweepMode mode(Rule rule) {
        return rule.sweepMode() != null ? rule.sweepMode() : RestrictApi.config().sweepMode();
    }
}
