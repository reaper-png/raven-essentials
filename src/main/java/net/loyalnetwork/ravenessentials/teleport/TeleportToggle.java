package net.loyalnetwork.ravenessentials.teleport;

import net.loyalnetwork.ravenessentials.core.RavenAttachments;
import net.minecraft.server.level.ServerPlayer;

/** Se o jogador aceita receber pedidos de teleporte. Fica salvo no jogador. */
public final class TeleportToggle {

    private TeleportToggle() {
    }

    public static boolean acceptsRequests(ServerPlayer player) {
        return !player.getData(RavenAttachments.TP_DISABLED);
    }

    /** Liga/desliga e devolve o estado novo. */
    public static boolean toggle(ServerPlayer player) {
        boolean enabled = !acceptsRequests(player);
        player.setData(RavenAttachments.TP_DISABLED, !enabled);
        return enabled;
    }
}
