package net.loyalnetwork.ravenessentials.command.player;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class NightVision {

    // Visão noturna infinita, sem partículas nem ícone. Rodar de novo desliga.
    @Command(value = "nightvision", aliases = "nv", permission = RavenPermissions.NIGHTVISION)
    void nightVision(ServerPlayer player) {
        boolean enable = !player.hasEffect(MobEffects.NIGHT_VISION);

        if (enable) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    MobEffectInstance.INFINITE_DURATION,
                    0,
                    false, // ambient
                    false, // partículas
                    false  // ícone
            ));
        } else {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        player.sendSystemMessage(enable ? Messages.success("Visão noturna ligada.") : Messages.error("Visão noturna desligada."));
    }
}
