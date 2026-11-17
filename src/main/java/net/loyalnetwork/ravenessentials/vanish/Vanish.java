package net.loyalnetwork.ravenessentials.vanish;

import com.mojang.serialization.Codec;
import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

/**
 * Estado do vanish. Fica salvo nos dados do jogador (sobrevive a relog, morte e restart),
 * e é consultado pelos mixins que escondem o jogador da entidade, do tab, da lista do servidor
 * e das mensagens de entrada/saída.
 */
public final class Vanish {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RavenEssentials.MODID);

    public static final Supplier<AttachmentType<Boolean>> VANISHED = ATTACHMENTS.register(
            "vanished",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build()
    );

    private Vanish() {
    }

    public static boolean isVanished(ServerPlayer player) {
        return player.getData(VANISHED);
    }

    /** Se {@code viewer} pode ver {@code target}: sempre, a não ser que o alvo esteja em vanish e o viewer não tenha a permissão de ver. */
    public static boolean canSee(ServerPlayer viewer, ServerPlayer target) {
        return viewer == target || !isVanished(target) || has(viewer, RavenPermissions.VANISH_SEE);
    }

    public static boolean has(Object subject, String node) {
        PermissionManager permissions = RavenEssentials.permissions();
        return permissions != null && permissions.has(subject, node);
    }

    /** Liga ou desliga o vanish e atualiza na hora o que os outros jogadores veem. */
    public static void setVanished(ServerPlayer player, boolean vanished) {
        if (isVanished(player) == vanished) return;

        player.setData(VANISHED, vanished);

        var server = player.getServer();
        if (server == null) return;

        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other == player || has(other, RavenPermissions.VANISH_SEE)) continue;

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

    /** Envia a mensagem só para quem pode ver o jogador em vanish (a equipe). */
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
}
