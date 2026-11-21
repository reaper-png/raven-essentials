package net.loyalnetwork.ravenessentials.home.menu;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeTextsTest {

    private static Home home(String name, String world, double x, double y, double z) {
        return new Home(new HomeName(name), new CoffeeLocation(world, x, y, z, 0, 0, Instant.EPOCH),
                Optional.empty(), false);
    }

    @Test
    void placeholdersDaHome() {
        var values = HomeTexts.of(home("casa", "minecraft:the_nether", 10.7, 64.2, -3.5));
        assertEquals("casa the_nether 10 64 -4 não", values.apply("{home} {world} {x} {y} {z} {public}"));
        assertEquals(true, values.context().get(Home.class).isPresent(), "a home vai no contexto");
    }

    @Test
    void mundoDeModFicaComONamespace() {
        assertEquals("twilightforest:twilight_forest",
                HomeTexts.world(home("casa", "twilightforest:twilight_forest", 0, 0, 0)));
    }

    @Test
    void placeholdersDoJogador() {
        PlayerHomes homes = PlayerHomes.NONE.with(home("mina", "minecraft:overworld", 0, 0, 0))
                .with(home("casa", "minecraft:overworld", 0, 0, 0));
        assertEquals("2/3 casa, mina", HomeTexts.of(homes, new HomeLimit(3)).apply("{count}/{limit} {homes}"));
        assertEquals("0/sem limite nenhuma",
                HomeTexts.of(PlayerHomes.NONE, HomeLimit.UNLIMITED).apply("{count}/{limit} {homes}"));
    }
}
