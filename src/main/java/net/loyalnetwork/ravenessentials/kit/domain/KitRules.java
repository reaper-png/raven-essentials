package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Quem pode pegar um kit e de quanto em quanto tempo. */
public record KitRules(Cooldown cooldown, boolean restricted) {

    public static final KitRules FREE = new KitRules(Cooldown.NONE, false);

    public KitRules {
        Objects.requireNonNull(cooldown, "cooldown");
    }

    KitRules withCooldown(Cooldown newCooldown) {
        return new KitRules(newCooldown, restricted);
    }

    KitRules toggleRestriction() {
        return new KitRules(cooldown, !restricted);
    }

    ClaimDecision decide(Optional<KitClaim> previous, Instant now) {
        return previous
                .map(last -> decideAfter(last, now))
                .orElseGet(() -> new ClaimDecision.Granted(KitClaim.first(now), Optional.empty()));
    }

    private ClaimDecision decideAfter(KitClaim last, Instant now) {
        Optional<Instant> availableAt = cooldown.availableAfter(last.lastClaim());
        if (availableAt.isEmpty()) {
            return new ClaimDecision.AlreadyClaimed();
        }
        if (availableAt.get().isAfter(now)) {
            return new ClaimDecision.OnCooldown(Duration.between(now, availableAt.get()));
        }
        return new ClaimDecision.Granted(last.next(now), Optional.of(last));
    }
}
