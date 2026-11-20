package net.loyalnetwork.ravenessentials.kit.domain;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;

/**
 * Como um kit aparece nos menus: o nome (com códigos de cor {@code &}) e o ícone.
 * O ícone é sempre uma cópia, para ninguém mudar o kit por fora.
 */
public final class KitAppearance {

    private static final int MAX_NAME = 64;

    private final String name;
    private final ItemStack icon;

    public KitAppearance(String name, ItemStack icon) {
        this.name = requireName(name);
        this.icon = requireIcon(icon).copyWithCount(1);
    }

    static KitAppearance defaultFor(KitId id) {
        return new KitAppearance(id.value(), new ItemStack(Items.CHEST));
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.length() > MAX_NAME) {
            throw new KitException("O nome do kit precisa ter de 1 a " + MAX_NAME + " caracteres.");
        }
        return name;
    }

    private static ItemStack requireIcon(ItemStack icon) {
        Objects.requireNonNull(icon, "icon");
        if (icon.isEmpty()) {
            throw new KitException("O ícone do kit não pode ser vazio: segure um item.");
        }
        return icon;
    }

    public String name() {
        return name;
    }

    public ItemStack icon() {
        return icon.copy();
    }

    KitAppearance rename(String newName) {
        return new KitAppearance(newName, icon);
    }

    KitAppearance withIcon(ItemStack newIcon) {
        return new KitAppearance(name, newIcon);
    }
}
