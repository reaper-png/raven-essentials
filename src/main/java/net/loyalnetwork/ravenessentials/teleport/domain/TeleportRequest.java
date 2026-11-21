package net.loyalnetwork.ravenessentials.teleport.domain;

import java.util.Objects;
import java.util.UUID;

/** Um pedido de teleporte pendente: quem pediu e quem pode aceitar. Imutável. */
public record TeleportRequest(UUID requester, String requesterName, UUID target, String targetName) {

    public TeleportRequest {
        Objects.requireNonNull(requester, "requester");
        Objects.requireNonNull(requesterName, "requesterName");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(targetName, "targetName");
    }
}
