package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.ArrayList;
import java.util.List;

public class Smelt {

    @Command(value = "smelt", permission = RavenPermissions.SMELT)
    void smelt(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Inventory inventory = player.getInventory();

        List<SmeltEntry> entries = new ArrayList<>();

        // Primeiro identifica todos os itens que podem ser derretidos.
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            var stack = inventory.getItem(i);

            if (stack.isEmpty()) continue;

            var input = new SingleRecipeInput(stack);

            var recipe = level.getRecipeManager().getRecipeFor(
                    RecipeType.SMELTING,
                    input,
                    level
            );

            if (recipe.isEmpty()) continue;

            var result = recipe.get().value().assemble(
                    input,
                    level.registryAccess()
            );

            if (result.isEmpty()) continue;

            entries.add(
                    new SmeltEntry(
                            i,
                            stack.copy(),
                            result.copy()
                    )
            );
        }

        int smelted = 0;

        // Depois processa os itens encontrados.
        for (var entry : entries) {
            var current = inventory.getItem(entry.slot());

            if (!ItemStack.isSameItemSameComponents(
                    current,
                    entry.original()
            )) {
                continue;
            }

            int amount = current.getCount();

            // Multiplica o resultado pela quantidade de itens.
            ItemStack result = entry.result().copyWithCount(
                    entry.result().getCount() * amount
            );

            // Remove o item original.
            inventory.setItem(entry.slot(), ItemStack.EMPTY);

            // Insere o resultado no inventário.
            Players.give(player, result);

            smelted += amount;
        }

        Messages.send(player, "Itens derretidos: " + smelted);
    }

    private record SmeltEntry(
            int slot,
            ItemStack original,
            ItemStack result
    ) {}
}
