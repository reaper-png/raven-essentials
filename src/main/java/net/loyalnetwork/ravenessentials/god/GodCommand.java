package net.loyalnetwork.ravenessentials.god;

import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Targets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class GodCommand {

    // /god [player]
    @Command(value = "god", permission = RavenPermissions.GOD)
    void god(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, RavenPermissions.GOD_OTHERS);

        boolean enabled = !God.isGod(target);
        God.setGod(target, enabled);
        Messages.toggled(source, target, "Modo god", enabled);
    }

    Collection<String> online(CommandSource source) {
        return Targets.onlineNames(source);
    }
}
