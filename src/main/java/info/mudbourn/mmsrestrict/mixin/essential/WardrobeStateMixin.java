package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.gui.wardrobe.WardrobeCategory;
import gg.essential.gui.wardrobe.WardrobeState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = WardrobeState.class, remap = false)
public abstract class WardrobeStateMixin {

    @Inject(method = "allCategories$lambda$0", at = @At("RETURN"), cancellable = true, require = 0)
    private static void mmsRestrict$emoteCategoriesOnly(CallbackInfoReturnable<List<WardrobeCategory>> cir) {
        List<WardrobeCategory> kept = cir.getReturnValue()
            .stream()
            .filter(category -> category == WardrobeCategory.Emotes.INSTANCE || category.getSuperCategory() == WardrobeCategory.Emotes.INSTANCE)
            .toList();
        cir.setReturnValue(kept);
    }
}
