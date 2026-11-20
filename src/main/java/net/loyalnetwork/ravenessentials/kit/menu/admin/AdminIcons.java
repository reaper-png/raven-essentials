package net.loyalnetwork.ravenessentials.kit.menu.admin;

import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.menu.KitTexts;
import net.loyalnetwork.ravenessentials.kit.menu.MenuItems;
import net.loyalnetwork.ravenessentials.kit.menu.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Os ícones dos menus de admin (fixos, sem config). */
final class AdminIcons {

    private AdminIcons() {
    }

    static ItemStack button(ServerPlayer viewer, Item item, String name, String... lore) {
        return MenuItems.dress(new ItemStack(item), name, List.of(lore), Placeholders.NONE, viewer);
    }

    /** O kit com o resumo das regras, e {@code hint} no fim. */
    static ItemStack kit(ServerPlayer viewer, Kit kit, String hint) {
        List<String> lore = new ArrayList<>(summary(kit));
        lore.add("");
        lore.add(hint);
        return MenuItems.dress(kit.appearance().icon(), "&f{kit}", lore, KitTexts.of(kit), viewer);
    }

    static List<String> summary(Kit kit) {
        return List.of(
                "&7Id: &f{id}",
                "&7Itens: &f{items}",
                "&7Cooldown: &f{cooldown}",
                "&7Usos: &f{uses}",
                "&7Acesso: &f" + (kit.rules().restricted() ? "só com " + kit.id().permission() : "todos"));
    }
}
