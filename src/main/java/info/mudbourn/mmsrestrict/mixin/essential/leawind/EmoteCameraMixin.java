package info.mudbourn.mmsrestrict.mixin.essential.leawind;

import gg.essential.config.EssentialConfig;
import gg.essential.util.HelpersKt;
import info.mudbourn.mmsrestrict.client.essential.LeawindEmoteCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "gg.essential.gui.emotes.EmoteWheel$Companion", remap = false)
public abstract class EmoteCameraMixin {

    @Redirect(
        method = "equipEmote",
        at = @At(value = "INVOKE", target = "Lgg/essential/util/HelpersKt;setPerspective(I)V"),
        require = 0
    )
    private void mmsRestrict$startEmoteCamera(int cameraType) {
        if (!LeawindEmoteCamera.begin(cameraType)) {
            HelpersKt.setPerspective(cameraType);
        }
    }

    @Redirect(
        method = "unequipCurrentEmote$lambda$1",
        at = @At(value = "INVOKE", target = "Lgg/essential/util/HelpersKt;setPerspective(I)V"),
        require = 0
    )
    private static void mmsRestrict$endEmoteCamera(int cameraType) {
        if (!LeawindEmoteCamera.end()) {
            HelpersKt.setPerspective(cameraType);
        }
    }

    @Redirect(
        method = "unequipCurrentEmote$lambda$1",
        at = @At(value = "INVOKE", target = "Lgg/essential/config/EssentialConfig;setEmoteThirdPersonType(I)V"),
        require = 0
    )
    private static void mmsRestrict$keepEmoteCameraChoice(EssentialConfig config, int cameraType) {
        if (!LeawindEmoteCamera.active()) {
            config.setEmoteThirdPersonType(cameraType);
        }
    }
}
