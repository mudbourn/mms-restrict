package info.mudbourn.mmsrestrict.mixin.essential;

import info.mudbourn.mmsrestrict.client.essential.EssentialGuard;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$blockEssentialScreens(Screen screen, CallbackInfo ci) {
        if (screen == null) {
            return;
        }
        String name = screen.getClass().getName();
        if (!EssentialRestrict.allowsScreen(name)) {
            EssentialRestrict.blocked("screens");
            ci.cancel();
        } else if (EssentialRestrict.isAllowedEssentialScreen(name)) {
            EssentialGuard.allowedScreenOpened();
        }
    }
}
