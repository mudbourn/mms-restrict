package info.mudbourn.mmsrestrict.mixin.trades;

import info.mudbourn.mmsrestrict.obtain.Obtain;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    @Inject(method = "getOffers", at = @At("RETURN"))
    private void mmsRestrict$dropRestrictedOffers(CallbackInfoReturnable<MerchantOffers> cir) {
        Obtain.filterOffers(cir.getReturnValue());
    }
}
