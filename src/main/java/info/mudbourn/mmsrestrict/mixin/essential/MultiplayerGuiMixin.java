package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.gui.multiplayer.EssentialMultiplayerGui;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EssentialMultiplayerGui.class, remap = false)
public abstract class MultiplayerGuiMixin {

    @Inject(
        method = {
            "initGui",
            "setupButtons",
            "draw",
            "updateButtonState",
            "onButtonClicked",
            "onClosed",
            "showTooltipString",
            "updateSpsSessions",
            "updatePlayerActivity",
            "switchTab"
        },
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void mmsRestrict$noServerTabs(CallbackInfo ci) {
        EssentialRestrict.blocked("multiplayer screen tabs and friends list");
        ci.cancel();
    }
}
