package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.cosmetics.PlayerWearableManager;
import gg.essential.gui.elementa.state.v2.State;
import gg.essential.mod.cosmetics.CosmeticSlot;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = PlayerWearableManager.class, remap = false)
public abstract class WearableManagerMixin {

    @Redirect(
        method = "updateCosmetics",
        at = @At(
            value = "INVOKE",
            target = "Lgg/essential/gui/elementa/state/v2/State;getUntracked()Ljava/lang/Object;"
        ),
        require = 0
    )
    private Object mmsRestrict$onlyEmotes(State<?> equipped) {
        Object value = equipped.getUntracked();
        if (!(value instanceof Map<?, ?> cosmetics)) {
            return value;
        }
        EssentialRestrict.blocked("worn cosmetics rendering");
        Map<Object, Object> emotes = new HashMap<>();
        for (Map.Entry<?, ?> entry : cosmetics.entrySet()) {
            // The cape slot holds the player's own cape, which Essential draws itself so it follows emote poses.
            if (entry.getKey() == CosmeticSlot.EMOTE || entry.getKey() == CosmeticSlot.CAPE) {
                emotes.put(entry.getKey(), entry.getValue());
            }
        }
        return emotes;
    }
}
