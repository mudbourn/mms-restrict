package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.handlers.OnlineIndicator;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OnlineIndicator.class, remap = false)
public abstract class OnlineIndicatorMixin {

    @Inject(method = "drawTabIndicatorOuter", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mmsRestrict$noTabIndicator(CallbackInfo ci) {
        EssentialRestrict.blocked("tab list online indicator");
        ci.cancel();
    }
}
