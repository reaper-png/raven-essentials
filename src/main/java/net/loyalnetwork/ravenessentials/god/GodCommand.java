package net.loyalnetwork.ravenessentials.god;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.OrSelf;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class GodCommand {

    // /god [player]
    @Command(value = "god", permission = RavenPermissions.GOD)
    void god(CommandSourceStack source, @OrSelf(others = RavenPermissions.GOD_OTHERS) ServerPlayer target) {
        boolean enabled = !God.isGod(target);
        God.setGod(target, enabled);
        Messages.toggled(source, target, "Modo god", enabled);
    }
}
