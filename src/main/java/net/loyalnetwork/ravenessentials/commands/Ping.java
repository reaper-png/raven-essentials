package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.util.Targets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class Ping {

    // /ping [player]
    @Command(value = "ping", permission = RavenPermissions.PING)
    void ping(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, null);
        String who = Targets.isSelf(source, target) ? "Seu ping" : "Ping de " + Players.name(target);
        Messages.send(source, who + ": " + target.connection.latency() + " ms");
    }

    Collection<String> online(CommandSource source) {
        return Targets.onlineNames(source);
    }
}
