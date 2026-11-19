package net.loyalnetwork.ravenessentials.command.inventory;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Compact {

    @Command(value = "compact", permission = RavenPermissions.COMPACT)
    void compact(ServerPlayer player) {
        var inventory = player.getInventory();
        ServerLevel level = player.serverLevel();

        List<ItemStack> candidates = new ArrayList<>();

        // Identifica os itens únicos do inventário.
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            var stack = inventory.getItem(i);

            if (stack.isEmpty()) continue;

            boolean exists = candidates.stream().anyMatch(
                    candidate -> ItemStack.isSameItemSameComponents(
                            candidate,
                            stack
                    )
            );

            if (!exists) {
                candidates.add(stack.copyWithCount(1));
            }
        }

        int compacted = 0;

        for (var candidate : candidates) {
            var match = findReversibleRecipe(level, candidate);

            if (match.isEmpty()) continue;

            var compactRecipe = match.get();

            int required = compactRecipe.required();
            var result = compactRecipe.result();

            // Conta os itens disponíveis.
            int total = countItems(inventory, candidate);

            int crafts = total / required;

            if (crafts <= 0) continue;

            // Calcula o espaço disponível para os resultados.
            int maxCrafts = getMaxCrafts(
                    inventory,
                    result,
                    crafts
            );

            crafts = Math.min(crafts, maxCrafts);

            if (crafts <= 0) continue;

            // Consome os ingredientes.
            consume(
                    inventory,
                    candidate,
                    crafts * required
            );

            // Insere os resultados.
            for (int i = 0; i < crafts; i++) {
                Players.give(player, result.copy());
            }

            compacted += crafts;
        }

        Messages.send(player, "Itens compactados: " + compacted);
    }

    private static Optional<CompactRecipe> findReversibleRecipe(
            ServerLevel level,
            ItemStack candidate
    ) {
        var recipeManager = level.getRecipeManager();

        // Prioriza receitas 3x3.
        for (int required : new int[]{9, 4}) {
            int size = required == 9 ? 3 : 2;

            var input = createInput(candidate, size);

            var recipes = recipeManager.getRecipesFor(
                    RecipeType.CRAFTING,
                    input,
                    level
            );

            for (var holder : recipes) {
                var recipe = holder.value();

                var result = recipe.assemble(
                        input,
                        level.registryAccess()
                );

                if (result.isEmpty()) continue;

                // Não permite compactar o item nele mesmo.
                if (ItemStack.isSameItemSameComponents(
                        candidate,
                        result
                )) {
                    continue;
                }

                // Simula a descompactação.
                var reverseInput = CraftingInput.of(
                        1,
                        1,
                        List.of(result.copyWithCount(1))
                );

                var reverseRecipes = recipeManager.getRecipesFor(
                        RecipeType.CRAFTING,
                        reverseInput,
                        level
                );

                boolean reversible = false;

                for (var reverseHolder : reverseRecipes) {
                    var reverseResult = reverseHolder.value().assemble(
                            reverseInput,
                            level.registryAccess()
                    );

                    if (reverseResult.isEmpty()) continue;

                    // Precisa devolver exatamente o ingrediente original.
                    if (ItemStack.isSameItemSameComponents(
                            candidate,
                            reverseResult
                    ) && reverseResult.getCount() == required) {
                        reversible = true;
                        break;
                    }
                }

                if (reversible) {
                    return Optional.of(
                            new CompactRecipe(
                                    recipe,
                                    input,
                                    result,
                                    required
                            )
                    );
                }
            }
        }

        return Optional.empty();
    }

    private static CraftingInput createInput(
            ItemStack stack,
            int size
    ) {
        int amount = size * size;

        return CraftingInput.of(
                size,
                size,
                Collections.nCopies(
                        amount,
                        stack.copyWithCount(1)
                )
        );
    }

    private static int countItems(
            Inventory inventory,
            ItemStack target
    ) {
        int total = 0;

        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            var stack = inventory.getItem(i);

            if (ItemStack.isSameItemSameComponents(target, stack)) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private static void consume(
            Inventory inventory,
            ItemStack target,
            int amount
    ) {
        for (int i = 0; i < Inventory.INVENTORY_SIZE && amount > 0; i++) {
            var stack = inventory.getItem(i);

            if (!ItemStack.isSameItemSameComponents(target, stack)) {
                continue;
            }

            int count = Math.min(amount, stack.getCount());

            inventory.removeItem(i, count);

            amount -= count;
        }
    }

    private static int getMaxCrafts(
            Inventory inventory,
            ItemStack result,
            int requested
    ) {
        int available = 0;

        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            var stack = inventory.getItem(i);

            if (stack.isEmpty()) {
                available += result.getMaxStackSize();
                continue;
            }

            if (ItemStack.isSameItemSameComponents(stack, result)) {
                available += Math.max(
                        0,
                        Math.min(
                                stack.getMaxStackSize(),
                                inventory.getMaxStackSize()
                        ) - stack.getCount()
                );
            }
        }

        int perCraft = result.getCount();

        return Math.min(requested, available / perCraft);
    }

    private record CompactRecipe(
            CraftingRecipe recipe,
            CraftingInput input,
            ItemStack result,
            int required
    ) {}
}
