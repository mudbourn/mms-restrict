package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.config.EssentialConfig;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EssentialConfig.class, remap = false)
public abstract class EssentialConfigMixin {

    @Inject(method = "getEssentialScreenshots", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$noScreenshotCapture(CallbackInfoReturnable<Boolean> cir) {
        EssentialRestrict.blocked("screenshot capture hook");
        cir.setReturnValue(false);
    }

    @Inject(method = "getShowEssentialIndicatorOnNametag", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$noNametagIndicator(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
