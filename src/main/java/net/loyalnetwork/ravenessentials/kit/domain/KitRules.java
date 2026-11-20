package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Quem pode pegar um kit e quando: o cooldown, se é restrito e se é de uma vez só.
 * <p>
 * Uma vez só e cooldown valem juntos e independentes: enquanto o kit é de uma vez só, quem
 * já pegou não pega mais; se deixar de ser, quem já pegou volta a poder depois do cooldown,
 * contado da última vez que pegou.
 */
public record KitRules(Cooldown cooldown, boolean restricted, boolean oneTime) {

    public static final KitRules FREE = new KitRules(Cooldown.NONE, false, false);

    public KitRules {
        Objects.requireNonNull(cooldown, "cooldown");
    }

    KitRules withCooldown(Cooldown newCooldown) {
        return new KitRules(newCooldown, restricted, oneTime);
    }

    KitRules toggleRestriction() {
        return new KitRules(cooldown, !restricted, oneTime);
    }

    KitRules toggleOneTime() {
        return new KitRules(cooldown, restricted, !oneTime);
    }

    ClaimDecision decide(Optional<KitClaim> previous, Instant now) {
        return previous
                .map(last -> decideAfter(last, now))
                .orElseGet(() -> new ClaimDecision.Granted(KitClaim.first(now), Optional.empty()));
    }

    private ClaimDecision decideAfter(KitClaim last, Instant now) {
        if (oneTime) {
            return new ClaimDecision.AlreadyClaimed();
        }
        Instant availableAt = cooldown.availableAfter(last.lastClaim());
        if (availableAt.isAfter(now)) {
            return new ClaimDecision.OnCooldown(Duration.between(now, availableAt));
        }
        return new ClaimDecision.Granted(last.next(now), Optional.of(last));
    }
}
