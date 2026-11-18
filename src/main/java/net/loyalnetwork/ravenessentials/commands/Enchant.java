package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.util.RegistryIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class Enchant {

    private static final int MAX_LEVEL = 255;

    // /enchant <enchantment> [level]
    @Command(value = "enchant", permission = RavenPermissions.ENCHANT)
    void enchant(
            ServerPlayer player,
            @Arg("enchantment") @Suggest(provider = "enchantments") String enchantmentId,
            @Arg("level") @Default("1") int level
    ) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new CommandException("O nível deve estar entre 1 e " + MAX_LEVEL + ".");
        }

        var stack = Players.requireHeldItem(player);
        var enchantment = RegistryIds.find(player.registryAccess(), Registries.ENCHANTMENT, enchantmentId, "encantamento");

        // Aplica diretamente, sem validar compatibilidade ou nível máximo vanilla.
        stack.enchant(enchantment, level);

        Messages.send(player, "Encantamento aplicado: " + enchantmentId + " " + level);
    }

    Collection<String> enchantments() {
        return RegistryIds.vanillaPaths(Registries.ENCHANTMENT);
    }
}
