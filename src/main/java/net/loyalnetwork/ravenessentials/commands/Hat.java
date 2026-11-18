package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class Hat {

    @Command(value = "hat", permission = RavenPermissions.HAT)
    void hat(ServerPlayer player) {
        var newHelmet = Players.requireHeldItem(player).copy();
        var oldHelmet = player.getItemBySlot(EquipmentSlot.HEAD);

        // Tira o item da mão, equipa e devolve o capacete anterior.
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.HEAD, newHelmet);
        Players.give(player, oldHelmet);

        Messages.send(player, "Aproveite seu novo chapéu");
    }
}
