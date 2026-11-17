package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pisar num sculk sensor (e no calibrado, que herda dele) ou num shrieker ativa o bloco
 * direto, sem passar pelo game event que o VanishEvents já cancela.
 */
@Mixin({SculkSensorBlock.class, SculkShriekerBlock.class})
public abstract class SculkStepMixin {

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void ravenessentials$ignoreVanishedStep(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player && Vanish.isVanished(player)) {
            ci.cancel();
        }
    }
}
