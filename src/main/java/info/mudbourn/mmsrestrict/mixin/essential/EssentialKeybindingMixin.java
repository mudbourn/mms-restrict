package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.key.EssentialKeybinding;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EssentialKeybinding.class, remap = false)
public abstract class EssentialKeybindingMixin {

    @Shadow
    @Final
    private String keyId;

    @Shadow
    @Final
    public KeyMapping keyBinding;

    @Inject(
        method = "<init>(Ljava/lang/String;Lnet/minecraft/client/KeyMapping$Category;IZ)V",
        at = @At("RETURN"),
        require = 0
    )
    private void mmsRestrict$forgetBlockedKey(CallbackInfo ci) {
        if (!EssentialRestrict.allowsKey(keyId)) {
            KeyMappingAccessor.mmsRestrict$all().remove(keyBinding.getName(), keyBinding);
            KeyMapping.resetMapping();
            EssentialRestrict.blocked("keybinds");
        }
    }

    @Inject(method = "register()V", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$skipRegister(CallbackInfo ci) {
        if (!EssentialRestrict.allowsKey(keyId)) {
            ci.cancel();
        }
    }

    @Inject(
        method = "register([Lnet/minecraft/client/KeyMapping;)[Lnet/minecraft/client/KeyMapping;",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void mmsRestrict$skipRegisterAll(KeyMapping[] bindings, CallbackInfoReturnable<KeyMapping[]> cir) {
        if (!EssentialRestrict.allowsKey(keyId)) {
            cir.setReturnValue(bindings);
        }
    }

    @Inject(method = "tickEvents()V", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$skipTick(CallbackInfo ci) {
        if (!EssentialRestrict.allowsKey(keyId)) {
            ci.cancel();
        }
    }
}
