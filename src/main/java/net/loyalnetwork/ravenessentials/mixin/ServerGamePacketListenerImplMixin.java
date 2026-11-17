package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Saída silenciosa: a mensagem "saiu do jogo" de quem está em vanish vai só para a equipe. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "removePlayerFromWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void ravenessentials$silentQuit(PlayerList self, Component message, boolean bypassHiddenChat) {
        if (Vanish.isVanished(player)) {
            Vanish.sendToViewers(player, message.copy().append(Component.literal(" (vanish)")));
        } else {
            self.broadcastSystemMessage(message, bypassHiddenChat);
        }
    }
}
