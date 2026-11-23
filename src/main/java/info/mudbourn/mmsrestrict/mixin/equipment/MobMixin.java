package info.mudbourn.mmsrestrict.mixin.equipment;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "equipItemIfPossible", at = @At("HEAD"), cancellable = true)
    private void mmsRestrict$leaveRestrictedItems(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (RestrictApi.isRestricted(Layer.OBTAIN, stack)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
