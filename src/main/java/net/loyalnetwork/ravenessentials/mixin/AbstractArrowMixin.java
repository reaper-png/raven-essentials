package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Flechas e tridentes no chão: mesma regra dos itens. */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void ravenessentials$noPickupWhileVanished(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (Vanish.blocksPickup(player)) {
            cir.setReturnValue(false);
        }
    }
}
