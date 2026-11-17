package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class Online {

    // Não conta quem está em vanish.
    @Command(value = "online", permission = RavenPermissions.ONLINE)
    void online(CommandSourceStack source) {
        var server = source.getServer();
        long count = server.getPlayerList().getPlayers().stream()
                .filter(player -> !Vanish.isVanished(player))
                .count();

        source.sendSystemMessage(Component.literal(
                "Jogadores online: " + count + "/" + server.getMaxPlayers()));
    }
}
