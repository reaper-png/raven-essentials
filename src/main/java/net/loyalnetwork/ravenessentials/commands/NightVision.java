package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class NightVision {

    // Visão noturna infinita, sem partículas nem ícone. Rodar de novo desliga.
    @Command(value = "nightvision", aliases = "nv", permission = RavenPermissions.NIGHTVISION)
    void nightVision(ServerPlayer player) {
        if (player.hasEffect(MobEffects.NIGHT_VISION)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.sendSystemMessage(Component.literal("Visão noturna desligada.").withStyle(ChatFormatting.RED));
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                MobEffectInstance.INFINITE_DURATION,
                0,
                false, // ambient
                false, // partículas
                false  // ícone
        ));
        player.sendSystemMessage(Component.literal("Visão noturna ligada.").withStyle(ChatFormatting.GREEN));
    }
}
