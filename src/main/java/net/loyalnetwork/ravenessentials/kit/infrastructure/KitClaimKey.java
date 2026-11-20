package net.loyalnetwork.ravenessentials.kit.infrastructure;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Chave de {@code kit_claims}: um registro por jogador e kit. Os dois campos são preenchidos
 * pelos {@code @MapsId} de {@link KitClaimEntity}, que definem as colunas.
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KitClaimKey implements Serializable {

    private String playerId;

    private String kitId;

    String playerId() {
        return playerId;
    }

    String kitId() {
        return kitId;
    }
}
