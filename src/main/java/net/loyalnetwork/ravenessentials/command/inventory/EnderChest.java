package net.loyalnetwork.ravenessentials.command.inventory;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Menus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;

public class EnderChest {

    @Command(value = "ec", permission = RavenPermissions.ENDERCHEST)
    void enderChest(ServerPlayer player) {
        Menus.open(player, Component.translatable("container.enderchest"),
                (containerId, inventory, menuPlayer) ->
                        ChestMenu.threeRows(containerId, inventory, menuPlayer.getEnderChestInventory()));
    }
}
