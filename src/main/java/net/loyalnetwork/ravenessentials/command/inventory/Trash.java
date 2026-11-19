package net.loyalnetwork.ravenessentials.command.inventory;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Menus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;

public class Trash {

    // Baú temporário: o que ficar dentro some quando o menu fecha.
    @Command(value = "trash", aliases = "disposal", permission = RavenPermissions.TRASH)
    void trash(ServerPlayer player) {
        Menus.open(player, "Lixeira", (containerId, inventory, menuPlayer) -> ChestMenu.fourRows(containerId, inventory));
    }
}
