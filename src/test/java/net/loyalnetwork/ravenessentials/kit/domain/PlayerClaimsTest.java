package net.loyalnetwork.ravenessentials.kit.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerClaimsTest {

    private static final KitId VIP = new KitId("vip");
    private static final KitClaim TWICE = new KitClaim(Instant.parse("2026-01-01T00:00:00Z"), 2);

    @Test
    void semRegistroEZeroVezes() {
        assertEquals(0, PlayerClaims.NONE.times(VIP));
        assertEquals(Optional.empty(), PlayerClaims.NONE.of(VIP));
    }

    @Test
    void withGuardaEContaAsVezes() {
        PlayerClaims claims = PlayerClaims.NONE.with(VIP, TWICE);
        assertEquals(2, claims.times(VIP));
        assertEquals(Optional.of(TWICE), claims.of(VIP));
    }

    @Test
    void withoutTiraSoAqueleKit() {
        KitId daily = new KitId("diario");
        PlayerClaims claims = PlayerClaims.NONE.with(VIP, TWICE).with(daily, TWICE).without(VIP);
        assertEquals(0, claims.times(VIP));
        assertEquals(2, claims.times(daily));
    }

    @Test
    void mudancasNaoAlteramOOriginal() {
        PlayerClaims original = PlayerClaims.NONE.with(VIP, TWICE);
        original.without(VIP);
        assertEquals(2, original.times(VIP));
        assertEquals(0, PlayerClaims.NONE.times(VIP));
    }
}
