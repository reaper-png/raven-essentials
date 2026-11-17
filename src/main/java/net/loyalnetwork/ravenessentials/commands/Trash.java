package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

public class Trash {

    // Baú temporário: o que ficar dentro some quando o menu fecha.
    @Command(value = "trash", aliases = "disposal", permission = RavenPermissions.TRASH)
    void trash(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> ChestMenu.fourRows(containerId, inventory),
                Component.literal("Lixeira")
        ));
    }
}
