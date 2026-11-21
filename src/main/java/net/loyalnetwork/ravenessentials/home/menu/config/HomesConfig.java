package net.loyalnetwork.ravenessentials.home.menu.config;

import net.loyalnetwork.coffeelib.api.config.ConfigFormat;
import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.coffeelib.api.config.annotation.ConfigFile;
import net.loyalnetwork.coffeelib.api.config.annotation.Key;
import net.loyalnetwork.coffeelib.api.config.annotation.Range;

/**
 * {@code raven/essentials/homes.yml}: o limite padrão e como as homes aparecem. As homes em
 * si ficam no banco. Recarregar: {@code /coffeelib config reload ravenessentials}.
 */
@ConfigFile(value = "homes", format = ConfigFormat.YAML)
public class HomesConfig {

    @Key("default-limit")
    @Range(min = 0, max = 100)
    @Comment("Quantas homes cada um pode ter sem permissão de limite. Mais: ravenessentials.homes.limit.<N> "
            + "(ou o meta ravenessentials.homes.limit no LuckPerms); sem limite: ravenessentials.homes.unlimited")
    public int defaultLimit = 3;

    @Key("default-icon")
    @Comment("Ícone de uma home que ainda não ganhou um (no menu da home: Ícone, com o item na mão)")
    public String defaultIcon = "minecraft:red_bed";

    @Comment("Lista de homes (/homes e /homes <jogador>)")
    public HomeListMenuConfig menu = new HomeListMenuConfig();

    @Comment("Placeholders da CoffeeLib, em qualquer texto: %ravenessentials_homes_count%, "
            + "%ravenessentials_homes_limit%, %ravenessentials_homes_list%, e com o nome da home no fim: "
            + "world, x, y, z, public (ex.: %ravenessentials_homes_world_casa%). No singular, sobre a home do "
            + "texto (menu, mensagens): %ravenessentials_home_name%, _world, _x, _y, _z, _public")
    public HomeMessages messages = new HomeMessages();
}
