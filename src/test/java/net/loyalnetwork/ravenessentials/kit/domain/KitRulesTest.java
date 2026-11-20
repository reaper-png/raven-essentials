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
    private static final Cooldown ONE_HOUR = Cooldown.parse("1h");
    private static final KitRules HOURLY = new KitRules(ONE_HOUR, false, false);

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
    void semCooldownSemprePode() {
        assertInstanceOf(ClaimDecision.Granted.class, KitRules.FREE.decide(Optional.of(FIRST), T0));
    }

    // ---- uma vez só ----

    @Test
    void umaVezSoPodeAPrimeira() {
        KitRules once = new KitRules(Cooldown.NONE, false, true);
        assertInstanceOf(ClaimDecision.Granted.class, once.decide(Optional.empty(), T0));
    }

    @Test
    void umaVezSoNuncaMaisMesmoSemCooldown() {
        KitRules once = new KitRules(Cooldown.NONE, false, true);
        assertInstanceOf(ClaimDecision.AlreadyClaimed.class,
                once.decide(Optional.of(FIRST), T0.plus(Duration.ofDays(3650))));
    }

    @Test
    void umaVezSoGanhaDoCooldownJaPassado() {
        KitRules both = new KitRules(ONE_HOUR, false, true);
        assertInstanceOf(ClaimDecision.AlreadyClaimed.class, both.decide(Optional.of(FIRST), T0.plusSeconds(7200)));
    }

    // Quem pegou um kit de uma vez só e o kit deixou de ser: pode de novo, mas só depois do cooldown.
    @Test
    void deixarDeSerUmaVezSoVoltaAContarOCooldownDaUltimaVez() {
        KitRules once = new KitRules(ONE_HOUR, false, true);
        KitRules notAnymore = once.toggleOneTime();

        var waiting = assertInstanceOf(ClaimDecision.OnCooldown.class,
                notAnymore.decide(Optional.of(FIRST), T0.plusSeconds(600)));
        assertEquals(Duration.ofMinutes(50), waiting.remaining());

        var granted = assertInstanceOf(ClaimDecision.Granted.class,
                notAnymore.decide(Optional.of(FIRST), T0.plusSeconds(3600)));
        assertEquals(2, granted.claim().times());
    }

    @Test
    void deixarDeSerUmaVezSoSemCooldownLiberaNaHora() {
        KitRules notAnymore = new KitRules(Cooldown.NONE, false, true).toggleOneTime();
        assertInstanceOf(ClaimDecision.Granted.class, notAnymore.decide(Optional.of(FIRST), T0));
    }

    // ---- mudanças ----

    @Test
    void mudancasDevolvemRegrasNovasESoMudamOQuePediram() {
        KitRules restricted = KitRules.FREE.toggleRestriction();
        assertTrue(restricted.restricted());
        assertFalse(KitRules.FREE.restricted(), "a original não muda");

        KitRules all = restricted.withCooldown(ONE_HOUR).toggleOneTime();
        assertEquals(new KitRules(ONE_HOUR, true, true), all);
        assertEquals(new KitRules(ONE_HOUR, true, false), all.toggleOneTime());
    }

    @Test
    void registroPrecisaDeUmaVezPeloMenos() {
        assertThrows(IllegalArgumentException.class, () -> new KitClaim(T0, 0));
    }
}
