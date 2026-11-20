package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.coffeelib.api.menu.Menu;
import net.loyalnetwork.coffeelib.api.text.TextFormatter;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.kit.menu.config.MenuAction;
import net.loyalnetwork.ravenessentials.kit.menu.config.MenuItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

/** Monta os itens dos menus: os da config ({@link MenuItem}) e os ícones com nome e descrição. */
public final class MenuItems {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");
    private static final TextFormatter<net.minecraft.network.chat.MutableComponent> TEXT =
            CoffeeLib.text(RavenEssentials.MODID);

    private MenuItems() {
    }

    /**
     * Põe no menu os itens da config cuja ação {@code shown} aceita, nos slots que cabem
     * em {@code size}; o clique chama {@code onClick} com a ação do item.
     */
    public static void place(Menu<ItemStack, ServerPlayer> menu, Map<String, MenuItem> items, int size,
                             Placeholders values, ServerPlayer viewer,
                             Predicate<MenuAction> shown, Consumer<MenuAction> onClick) {
        for (MenuItem item : items.values()) {
            if (shown.test(item.action)) {
                placeOne(menu, item, size, values, viewer, onClick);
            }
        }
    }

    private static void placeOne(Menu<ItemStack, ServerPlayer> menu, MenuItem item, int size,
                                 Placeholders values, ServerPlayer viewer, Consumer<MenuAction> onClick) {
        ItemStack stack = render(item, values, viewer);
        for (int slot : item.slots) {
            if (slot >= 0 && slot < size) {
                menu.item(slot, stack, click -> onClick.accept(item.action));
            }
        }
    }

    public static ItemStack render(MenuItem item, Placeholders values, ServerPlayer viewer) {
        ItemStack stack = dress(new ItemStack(material(item.material)), item.name, item.lore, values, viewer);
        if (item.glow) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return stack;
    }

    /** Uma cópia de {@code base} com nome e descrição; nome vazio mantém o do item. */
    public static ItemStack dress(ItemStack base, String name, List<String> lore, Placeholders values,
                                  ServerPlayer viewer) {
        ItemStack stack = base.copy();
        if (!name.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, line(name, values, viewer));
        }
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore.stream().map(text -> line(text, values, viewer)).toList()));
        }
        return stack;
    }

    // Nome e descrição de item saem em itálico por padrão: desligado, para o texto sair como na config.
    private static Component line(String text, Placeholders values, ServerPlayer viewer) {
        return TEXT.parse(values.apply(text), viewer).withStyle(style -> style.withItalic(false));
    }

    private static Item material(String id) {
        Optional<Item> item = Optional.ofNullable(ResourceLocation.tryParse(id))
                .flatMap(BuiltInRegistries.ITEM::getOptional);
        if (item.isEmpty() || item.get() == Items.AIR) {
            LOGGER.warning("Material inválido em kits.yml: " + id + " (usando barreira)");
            return Items.BARRIER;
        }
        return item.get();
    }
}
