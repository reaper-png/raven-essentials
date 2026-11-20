package net.loyalnetwork.ravenessentials.database;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Primeira e última vez que algo foi visto (um IP, um nome). Valor das coleções do {@link PlayerEntity}. */
@Embeddable
@Getter
@NoArgsConstructor
public class Usage {

    private Instant firstSeen;
    private Instant lastSeen;

    public Usage(Instant now) {
        this.firstSeen = now;
        this.lastSeen = now;
    }

    public void seen(Instant now) {
        this.lastSeen = now;
    }
}
