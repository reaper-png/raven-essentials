package net.loyalnetwork.ravenessentials.teleport.command;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.teleport.application.TeleportService;
import net.loyalnetwork.ravenessentials.teleport.message.TeleportFeedback;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/** {@code /tphere <jogador>} e {@code /tpall}: teleportes diretos, sem pedido. */
public class TeleportDirectCommand {

    @Receive
    TeleportService teleports;

    @Receive
    TeleportFeedback feedback;

    @Command(value = "tphere", permission = RavenPermissions.TPHERE)
    void tphere(ServerPlayer player, @Arg("player") ServerPlayer target) {
        if (player.getUUID().equals(target.getUUID())) {
            feedback.self(player);
            return;
        }

        String targetName = Players.name(target);
        UUID targetId = target.getUUID();
        if (!teleports.here(player, target)) {
            feedback.failed(player);
            return;
        }
        feedback.here(player, targetName, targetId);
    }

    @Command(value = "tpall", permission = RavenPermissions.TPALL)
    void tpall(ServerPlayer player) {
        feedback.all(player, teleports.all(player));
    }
}
