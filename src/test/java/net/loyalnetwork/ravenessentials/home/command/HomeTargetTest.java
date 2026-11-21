package net.loyalnetwork.ravenessentials.home.command;

import net.loyalnetwork.ravenessentials.home.domain.HomeException;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HomeTargetTest {

    @Test
    void nomeSozinhoEUmaHomeSua() {
        assertEquals(new HomeTarget.Own(new HomeName("casa")), HomeTarget.parse("casa"));
    }

    @Test
    void donoENomeEAHomeDeOutro() {
        assertEquals(new HomeTarget.Other("Renan", new HomeName("casa")), HomeTarget.parse("Renan:casa"));
    }

    @Test
    void donoSemNomeEAHomeDele() {
        assertEquals(new HomeTarget.Other("Renan", HomeName.DEFAULT), HomeTarget.parse("Renan:"));
    }

    @Test
    void nomeInvalidoFalha() {
        assertThrows(HomeException.class, () -> HomeTarget.parse("Renan:com espaco"));
        assertThrows(HomeException.class, () -> HomeTarget.parse("com espaco"));
    }
}
