package info.mudbourn.mmsrestrict.use;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.Rule;
import info.mudbourn.mmsrestrict.Stat;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UseGuard {

    private static final long NOTICE_INTERVAL_NANOS = 3_000_000_000L;
    private static final Map<UUID, Long> LAST_NOTICE = new ConcurrentHashMap<>();

    private UseGuard() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) ->
            blocked(player, level, player.getItemInHand(hand)) ? InteractionResult.FAIL : InteractionResult.PASS);
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
            blocked(player, level, player.getItemInHand(hand)) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
            blocked(player, level, player.getItemInHand(hand)) ? InteractionResult.FAIL : InteractionResult.PASS);
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) ->
            !blocked(player, level, player.getMainHandItem()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_NOTICE.remove(handler.getPlayer().getUUID()));
    }

    private static boolean blocked(Player player, Level level, ItemStack stack) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        List<Rule> rules = RestrictApi.rules(Layer.USE, stack);
        if (rules.isEmpty() || RestrictApi.isBypassed(player)) {
            return false;
        }
        rules.forEach(rule -> rule.add(Stat.USES, 1));
        notice(serverPlayer, rules.getFirst().reason());
        return true;
    }

    private static void notice(ServerPlayer player, String reason) {
        long now = System.nanoTime();
        Long last = LAST_NOTICE.get(player.getUUID());
        if (last == null || now - last >= NOTICE_INTERVAL_NANOS) {
            LAST_NOTICE.put(player.getUUID(), now);
            player.sendSystemMessage(Component.literal("You can't use this: " + reason), true);
        }
    }
}
