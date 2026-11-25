package info.mudbourn.mmsrestrict.mixin.essential;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.essential.mixins.impl.client.entity.AbstractClientPlayerExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Essential no longer poses the vanilla cape, so an emote leaves it hanging where a standing
// player's cape would be. Hide the vanilla cape while an emote is posing the player.
@Mixin(CapeLayer.class)
public abstract class EmoteCapeMixin {

    @Inject(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mmsRestrict$hideDuringEmote(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        Entity entity = client.level.getEntity(state.id);
        if (entity instanceof AbstractClientPlayerExt ext && ext.isPoseModified()) {
            ci.cancel();
        }
    }
}
