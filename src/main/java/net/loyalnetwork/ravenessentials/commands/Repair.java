package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class Repair {

    public enum Scope { HAND, ALL }

    // /repair [hand|all]   all = inventário inteiro, armadura e mão secundária inclusas.
    @Command(value = "repair", aliases = "fix", permission = RavenPermissions.REPAIR)
    void repair(ServerPlayer player, @Arg("scope") @Default("hand") Scope scope) {
        if (scope == Scope.HAND) {
            ItemStack stack = Players.requireHeldItem(player);
            if (!stack.isDamageableItem()) {
                throw new CommandException("Esse item não pode ser reparado.");
            }
            if (!repair(stack)) {
                throw new CommandException("Esse item já está inteiro.");
            }
            Messages.send(player, "Item reparado.");
            return;
        }

        var inventory = player.getInventory();
        int repaired = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (repair(inventory.getItem(i))) repaired++;
        }

        Messages.send(player, "Itens reparados: " + repaired);
    }

    private static boolean repair(ItemStack stack) {
        if (!stack.isDamageableItem() || !stack.isDamaged()) return false;
        stack.setDamageValue(0);
        return true;
    }
}
