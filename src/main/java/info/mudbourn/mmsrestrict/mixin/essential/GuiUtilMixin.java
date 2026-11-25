package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.util.GuiUtil;
import info.mudbourn.mmsrestrict.client.essential.EssentialGuard;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import kotlin.jvm.functions.Function0;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiUtil.class, remap = false)
public abstract class GuiUtilMixin {

    @Inject(
        method = "openScreen(Ljava/lang/Class;Lkotlin/jvm/functions/Function0;Z)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void mmsRestrict$gateOpen(Class<?> type, Function0<?> screen, boolean replace, CallbackInfo ci) {
        if (EssentialRestrict.allowsScreen(type.getName())) {
            EssentialGuard.openingAllowedScreen();
            return;
        }
        EssentialRestrict.blocked("screens");
        ci.cancel();
    }
}
