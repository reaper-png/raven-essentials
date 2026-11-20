package net.loyalnetwork.ravenessentials.command.info;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.OrSelf;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class Ping {

    // /ping [player]
    @Command(value = "ping", permission = RavenPermissions.PING)
    void ping(CommandSourceStack source, @OrSelf ServerPlayer target) {
        String who = target == source.getPlayer() ? "Seu ping" : "Ping de " + Players.name(target);
        Messages.send(source, who + ": " + target.connection.latency() + " ms");
    }
}
