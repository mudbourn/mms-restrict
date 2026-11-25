package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.util.EventBus;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EventBus.class, remap = false)
public abstract class EventBusMixin {

    @Inject(method = "register(Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$skipFeatureListeners(Object listener, CallbackInfo ci) {
        if (EssentialRestrict.blocksSubscriber(listener)) {
            EssentialRestrict.blocked("listener " + listener.getClass().getSimpleName());
            ci.cancel();
        }
    }
}
