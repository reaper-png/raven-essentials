package net.loyalnetwork.ravenessentials.command.info;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.commands.CommandSourceStack;

public class Online {

    // Não conta quem está em vanish.
    @Command(value = "online", permission = RavenPermissions.ONLINE)
    void online(CommandSourceStack source) {
        var server = source.getServer();
        int count = Vanish.visiblePlayers(server.getPlayerList()).size();
        Messages.send(source, "Jogadores online: " + count + "/" + server.getMaxPlayers());
    }
}
