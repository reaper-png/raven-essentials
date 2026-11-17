package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;

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

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            throw new CommandException("Você precisa estar segurando um item.");
        }

        ResourceLocation location = ResourceLocation.tryParse(enchantmentId);

        if (location == null) {
            throw new CommandException("ID de encantamento inválido.");
        }

        var registry = player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        var key = ResourceKey.create(
                Registries.ENCHANTMENT,
                location
        );

        var enchantment = registry.get(key);

        if (enchantment.isEmpty()) {
            throw new CommandException("Encantamento não encontrado: " + enchantmentId);
        }

        Holder<Enchantment> holder = enchantment.get();

        // Aplica diretamente, sem validar compatibilidade ou nível máximo vanilla.
        stack.enchant(holder, level);

        player.sendSystemMessage(
                Component.literal(
                        "Encantamento aplicado: " + enchantmentId + " " + level
                )
        );
    }

    Collection<String> enchantments() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) return List.of();

        return server.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .keySet()
                .stream()
                // O argumento é uma palavra só (sem ':'), então sugere os encantamentos vanilla pelo nome.
                .filter(id -> id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                .map(ResourceLocation::getPath)
                .toList();
    }
}
