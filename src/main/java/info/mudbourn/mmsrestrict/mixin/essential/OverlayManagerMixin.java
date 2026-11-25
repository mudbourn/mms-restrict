package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.gui.overlay.Layer;
import gg.essential.gui.overlay.OverlayManagerImpl;
import info.mudbourn.mmsrestrict.client.essential.EssentialGuard;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OverlayManagerImpl.class, remap = false)
public abstract class OverlayManagerMixin {

    @Inject(method = "addLayer", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$blockLayers(Layer layer, CallbackInfo ci) {
        if (!EssentialGuard.allowsOverlay()) {
            EssentialRestrict.blocked("overlays, modals and menu layers");
            ci.cancel();
        }
    }
}
