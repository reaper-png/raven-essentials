package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.fly.Flight;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.util.Targets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class Fly {

    // /fly [player]   a regra do voo fica no Flight.
    @Command(value = "fly", permission = RavenPermissions.FLY)
    void fly(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, RavenPermissions.FLY_OTHERS);

        boolean wanted = !Flight.isEnabled(target);
        boolean enabled = Flight.setEnabled(target, wanted);

        // Ligado por outro jogador, mas o alvo não tem a permissão: o sync já desligou e avisou.
        if (wanted && !enabled) {
            source.sendSystemMessage(Messages.error(Players.name(target) + " não tem permissão para voar."));
            return;
        }
        Messages.toggled(source, target, "Fly", enabled);
    }

    Collection<String> online(CommandSource source) {
        return Targets.onlineNames(source);
    }
}
