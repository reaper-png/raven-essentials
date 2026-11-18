package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Placas de pressão e tripwire ignoram quem está em vanish. */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isIgnoringBlockTriggers", at = @At("HEAD"), cancellable = true)
    private void ravenessentials$ignoreTriggersWhileVanished(CallbackInfoReturnable<Boolean> cir) {
        if (Vanish.isVanished((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
