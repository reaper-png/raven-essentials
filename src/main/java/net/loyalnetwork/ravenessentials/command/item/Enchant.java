package net.loyalnetwork.ravenessentials.command.item;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Registry;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;


public class Enchant {

    private static final int MAX_LEVEL = 255;

    // /enchant <enchantment> [level]
    @Command(value = "enchant", permission = RavenPermissions.ENCHANT)
    void enchant(
            ServerPlayer player,
            @Arg("enchantment") @Registry("enchantment") Holder<Enchantment> enchantment,
            @Arg("level") @Default("1") int level
    ) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new CommandException("O nível deve estar entre 1 e " + MAX_LEVEL + ".");
        }

        var stack = Players.requireHeldItem(player);

        // Aplica diretamente, sem validar compatibilidade ou nível máximo vanilla.
        stack.enchant(enchantment, level);

        Messages.send(player, "Encantamento aplicado: " + enchantment.getRegisteredName() + " " + level);
    }
}
