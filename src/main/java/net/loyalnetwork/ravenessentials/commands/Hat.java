package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class Hat {

    @Command(value = "hat", permission = RavenPermissions.HAT)
    void hat(ServerPlayer player) {
        var handItem = player.getMainHandItem();

        if (handItem.isEmpty()) {
            throw new CommandException("Você precisa estar segurando um item.");
        }

        var helmetItem = player.getItemBySlot(EquipmentSlot.HEAD);

        // Copia o item que será equipado e remove o item da mão
        var newHelmet = handItem.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        // Equipa o novo item e devolve o anterior
        player.setItemSlot(EquipmentSlot.HEAD, newHelmet);

        if (!helmetItem.isEmpty()) {
            player.getInventory().placeItemBackInInventory(helmetItem);
        }

        player.sendSystemMessage(Component.literal("Aproveite seu novo chapéu"));
    }
}
