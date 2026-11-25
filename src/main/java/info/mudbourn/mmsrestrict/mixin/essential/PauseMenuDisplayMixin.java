package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.handlers.PauseMenuDisplay;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PauseMenuDisplay.class, remap = false)
public abstract class PauseMenuDisplayMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mmsRestrict$menuOff(CallbackInfoReturnable<Boolean> cir) {
        EssentialRestrict.blocked("main menu and pause menu sidebars");
        cir.setReturnValue(false);
    }

    @Inject(method = "canRescale", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mmsRestrict$noRescale(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
