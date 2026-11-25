package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.gui.wardrobe.Wardrobe;
import gg.essential.gui.wardrobe.WardrobeCategory;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Wardrobe.class, remap = false)
public abstract class WardrobeMixin {

    @ModifyVariable(
        method = "<init>(Lgg/essential/gui/wardrobe/WardrobeCategory;Z)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private static WardrobeCategory mmsRestrict$emotesOnly(WardrobeCategory category) {
        EssentialRestrict.blocked("wardrobe pages other than the emote wheel page");
        return WardrobeCategory.Emotes.INSTANCE;
    }

    @ModifyVariable(
        method = "<init>(Lgg/essential/gui/wardrobe/WardrobeCategory;Z)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private static boolean mmsRestrict$wheelPage(boolean inEmoteWheel) {
        return true;
    }
}
