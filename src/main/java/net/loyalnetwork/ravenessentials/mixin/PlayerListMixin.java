package net.loyalnetwork.ravenessentials.mixin;

import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/** Esconde os jogadores em vanish do tab list e das mensagens de entrada. */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    // Tab list enviado para quem está entrando: sem os jogadores em vanish que ele não pode ver.
    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket;createPlayerInitializing(Ljava/util/Collection;)Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket;",
                    ordinal = 0
            )
    )
    private ClientboundPlayerInfoUpdatePacket ravenessentials$filterTabForJoiner(
            Collection<ServerPlayer> players, Connection connection, ServerPlayer joining, CommonListenerCookie cookie
    ) {
        return ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
                players.stream().filter(other -> Vanish.canSee(joining, other)).toList()
        );
    }

    // Quem entra em vanish só é adicionado ao tab de quem pode vê-lo.
    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 0
            )
    )
    private void ravenessentials$announceToViewers(
            PlayerList self, Packet<?> packet, Connection connection, ServerPlayer joining, CommonListenerCookie cookie
    ) {
        for (ServerPlayer viewer : self.getPlayers()) {
            if (Vanish.canSee(viewer, joining)) {
                viewer.connection.send(packet);
            }
        }
    }

    /*
     * Sons, efeitos de bloco (quebrar, colocar, portas) e progresso de quebra passam por aqui.
     * Se a origem é alguém em vanish - o "except" ou um som tocado exatamente na posição dele -
     * só quem pode vê-lo recebe o pacote.
     */
    @Inject(method = "broadcast", at = @At("HEAD"), cancellable = true)
    private void ravenessentials$muteVanished(
            @Nullable Player except, double x, double y, double z, double radius,
            ResourceKey<Level> dimension, Packet<?> packet, CallbackInfo ci
    ) {
        PlayerList self = (PlayerList) (Object) this;
        ServerPlayer source = except instanceof ServerPlayer player && Vanish.isVanished(player)
                ? player
                : except == null ? Vanish.vanishedAt(self, x, y, z, dimension) : null;

        if (source == null) return;
        ci.cancel();

        for (ServerPlayer viewer : self.getPlayers()) {
            if (viewer == except || viewer.level().dimension() != dimension) continue;
            if (viewer.distanceToSqr(x, y, z) >= radius * radius) continue;
            if (Vanish.canSee(viewer, source)) {
                viewer.connection.send(packet);
            }
        }
    }

    // Entrada silenciosa: a mensagem "entrou no jogo" vai só para a equipe.
    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
                    ordinal = 0
            )
    )
    private void ravenessentials$silentJoin(
            PlayerList self, Component message, boolean bypassHiddenChat,
            Connection connection, ServerPlayer joining, CommonListenerCookie cookie
    ) {
        if (Vanish.isVanished(joining)) {
            Vanish.sendToViewers(joining, message.copy().append(Component.literal(" (vanish)")));
        } else {
            self.broadcastSystemMessage(message, bypassHiddenChat);
        }
    }
}
