package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Menus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public class Craft {

    @Command(value = "craft", permission = RavenPermissions.CRAFT)
    void craft(ServerPlayer player) {
        Menus.open(player, "Crafting",
                (containerId, inventory, menuPlayer) -> new CraftingMenu(containerId, inventory, ContainerLevelAccess.NULL));
    }
}
