package net.loyalnetwork.ravenessentials.kit.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitRulesTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final KitClaim FIRST = new KitClaim(T0, 1);
    private static final KitRules HOURLY = new KitRules(Cooldown.parse("1h"), false);

    @Test
    void primeiraVezPode() {
        var granted = assertInstanceOf(ClaimDecision.Granted.class, HOURLY.decide(Optional.empty(), T0));
        assertEquals(new KitClaim(T0, 1), granted.claim());
        assertTrue(granted.previous().isEmpty());
    }

    @Test
    void antesDoCooldownDizQuantoFalta() {
        var cooldown = assertInstanceOf(ClaimDecision.OnCooldown.class,
                HOURLY.decide(Optional.of(FIRST), T0.plusSeconds(1800)));
        assertEquals(Duration.ofMinutes(30), cooldown.remaining());
    }

    @Test
    void depoisDoCooldownPodeEContaAVez() {
        Instant later = T0.plusSeconds(3600);
        var granted = assertInstanceOf(ClaimDecision.Granted.class, HOURLY.decide(Optional.of(FIRST), later));
        assertEquals(new KitClaim(later, 2), granted.claim());
        assertEquals(Optional.of(FIRST), granted.previous(), "o anterior fica para desfazer uma entrega");
    }

    @Test
    void umaVezSoNuncaMais() {
        KitRules once = new KitRules(Cooldown.ONCE, false);
        assertInstanceOf(ClaimDecision.AlreadyClaimed.class,
                once.decide(Optional.of(FIRST), T0.plus(Duration.ofDays(3650))));
    }

    @Test
    void semCooldownSemprePode() {
        assertInstanceOf(ClaimDecision.Granted.class, KitRules.FREE.decide(Optional.of(FIRST), T0));
    }

    @Test
    void mudancasDevolvemRegrasNovas() {
        KitRules restricted = KitRules.FREE.toggleRestriction();
        assertTrue(restricted.restricted());
        assertFalse(KitRules.FREE.restricted(), "a original não muda");
        assertEquals(Cooldown.ONCE, restricted.withCooldown(Cooldown.ONCE).cooldown());
        assertTrue(restricted.withCooldown(Cooldown.ONCE).restricted());
    }

    @Test
    void registroPrecisaDeUmaVezPeloMenos() {
        assertThrows(IllegalArgumentException.class, () -> new KitClaim(T0, 0));
    }
}
