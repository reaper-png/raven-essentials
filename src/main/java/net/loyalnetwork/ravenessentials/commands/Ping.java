package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class Ping {

    // /ping [player]
    @Command(value = "ping", permission = RavenPermissions.PING)
    void ping(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, null);
        int latency = target.connection.latency();

        String who = target == source.getPlayer() ? "Seu ping" : "Ping de " + target.getGameProfile().getName();
        source.sendSystemMessage(Component.literal(who + ": " + latency + " ms"));
    }

    Collection<String> online() {
        return Targets.onlineNames();
    }
}
