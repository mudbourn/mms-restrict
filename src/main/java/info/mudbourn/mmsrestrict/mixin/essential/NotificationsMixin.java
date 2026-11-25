package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.gui.notification.NotificationsImpl;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NotificationsImpl.class, remap = false)
public abstract class NotificationsMixin {

    @Inject(method = { "pushSync", "pushPersistentToast" }, at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$dropToasts(CallbackInfo ci) {
        EssentialRestrict.blocked("toasts and notifications");
        ci.cancel();
    }
}
