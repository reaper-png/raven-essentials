package net.loyalnetwork.ravenessentials.fly;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.OrSelf;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.fly.Flight;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class FlyCommand {

    // /fly [player]   a regra do voo fica no Flight.
    @Command(value = "fly", permission = RavenPermissions.FLY)
    void fly(CommandSourceStack source, @OrSelf(others = RavenPermissions.FLY_OTHERS) ServerPlayer target) {
        boolean wanted = !Flight.isEnabled(target);
        boolean enabled = Flight.setEnabled(target, wanted);

        // Ligado por outro jogador, mas o alvo não tem a permissão: o sync já desligou e avisou.
        if (wanted && !enabled) {
            source.sendSystemMessage(Messages.error(Players.name(target) + " não tem permissão para voar."));
            return;
        }
        Messages.toggled(source, target, "Fly", enabled);
    }
}
