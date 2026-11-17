package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.commands.God;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Em modo god a vida nunca diminui, venha de onde vier: dano, /kill, void,
 * ou um mod chamando setHealth direto. Curar continua funcionando.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract float getHealth();

    @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true)
    private float ravenessentials$godKeepsHealth(float health) {
        // connection != null: só jogadores já conectados (setHealth também roda no construtor).
        if ((Object) this instanceof ServerPlayer player && player.connection != null && God.isGod(player)) {
            return Math.max(health, getHealth());
        }
        return health;
    }
}
