package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Instant;
import java.util.Objects;

/** A última vez que um jogador pegou um kit, e quantas vezes já pegou. */
public record KitClaim(Instant lastClaim, int times) {

    public KitClaim {
        Objects.requireNonNull(lastClaim, "lastClaim");
        if (times < 1) {
            throw new IllegalArgumentException("times must be positive: " + times);
        }
    }

    static KitClaim first(Instant now) {
        return new KitClaim(now, 1);
    }

    KitClaim next(Instant now) {
        return new KitClaim(now, times + 1);
    }
}
