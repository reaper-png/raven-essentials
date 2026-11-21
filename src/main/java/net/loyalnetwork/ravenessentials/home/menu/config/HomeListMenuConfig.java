package net.loyalnetwork.ravenessentials.home.menu.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.coffeelib.api.config.annotation.Key;
import net.loyalnetwork.coffeelib.api.config.annotation.Range;
import net.loyalnetwork.ravenessentials.menu.MenuAction;
import net.loyalnetwork.ravenessentials.menu.MenuItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A lista de homes ({@code /homes} e {@code /homes <jogador>}). */
public class HomeListMenuConfig {

    @Comment("Título das suas homes. {count}: quantas você tem, {limit}: quantas pode ter")
    public String title = "&8Suas homes &7({count}/{limit})";

    @Key("others-title")
    @Comment("Título das homes de outro jogador. {owner}: o dono")
    public String othersTitle = "&8Homes de {owner}";

    @Range(min = 1, max = 6)
    public int rows = 5;

    @Key("home-slots")
    @Comment("Onde as homes aparecem, em ordem. Com mais homes que posições, o menu ganha páginas.")
    public List<Integer> homeSlots = new ArrayList<>(List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34));

    @Key("home-name")
    @Comment("Nome do item de cada home. Placeholders: {home}, {world}, {x}, {y}, {z}, {public}, {owner}")
    public String homeName = "&e{home}";

    @Comment("Descrição das suas homes: botão esquerdo vai, direito abre as configurações")
    public List<String> lore = new ArrayList<>(List.of(
            "&7Mundo: &f{world}",
            "&7Posição: &f{x}, {y}, {z}",
            "&7Pública: &f{public}",
            "",
            "&aBotão esquerdo: ir",
            "&eBotão direito: configurar"));

    @Key("others-lore")
    @Comment("Descrição das homes de outro jogador")
    public List<String> othersLore = new ArrayList<>(List.of(
            "&7Mundo: &f{world}",
            "&7Posição: &f{x}, {y}, {z}",
            "",
            "&aClique para ir"));

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
