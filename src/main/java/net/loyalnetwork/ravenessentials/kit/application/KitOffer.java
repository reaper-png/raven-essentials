package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;

/** Um kit como um jogador o vê agora: o kit e se ele pode pegar. */
public record KitOffer(Kit kit, KitStatus status) {

    public boolean isLocked() {
        return status instanceof KitStatus.Locked;
    }
}
