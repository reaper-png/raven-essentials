package net.loyalnetwork.ravenessentials.kit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownTest {

    @ParameterizedTest
    @ValueSource(strings = {"0", "nenhum", "none", " NENHUM "})
    void semCooldown(String input) {
        assertTrue(Cooldown.parse(input).isNone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"uma-vez", "uma vez", "once", "UMA-VEZ"})
    void umaVezSo(String input) {
        assertTrue(Cooldown.parse(input).isOnce());
    }

    @ParameterizedTest
    @CsvSource({
            "90, 90",
            "30m, 1800",
            "1d 12h, 129600",
            "1h30m10s, 5410",
            "2d, 172800",
            "45 s, 45"
    })
    void tempo(String input, long seconds) {
        assertEquals(seconds, Cooldown.parse(input).seconds());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "10x", "1h banana", "", "-5", "1.5h"})
    void textoInvalidoERecusado(String input) {
        assertThrows(KitException.class, () -> Cooldown.parse(input));
    }

    @Test
    void segundosNegativosAlemDeUmaVezSaoRecusados() {
        assertThrows(KitException.class, () -> new Cooldown(-2));
    }

    @Test
    void duracaoDeUmaVezSoEZero() {
        assertEquals(Duration.ZERO, Cooldown.ONCE.duration());
        assertEquals(Duration.ofMinutes(5), Cooldown.parse("5m").duration());
    }

    @Test
    void disponivelDepoisDoCooldown() {
        Instant claimed = Instant.parse("2026-01-01T00:00:00Z");
        assertEquals(Optional.of(claimed.plusSeconds(60)), new Cooldown(60).availableAfter(claimed));
        assertEquals(Optional.empty(), Cooldown.ONCE.availableAfter(claimed));
    }
}
