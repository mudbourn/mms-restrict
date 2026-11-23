package info.mudbourn.mmsrestrict.obtain;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.Stat;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.trading.MerchantOffers;

public final class Obtain {

    private Obtain() {
    }

    // Strips restricted stacks from every loot table drop.
    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((table, context, drops) ->
            drops.removeIf(stack -> RestrictApi.deny(Layer.OBTAIN, stack, Stat.LOOT)));
    }

    public static void filterOffers(MerchantOffers offers) {
        offers.removeIf(offer -> RestrictApi.deny(Layer.OBTAIN, offer.getResult(), Stat.TRADES));
    }
}
