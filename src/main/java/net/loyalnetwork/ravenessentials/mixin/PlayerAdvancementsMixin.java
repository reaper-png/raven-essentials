package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Anúncio de conquista de quem está em vanish vai só para a equipe. */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    // O anúncio fica num lambda dentro de award(): lambda$award$2 no jar do NeoForge 21.1.
    @Redirect(
            method = "lambda$award$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void ravenessentials$silentAdvancement(PlayerList list, Component message, boolean bypassHiddenChat) {
        if (Vanish.isVanished(player)) {
            Vanish.sendToViewers(player, message);
        } else {
            list.broadcastSystemMessage(message, bypassHiddenChat);
        }
    }
}
