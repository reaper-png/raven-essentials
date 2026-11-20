package net.loyalnetwork.ravenessentials.kit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KitIdTest {

    @Test
    void oQueSeDigitaENormalizado() {
        assertEquals("vip", KitId.parse("  VIP ").value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inicial", "vip-2", "semanal_pvp", "1"})
    void idsValidos(String id) {
        assertEquals(id, new KitId(id).value());
    }

    // "-" é o marcador de "sem argumento" do /kit claim: nenhum kit pode começar com ele.
    @ParameterizedTest
    @ValueSource(strings = {"-", "-vip", "_vip", "com espaco", "", "acentuação", "abcdefghijklmnopqrstuvwxyz1234567"})
    void idsInvalidos(String id) {
        assertThrows(KitException.class, () -> new KitId(id));
    }

    @Test
    void permissaoDoKit() {
        assertEquals("ravenessentials.kits.vip", new KitId("vip").permission());
    }
}
