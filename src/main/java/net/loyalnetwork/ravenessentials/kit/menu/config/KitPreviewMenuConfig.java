package net.loyalnetwork.ravenessentials.kit.menu.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.ravenessentials.menu.MenuAction;
import net.loyalnetwork.ravenessentials.menu.MenuItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Os itens de um kit, como o admin montou. Tem sempre 6 fileiras: as 5 de cima são os
 * itens do kit, a última é dos botões.
 */
public class KitPreviewMenuConfig {

    @Comment("{kit}: o nome do kit")
    public String title = "&8Kit {kit}";

    @Comment("Itens da última fileira (slots 45 a 53). action: NONE, CLOSE, BACK, CLAIM. "
            + "Placeholders: {kit}, {id}, {items}, {cooldown}, {uses}, {remaining}")
    public Map<String, MenuItem> items = defaults();

    private static Map<String, MenuItem> defaults() {
        Map<String, MenuItem> items = new LinkedHashMap<>();
        items.put("voltar", MenuItem.of("minecraft:arrow", "&eVoltar", MenuAction.BACK, List.of(45)));
        items.put("borda", MenuItem.of("minecraft:gray_stained_glass_pane", " ", MenuAction.NONE,
                List.of(46, 47, 48, 50, 51, 52, 53)));
        items.put("pegar", MenuItem.of("minecraft:lime_dye", "&aPegar o kit", MenuAction.CLAIM, List.of(49))
                .withLore("&7Cooldown: &f{cooldown}"));
        return items;
    }
}
