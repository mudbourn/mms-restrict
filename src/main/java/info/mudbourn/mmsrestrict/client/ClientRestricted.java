package info.mudbourn.mmsrestrict.client;

import info.mudbourn.mmsrestrict.client.jei.RestrictJeiPlugin;
import info.mudbourn.mmsrestrict.mixin.hide.CreativeModeTabsAccessor;
import info.mudbourn.mmsrestrict.net.RestrictedPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

// The hidden item ids last sent by the server.
public final class ClientRestricted {

    private static volatile Set<Identifier> hidden = Set.of();

    private ClientRestricted() {
    }

    public static Set<Identifier> hidden() {
        return hidden;
    }

    public static boolean isHidden(ItemStack stack) {
        return hidden.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static void reset() {
        hidden = Set.of();
        CreativeModeTabsAccessor.mmsRestrict$setCachedParameters(null);
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            RestrictJeiPlugin.refresh();
        }
    }

    public static void accept(RestrictedPayload payload, ClientPlayNetworking.Context context) {
        hidden = Set.copyOf(new HashSet<>(payload.items()));
        CreativeModeTabsAccessor.mmsRestrict$setCachedParameters(null);
        LocalPlayer player = context.client().player;
        if (player != null) {
            CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), player.canUseGameMasterBlocks(), player.connection.registryAccess());
        }
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            RestrictJeiPlugin.refresh();
        }
    }
}
