package net.loyalnetwork.ravenessentials.home.domain;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerHomesTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final CoffeeLocation SPAWN = at(0, 64, 0);
    private static final CoffeeLocation FARM = at(100, 70, -20);
    private static final HomeName CASA = new HomeName("casa");
    private static final HomeName MINA = new HomeName("mina");

    private static CoffeeLocation at(double x, double y, double z) {
        return new CoffeeLocation("minecraft:overworld", x, y, z, 0, 0, NOW);
    }

    private static PlayerHomes homes(HomeName... names) {
        PlayerHomes homes = PlayerHomes.NONE;
        for (HomeName name : names) {
            homes = homes.with(new Home(name, SPAWN, Optional.empty(), false));
        }
        return homes;
    }

    @Test
    void primeiraHomeECriada() {
        var created = assertInstanceOf(SetHome.Created.class, PlayerHomes.NONE.set(CASA, SPAWN, new HomeLimit(3)));
        assertEquals(CASA, created.home().name());
        assertEquals(SPAWN, created.home().location());
        assertFalse(created.home().isPublic(), "home nova é particular");
    }

    @Test
    void noLimiteNaoCriaOutra() {
        var reached = assertInstanceOf(SetHome.LimitReached.class, homes(CASA).set(MINA, SPAWN, new HomeLimit(1)));
        assertEquals(new HomeLimit(1), reached.limit());
    }

    // No limite, um /sethome com o nome de uma que já existe só muda de lugar.
    @Test
    void moverNaoContaParaOLimite() {
        PlayerHomes full = homes(CASA).with(homes(CASA).require(CASA).withIcon("minecraft:diamond").togglePublic());
        var moved = assertInstanceOf(SetHome.Moved.class, full.set(CASA, FARM, new HomeLimit(1)));
        assertEquals(FARM, moved.home().location());
        assertEquals(Optional.of("minecraft:diamond"), moved.home().icon(), "o ícone continua");
        assertTrue(moved.home().isPublic(), "continua pública");
    }

    @Test
    void semLimiteSempreCria() {
        assertInstanceOf(SetHome.Created.class, homes(CASA, MINA).set(new HomeName("base"), SPAWN, HomeLimit.UNLIMITED));
    }

    @Test
    void limiteZeroNaoDeixaCriarNenhuma() {
        assertInstanceOf(SetHome.LimitReached.class, PlayerHomes.NONE.set(CASA, SPAWN, new HomeLimit(0)));
    }

    @Test
    void renomearTrocaONome() {
        Home renamed = homes(CASA).rename(CASA, MINA);
        assertEquals(MINA, renamed.name());
        assertEquals(SPAWN, renamed.location());
    }

    @Test
    void renomearParaUmNomeQueJaExisteFalha() {
        assertThrows(HomeException.class, () -> homes(CASA, MINA).rename(CASA, MINA));
    }

    @Test
    void renomearUmaQueNaoExisteFalha() {
        assertThrows(HomeException.class, () -> homes(CASA).rename(MINA, new HomeName("base")));
    }

    @Test
    void ficamEmOrdemDeNome() {
        List<String> names = homes(MINA, CASA, new HomeName("base")).all().stream()
                .map(home -> home.name().value()).toList();
        assertEquals(List.of("base", "casa", "mina"), names);
    }

    @Test
    void withSubstituiAMesmaEWithoutTira() {
        PlayerHomes homes = homes(CASA, MINA);
        assertEquals(2, homes.with(new Home(CASA, FARM, Optional.empty(), true)).count());
        assertEquals(FARM, homes.with(new Home(CASA, FARM, Optional.empty(), true)).require(CASA).location());
        assertEquals(1, homes.without(CASA).count());
        assertEquals(2, homes.count(), "o original não muda");
    }

    @Test
    void particularSoComBypass() {
        Home privateHome = new Home(CASA, SPAWN, Optional.empty(), false);
        assertFalse(privateHome.visitableBy(false));
        assertTrue(privateHome.visitableBy(true));
        assertTrue(privateHome.togglePublic().visitableBy(false));
    }
}
