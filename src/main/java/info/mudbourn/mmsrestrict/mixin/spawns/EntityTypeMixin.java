package info.mudbourn.mmsrestrict.mixin.spawns;

import info.mudbourn.mmsrestrict.RestrictApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(
        method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Lnet/minecraft/world/entity/Entity;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mmsRestrict$denySpawn(Level level, EntitySpawnRequest request, CallbackInfoReturnable<Entity> cir) {
        EntitySpawnReason reason = request.reason();
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION && reason != EntitySpawnReason.EVENT) {
            return;
        }
        if (RestrictApi.isSpawnDenied(level.dimension(), (EntityType<?>) (Object) this)) {
            cir.setReturnValue(null);
        }
    }
}
