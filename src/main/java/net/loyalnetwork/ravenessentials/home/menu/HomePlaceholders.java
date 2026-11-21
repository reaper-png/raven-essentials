package net.loyalnetwork.ravenessentials.home.menu;

import net.loyalnetwork.coffeelib.api.placeholder.annotation.Context;
import net.loyalnetwork.coffeelib.api.placeholder.annotation.Placeholder;
import net.loyalnetwork.ravenessentials.home.application.HomeService;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeException;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Function;

/**
 * Os placeholders de home na CoffeeLib, no namespace {@code ravenessentials}.
 * <ul>
 *   <li>Do jogador: {@code %ravenessentials_homes_count%}, {@code _limit}, {@code _list}, e com
 *       o nome de uma home dele no fim: {@code %ravenessentials_homes_world_<home>%},
 *       {@code _x}, {@code _y}, {@code _z}, {@code _public}.</li>
 *   <li>Singular, sobre a home do texto (o {@code TextContext}: o menu, as mensagens):
 *       {@code %ravenessentials_home_name%}, {@code _world}, {@code _x}, {@code _y}, {@code _z},
 *       {@code _public}.</li>
 * </ul>
 * Respondem pelo que está em memória: logo depois de entrar, ou quando o cache expirou, ficam
 * como estão escritos até as homes carregarem (nunca "0" ou "nenhuma" de mentira).
 */
public final class HomePlaceholders {

    private final HomeService homes;

    public HomePlaceholders(HomeService homes) {
        this.homes = homes;
    }

    // ---- do jogador ----

    // Antes de as homes carregarem (logo depois de entrar, ou depois de o cache expirar), ficam
    // como estão escritos: "0" seria mentira para quem tem homes.
    @Placeholder("homes_count")
    Integer count(ServerPlayer player) {
        return homes.loadedHomes(player).map(PlayerHomes::count).orElse(null);
    }

    @Placeholder("homes_limit")
    String limit(ServerPlayer player) {
        return HomeTexts.limit(homes.limit(player));
    }

    @Placeholder("homes_list")
    String list(ServerPlayer player) {
        return homes.loadedHomes(player).map(HomeTexts::list).orElse(null);
    }

    @Placeholder("homes_world")
    String world(ServerPlayer player, String home) {
        return byName(player, home, HomeTexts::world);
    }

    @Placeholder("homes_x")
    String x(ServerPlayer player, String home) {
        return byName(player, home, found -> HomeTexts.coordinate(found.location().x()));
    }

    @Placeholder("homes_y")
    String y(ServerPlayer player, String home) {
        return byName(player, home, found -> HomeTexts.coordinate(found.location().y()));
    }

    @Placeholder("homes_z")
    String z(ServerPlayer player, String home) {
        return byName(player, home, found -> HomeTexts.coordinate(found.location().z()));
    }

    @Placeholder("homes_public")
    String visibility(ServerPlayer player, String home) {
        return byName(player, home, HomeTexts::visibility);
    }

    // ---- singular: a home do texto ----

    @Placeholder("home_name")
    String currentName(@Context Home home) {
        return home.name().value();
    }

    @Placeholder("home_world")
    String currentWorld(@Context Home home) {
        return HomeTexts.world(home);
    }

    @Placeholder("home_x")
    String currentX(@Context Home home) {
        return HomeTexts.coordinate(home.location().x());
    }

    @Placeholder("home_y")
    String currentY(@Context Home home) {
        return HomeTexts.coordinate(home.location().y());
    }

    @Placeholder("home_z")
    String currentZ(@Context Home home) {
        return HomeTexts.coordinate(home.location().z());
    }

    @Placeholder("home_public")
    String currentVisibility(@Context Home home) {
        return HomeTexts.visibility(home);
    }

    // O argumento vem do texto: um nome inválido, ou de uma home que o jogador não tem, fica como está.
    private String byName(ServerPlayer player, String home, Function<Home, String> value) {
        return parse(home).flatMap(name -> homes.loadedHomes(player).flatMap(found -> found.find(name)))
                .map(value).orElse(null);
    }

    private static Optional<HomeName> parse(String home) {
        try {
            return Optional.of(HomeName.parse(home));
        } catch (HomeException e) {
            return Optional.empty();
        }
    }
}
