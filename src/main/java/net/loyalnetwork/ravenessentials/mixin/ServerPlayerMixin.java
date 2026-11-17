package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    /** O rastreamento de entidades pergunta aqui se deve mandar o jogador para outro cliente. */
    @Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
    private void ravenessentials$hideVanished(ServerPlayer viewer, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!Vanish.canSee(viewer, self)) {
            cir.setReturnValue(false);
        }
    }

    // Mensagem de morte: quem está em vanish morre em silêncio (só a equipe vê).

    @Redirect(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void ravenessentials$silentDeath(PlayerList list, Component message, boolean bypassHiddenChat) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (Vanish.isVanished(self)) {
            Vanish.sendToViewers(self, message);
        } else {
            list.broadcastSystemMessage(message, bypassHiddenChat);
        }
    }

    @Redirect(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemToTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void ravenessentials$silentDeathTeam(PlayerList list, Player player, Component message) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (Vanish.isVanished(self)) {
            Vanish.sendToViewers(self, message);
        } else {
            list.broadcastSystemToTeam(player, message);
        }
    }

    @Redirect(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemToAllExceptTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void ravenessentials$silentDeathOthers(PlayerList list, Player player, Component message) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (Vanish.isVanished(self)) {
            Vanish.sendToViewers(self, message);
        } else {
            list.broadcastSystemToAllExceptTeam(player, message);
        }
    }
}
