package net.loyalnetwork.ravenessentials.kit.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Os registros de um jogador, um por kit que ele já pegou. Imutável. */
public final class PlayerClaims {

    public static final PlayerClaims NONE = new PlayerClaims(Map.of());

    private final Map<KitId, KitClaim> claims;

    public PlayerClaims(Map<KitId, KitClaim> claims) {
        this.claims = Map.copyOf(claims);
    }

    public Optional<KitClaim> of(KitId kit) {
        return Optional.ofNullable(claims.get(kit));
    }

    /** Quantas vezes pegou o kit; zero se nunca. */
    public int times(KitId kit) {
        return of(kit).map(KitClaim::times).orElse(0);
    }

    public PlayerClaims with(KitId kit, KitClaim claim) {
        Map<KitId, KitClaim> copy = new HashMap<>(claims);
        copy.put(kit, claim);
        return new PlayerClaims(copy);
    }

    public PlayerClaims without(KitId kit) {
        Map<KitId, KitClaim> copy = new HashMap<>(claims);
        copy.remove(kit);
        return new PlayerClaims(copy);
    }
}
