package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.gui.proxies.ScreenWithProxiesHandler;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenWithProxiesHandler.class, remap = false)
public abstract class ScreenProxiesMixin {

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$noProxyButtons(CallbackInfo ci) {
        EssentialRestrict.blocked("menu button proxies and tinted vanilla buttons");
        ci.cancel();
    }
}
