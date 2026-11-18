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

public class Heal {

    // /heal [player]   vida cheia, fome cheia, apaga o fogo e tira efeitos negativos.
    @Command(value = "heal", permission = RavenPermissions.HEAL)
    void heal(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, RavenPermissions.HEAL_OTHERS);
        Players.heal(target);
        Messages.notify(source, target, "Você foi curado.", Players.name(target) + " foi curado.");
    }

    Collection<String> online(CommandSource source) {
        return Targets.onlineNames(source);
    }
}
