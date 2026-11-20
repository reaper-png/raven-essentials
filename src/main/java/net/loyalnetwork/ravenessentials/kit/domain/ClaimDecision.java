package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Duration;
import java.util.Optional;

/** O que as regras de um kit dizem sobre pegá-lo agora. */
public sealed interface ClaimDecision {

    /** Pode pegar: {@code claim} é o novo registro, {@code previous} o que ele substitui. */
    record Granted(KitClaim claim, Optional<KitClaim> previous) implements ClaimDecision {
    }

    /** Ainda em cooldown por {@code remaining}. */
    record OnCooldown(Duration remaining) implements ClaimDecision {
    }

    /** Kit de uma vez só, já pego. */
    record AlreadyClaimed() implements ClaimDecision {
    }
}
