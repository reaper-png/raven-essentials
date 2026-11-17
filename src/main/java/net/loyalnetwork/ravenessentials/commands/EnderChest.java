package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class EnderChest {

    @Command(value = "ec", permission = RavenPermissions.ENDERCHEST)
    void enderChest(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new ChestMenu(
                        MenuType.GENERIC_9x3,
                        containerId,
                        inventory,
                        menuPlayer.getEnderChestInventory(),
                        3
                ),
                Component.translatable("container.enderchest")
        ));
    }
}
