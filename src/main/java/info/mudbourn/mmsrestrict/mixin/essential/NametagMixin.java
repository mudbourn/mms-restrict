package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.cosmetics.IconCosmeticRenderer;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IconCosmeticRenderer.class, remap = false)
public abstract class NametagMixin {

    @Inject(
        method = { "drawNameTagIconAndVersionConsistentPadding", "drawTextureInTabList" },
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void mmsRestrict$noIcons(CallbackInfo ci) {
        EssentialRestrict.blocked("nametag and tab list icons");
        ci.cancel();
    }

    @Inject(method = "getNameplateXOffset", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$noOffset(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0f);
    }
}
