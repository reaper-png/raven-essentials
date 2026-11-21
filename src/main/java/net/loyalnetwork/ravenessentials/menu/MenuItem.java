package net.loyalnetwork.ravenessentials.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Um item de menu na config: o que é, o texto, onde fica e o que faz.
 * Os textos aceitam {@code &} e {@code &#RRGGBB}, e os placeholders do menu.
 */
public class MenuItem {

    public String material = "minecraft:stone";
    public String name = "";
    public List<String> lore = new ArrayList<>();
    public boolean glow = false;
    public List<Integer> slots = new ArrayList<>();
    public MenuAction action = MenuAction.NONE;

    public MenuItem() {
    }

    public static MenuItem of(String material, String name, MenuAction action, List<Integer> slots) {
        MenuItem item = new MenuItem();
        item.material = material;
        item.name = name;
        item.action = action;
        item.slots = new ArrayList<>(slots);
        return item;
    }

    public MenuItem withLore(String... lines) {
        this.lore = new ArrayList<>(List.of(lines));
        return this;
    }
}
