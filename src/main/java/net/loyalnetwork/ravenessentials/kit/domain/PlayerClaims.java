package net.loyalnetwork.ravenessentials.kit.domain;

import java.util.Map;
import java.util.Optional;

/** Os registros de um jogador, um por kit que ele já pegou. */
public final class PlayerClaims {

    public static final PlayerClaims NONE = new PlayerClaims(Map.of());

    private final Map<KitId, KitClaim> claims;

    public PlayerClaims(Map<KitId, KitClaim> claims) {
        this.claims = Map.copyOf(claims);
    }

    public Optional<KitClaim> of(KitId kit) {
        return Optional.ofNullable(claims.get(kit));
    }
}
