package info.mudbourn.mmsrestrict.mixin.equipment;

import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.Stat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    // A restricted weapon in a mob's hand becomes the vanilla sword of the same tier, so the mob stays armed.
    @ModifyVariable(method = "setItemSlot", at = @At("HEAD"), argsOnly = true)
    private ItemStack mmsRestrict$swapRestrictedWeapon(ItemStack stack, EquipmentSlot slot, ItemStack original) {
        if (!((Object) this instanceof Mob) || slot.getType() != EquipmentSlot.Type.HAND) return stack;
        if (!RestrictApi.deny(Layer.OBTAIN, stack, Stat.MOB_GEAR)) return stack;
        return new ItemStack(mmsRestrict$swordFor(stack));
    }

    // Other restricted gear, such as armor, is refused outright.
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void mmsRestrict$refuseRestrictedGear(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if ((Object) this instanceof Mob && RestrictApi.deny(Layer.OBTAIN, stack, Stat.MOB_GEAR)) {
            ci.cancel();
        }
    }

    @Unique
    private static Item mmsRestrict$swordFor(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (String tier : new String[]{"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"}) {
            if (path.startsWith(tier + "_")) {
                Item sword = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(tier + "_sword"));
                if (sword != Items.AIR) return sword;
            }
        }
        return Items.IRON_SWORD;
    }
}
