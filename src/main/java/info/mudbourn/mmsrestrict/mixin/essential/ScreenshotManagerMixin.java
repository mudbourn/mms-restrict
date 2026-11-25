package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.network.connectionmanager.media.ScreenshotManager;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenshotManager.class, remap = false)
public abstract class ScreenshotManagerMixin {

    @Inject(method = "handleScreenshotKeyPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$vanillaScreenshot(CallbackInfo ci) {
        EssentialRestrict.blocked("screenshot manager and overlay");
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(
            minecraft.gameDirectory,
            minecraft.gameRenderer.mainRenderTarget(),
            message -> minecraft.execute(() -> minecraft.showDebugChat(message))
        );
        ci.cancel();
    }

    @Inject(method = "handleNewScreenshot", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$ignoreNewScreenshot(CallbackInfo ci) {
        ci.cancel();
    }
}
