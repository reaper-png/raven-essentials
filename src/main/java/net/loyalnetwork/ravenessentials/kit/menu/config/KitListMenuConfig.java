package net.loyalnetwork.ravenessentials.kit.menu.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.coffeelib.api.config.annotation.Key;
import net.loyalnetwork.coffeelib.api.config.annotation.Range;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A lista de kits ({@code /kit view} e {@code /kit claim}). */
public class KitListMenuConfig {

    public String title = "&8Kits";

    @Range(min = 1, max = 6)
    public int rows = 5;

    @Key("kit-slots")
    @Comment("Onde os kits aparecem, em ordem. Com mais kits que posições, o menu ganha páginas.")
    public List<Integer> kitSlots = new ArrayList<>(List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34));

    @Key("hide-locked")
    @Comment("Esconde os kits restritos de quem não tem a permissão (em vez de mostrar bloqueados)")
    public boolean hideLocked = false;

    @Key("kit-name")
    @Comment("Nome do item de cada kit. {kit}: o nome do kit")
    public String kitName = "{kit}";

    @Comment("Placeholders: {kit}, {items}, {cooldown}, {remaining}, {action}")
    public KitLore lore = new KitLore();

    @Key("view-hint")
    @Comment("O {action} no /kit view")
    public String viewHint = "&eClique para ver os itens";

    @Key("claim-hint")
    @Comment("O {action} no /kit claim")
    public String claimHint = "&aClique para pegar";

    @Comment("Itens fixos. action: NONE, CLOSE, PREVIOUS_PAGE, NEXT_PAGE (as de página só aparecem se houver página)")
    public Map<String, MenuItem> items = defaults();

    private static Map<String, MenuItem> defaults() {
        Map<String, MenuItem> items = new LinkedHashMap<>();
        items.put("borda", MenuItem.of("minecraft:gray_stained_glass_pane", " ", MenuAction.NONE, List.of(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 42, 43, 44)));
        items.put("anterior", MenuItem.of("minecraft:arrow", "&ePágina anterior", MenuAction.PREVIOUS_PAGE,
                List.of(39)));
        items.put("fechar", MenuItem.of("minecraft:barrier", "&cFechar", MenuAction.CLOSE, List.of(40)));
        items.put("proxima", MenuItem.of("minecraft:arrow", "&ePróxima página", MenuAction.NEXT_PAGE,
                List.of(41)));
        return items;
    }
}
