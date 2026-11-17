package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public class Craft {

    @Command(value = "craft", permission = RavenPermissions.CRAFT)
    void craft(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new CraftingMenu(containerId, inventory, ContainerLevelAccess.NULL),
                Component.literal("Crafting")
        ));
    }
}
