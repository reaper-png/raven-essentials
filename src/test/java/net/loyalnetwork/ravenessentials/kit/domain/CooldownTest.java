package net.loyalnetwork.ravenessentials.kit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;

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

    // "Uma vez só" virou uma regra à parte (KitRules.oneTime): não é mais um cooldown.
    @ParameterizedTest
    @ValueSource(strings = {"abc", "10x", "1h banana", "", "-5", "1.5h", "uma-vez", "once"})
    void textoInvalidoERecusado(String input) {
        assertThrows(KitException.class, () -> Cooldown.parse(input));
    }

    @Test
    void segundosNegativosSaoRecusados() {
        assertThrows(KitException.class, () -> new Cooldown(-1));
    }

    @Test
    void duracao() {
        assertEquals(Duration.ofMinutes(5), Cooldown.parse("5m").duration());
        assertEquals(Duration.ZERO, Cooldown.NONE.duration());
    }

    @Test
    void disponivelDepoisDoCooldown() {
        Instant claimed = Instant.parse("2026-01-01T00:00:00Z");
        assertEquals(claimed.plusSeconds(60), new Cooldown(60).availableAfter(claimed));
        assertEquals(claimed, Cooldown.NONE.availableAfter(claimed));
    }
}
