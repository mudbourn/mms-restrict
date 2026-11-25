package info.mudbourn.mmsrestrict.mixin.essential;

import gg.essential.api.commands.Command;
import gg.essential.commands.EssentialCommandRegistry;
import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EssentialCommandRegistry.class, remap = false)
public abstract class CommandRegistryMixin {

    @Inject(method = "registerCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsRestrict$noCommands(Command command, CallbackInfo ci) {
        EssentialRestrict.blocked("chat commands");
        ci.cancel();
    }
}
