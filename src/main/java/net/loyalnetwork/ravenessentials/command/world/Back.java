package net.loyalnetwork.ravenessentials.command.world;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.coffeelib.neoforge.location.Locations;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.database.PlayerRepository;
import net.loyalnetwork.ravenessentials.database.PlayerTracker;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class Back {

    @Receive
    PlayerRepository players;

    @Receive
    Scheduler scheduler;

    // /back volta para o mais recente entre o último teleporte e a última morte.
    // Assíncrono: a CoffeeLib responde quando o future termina, e uma CommandException lançada
    // dentro dele vira a mensagem de erro, como num comando comum.
    @Command(value = "back", permission = RavenPermissions.BACK)
    CompletableFuture<Void> back(ServerPlayer player) {
        // onMain: o resto roda na thread do servidor, onde dá para teleportar.
        return scheduler.onMain(players.findById(player.getStringUUID())).thenAccept(found -> {
            if (player.hasDisconnected()) {
                return;
            }
            CoffeeLocation target = found.map(PlayerEntity::backLocation)
                    .orElseThrow(() -> new CommandException("Nenhum local para voltar."));

            // Guarda onde ele está agora: um segundo /back volta para cá.
            PlayerTracker.rememberLocation(player);
            if (!Locations.teleport(player, target)) {
                throw new CommandException("A dimensão desse local não existe mais.");
            }
            player.sendSystemMessage(Messages.success("Voltando para o último local."));
        });
    }
}
