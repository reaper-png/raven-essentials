package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Duration;

/** Se um jogador pode pegar um kit agora, e se não, por quê. */
public sealed interface KitStatus {

    record Available() implements KitStatus {
    }

    /** Kit restrito, sem a permissão. */
    record Locked() implements KitStatus {
    }

    record OnCooldown(Duration remaining) implements KitStatus {
    }

    /** Kit de uma vez só, já pego. */
    record Claimed() implements KitStatus {
    }
}
