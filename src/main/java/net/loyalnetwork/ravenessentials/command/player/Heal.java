package net.loyalnetwork.ravenessentials.command.player;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.OrSelf;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class Heal {

    // /heal [player]   vida cheia, fome cheia, apaga o fogo e tira efeitos negativos.
    @Command(value = "heal", permission = RavenPermissions.HEAL)
    void heal(CommandSourceStack source, @OrSelf(others = RavenPermissions.HEAL_OTHERS) ServerPlayer target) {
        Players.heal(target);
        Messages.notify(source, target, "Você foi curado.", Players.name(target) + " foi curado.");
    }
}
