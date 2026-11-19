package net.loyalnetwork.ravenessentials.command.inventory;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Menus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class Anvil {

    @Command(value = "anvil", permission = RavenPermissions.ANVIL)
    void anvil(ServerPlayer player) {
        Menus.open(player, "Anvil",
                (containerId, inventory, menuPlayer) -> new AnvilMenu(containerId, inventory, ContainerLevelAccess.NULL));
    }
}
