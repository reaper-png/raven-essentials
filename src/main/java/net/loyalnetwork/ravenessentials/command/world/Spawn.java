package net.loyalnetwork.ravenessentials.command.world;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;

public class Spawn {

    @Command(value = "spawn", permission = RavenPermissions.SPAWN)
    void spawn(ServerPlayer player) {
        Players.teleportToSpawn(player);
        Messages.send(player, "Teleportado para o spawn.");
    }
}
