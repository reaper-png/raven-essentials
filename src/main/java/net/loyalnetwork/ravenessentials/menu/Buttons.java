package net.loyalnetwork.ravenessentials.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Botões fixos de menu (sem config): um item com nome e descrição, aceitando {@code &}. */
public final class Buttons {

    private Buttons() {
    }

    public static ItemStack button(ServerPlayer viewer, Item item, String name, String... lore) {
        return MenuItems.dress(new ItemStack(item), name, List.of(lore), Placeholders.NONE, viewer);
    }
}
