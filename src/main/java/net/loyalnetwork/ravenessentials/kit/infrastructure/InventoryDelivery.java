package net.loyalnetwork.ravenessentials.kit.infrastructure;

import net.loyalnetwork.ravenessentials.kit.application.KitDelivery;
import net.loyalnetwork.ravenessentials.kit.domain.KitItems;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/** Entrega no inventário do jogador, seguindo o {@link FullInventory} da config. */
public final class InventoryDelivery implements KitDelivery {

    private final Supplier<FullInventory> whenFull;

    public InventoryDelivery(Supplier<FullInventory> whenFull) {
        this.whenFull = whenFull;
    }

    // Conta só espaços vazios (não junta com pilhas parecidas): pode pedir espaço a mais, nunca a menos.
    @Override
    public boolean accepts(ServerPlayer player, KitItems items) {
        return whenFull.get() == FullInventory.DROP || freeSlots(player) >= items.count();
    }

    @Override
    public void deliver(ServerPlayer player, KitItems items) {
        for (ItemStack stack : items.stacks()) {
            Players.give(player, stack);
        }
    }

    public static int freeSlots(ServerPlayer player) {
        return (int) player.getInventory().items.stream().filter(ItemStack::isEmpty).count();
    }
}
