package net.loyalnetwork.ravenessentials.command.inventory;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.minecraft.server.level.ServerPlayer;

public class Clear {

    @Command(value = "clear", permission = RavenPermissions.CLEAR)
    void clear(ServerPlayer player) {
        player.getInventory().clearContent();
    }
}
