package net.loyalnetwork.ravenessentials.vanish;

import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.core.RavenAttachments;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Estado do vanish. Fica salvo nos dados do jogador (sobrevive a relog, morte e restart),
 * e é consultado pelos mixins que escondem o jogador da entidade, do tab, da lista do servidor
 * e das mensagens de entrada/saída.
 */
public final class Vanish {

    private Vanish() {
    }

    /**
     * Registra a regra do vanish no diretório de jogadores da CoffeeLib: todo comando que recebe
     * um jogador (deste mod ou de outro) deixa quem está em vanish fora do autocompletar e
     * responde "não encontrado" ao nome dele, para quem não tem a permissão de ver.
     */
    public static void install() {
        CoffeeLib.players().visibility(RavenEssentials.MODID, (viewer, target) ->
                !(viewer instanceof ServerPlayer seer) || !(target instanceof ServerPlayer seen) || canSee(seer, seen));
    }

    /** Se é um jogador em vanish. Aceita qualquer entidade (ou null) para os eventos e mixins. */
    public static boolean isVanished(@Nullable Entity entity) {
        return entity instanceof ServerPlayer player && player.getData(RavenAttachments.VANISHED);
    }

    /** Se {@code viewer} pode ver {@code target}: sempre, a não ser que o alvo esteja em vanish e o viewer não tenha a permissão de ver. */
    public static boolean canSee(ServerPlayer viewer, ServerPlayer target) {
        return viewer == target || !isVanished(target) || Permissions.has(viewer, RavenPermissions.VANISH_SEE);
    }

    /** Em vanish, sem a permissão de pegar itens: não pega itens, XP nem flechas. */
    public static boolean blocksPickup(@Nullable Entity entity) {
        return isVanished(entity) && !Permissions.has(entity, RavenPermissions.VANISH_PICKUP);
    }

    /** Jogadores que não estão em vanish (contagem pública, lista do servidor...). */
    public static List<ServerPlayer> visiblePlayers(PlayerList players) {
        return players.getPlayers().stream().filter(player -> !isVanished(player)).toList();
    }

    /** Liga ou desliga o vanish e atualiza na hora o que os outros jogadores veem. */
    public static void setVanished(ServerPlayer player, boolean vanished) {
        if (isVanished(player) == vanished) return;

        player.setData(RavenAttachments.VANISHED, vanished);

        var server = player.getServer();
        if (server == null) return;

        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other == player || Permissions.has(other, RavenPermissions.VANISH_SEE)) continue;

            if (vanished) {
                other.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID())));
            } else {
                other.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player)));
            }
        }

        // Recalcula quem rastreia a entidade: some (ou reaparece) para os outros imediatamente.
        player.serverLevel().getChunkSource().chunkMap.move(player);
        server.invalidateStatus();
    }

    /**
     * Jogador em vanish parado exatamente em (x, y, z): vários sons do jogador (ataque, level up)
     * são tocados na posição dele sem dizer quem é a origem.
     */
    @Nullable
    public static ServerPlayer vanishedAt(PlayerList players, double x, double y, double z, ResourceKey<Level> dimension) {
        for (ServerPlayer player : players.getPlayers()) {
            if (player.level().dimension() == dimension
                    && isVanished(player)
                    && player.distanceToSqr(x, y, z) < 0.01) {
                return player;
            }
        }
        return null;
    }

    /** Envia o pacote só para quem pode ver {@code target} (incluindo ele mesmo). */
    public static void sendPacketToViewers(PlayerList players, ServerPlayer target, Packet<?> packet) {
        for (ServerPlayer viewer : players.getPlayers()) {
            if (canSee(viewer, target)) {
                viewer.connection.send(packet);
            }
        }
    }

    /** Envia a mensagem só para quem pode ver o jogador em vanish (a equipe) e para o console. */
    public static void sendToViewers(ServerPlayer vanished, Component message) {
        var server = vanished.getServer();
        if (server == null) return;

        server.sendSystemMessage(message);

        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != vanished && canSee(other, vanished)) {
                other.sendSystemMessage(message);
            }
        }
    }

    /**
     * Anúncio público sobre {@code player} (morte, conquista, entrada...): se ele está em vanish,
     * {@code hiddenMessage} vai só para a equipe; senão roda o anúncio vanilla.
     */
    public static void announce(ServerPlayer player, Component hiddenMessage, Runnable vanillaBroadcast) {
        if (isVanished(player)) {
            sendToViewers(player, hiddenMessage);
        } else {
            vanillaBroadcast.run();
        }
    }

    /** A mensagem com " (vanish)" no fim, para a equipe saber que só ela viu. */
    public static Component tagged(Component message) {
        return message.copy().append(" (vanish)");
    }
}
