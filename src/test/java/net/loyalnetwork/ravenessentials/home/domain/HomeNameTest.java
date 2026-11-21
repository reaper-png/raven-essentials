package net.loyalnetwork.ravenessentials.home.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeNameTest {

    @Test
    void oQueSeDigitaENormalizado() {
        assertEquals("casa", HomeName.parse(" CASA ").value());
    }

    @Test
    void semNomeEHome() {
        assertEquals("home", HomeName.DEFAULT.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"casa", "mina-2", "base_pvp", "1"})
    void nomesValidos(String name) {
        assertEquals(name, new HomeName(name).value());
    }

    // ":" separa o dono no /home dono:casa, então não pode fazer parte do nome.
    @ParameterizedTest
    @ValueSource(strings = {"", "-", "com espaco", "dono:casa", "ção", "abcdefghijklmnopq"})
    void nomesInvalidos(String name) {
        assertThrows(HomeException.class, () -> new HomeName(name));
    }

    @Test
    void limite() {
        assertTrue(new HomeLimit(2).allowsAnother(1));
        assertFalse(new HomeLimit(2).allowsAnother(2));
        assertTrue(HomeLimit.UNLIMITED.allowsAnother(1_000));
        assertThrows(IllegalArgumentException.class, () -> new HomeLimit(-2));
    }
}
