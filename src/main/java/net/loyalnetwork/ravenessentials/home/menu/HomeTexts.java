package net.loyalnetwork.ravenessentials.home.menu;

import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.loyalnetwork.ravenessentials.menu.Placeholders;

import java.util.stream.Collectors;

/** Homes, limites e posições como texto para o jogador. */
public final class HomeTexts {

    private static final String VANILLA = "minecraft:";

    private HomeTexts() {
    }

    /** {home}, {world}, {x}, {y}, {z} e {public} de uma home, com ela no contexto (para os {@code %ravenessentials_home_*%}). */
    public static Placeholders of(Home home) {
        return Placeholders.NONE.about(home)
                .with("home", home.name().value())
                .with("world", world(home))
                .with("x", coordinate(home.location().x()))
                .with("y", coordinate(home.location().y()))
                .with("z", coordinate(home.location().z()))
                .with("public", visibility(home));
    }

    /** {count}, {limit} e {homes} das homes de um jogador. */
    public static Placeholders of(PlayerHomes homes, HomeLimit limit) {
        return Placeholders.NONE
                .with("count", String.valueOf(homes.count()))
                .with("limit", limit(limit))
                .with("homes", list(homes));
    }

    /** A dimensão sem o {@code minecraft:} das vanilla ({@code overworld}, {@code the_nether}). */
    public static String world(Home home) {
        String dimension = home.location().world();
        return dimension.startsWith(VANILLA) ? dimension.substring(VANILLA.length()) : dimension;
    }

    public static String coordinate(double value) {
        return String.valueOf((long) Math.floor(value));
    }

    public static String visibility(Home home) {
        return home.isPublic() ? "sim" : "não";
    }

    public static String limit(HomeLimit limit) {
        return limit.isUnlimited() ? "sem limite" : String.valueOf(limit.max());
    }

    public static String list(PlayerHomes homes) {
        if (homes.count() == 0) {
            return "nenhuma";
        }
        return homes.all().stream().map(home -> home.name().value()).collect(Collectors.joining(", "));
    }
}
