package net.loyalnetwork.ravenessentials.home.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Chave de {@code homes}: um nome por dono. O {@code ownerId} é preenchido pelo
 * {@code @MapsId} de {@link HomeEntity}, que define a coluna.
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HomeKey implements Serializable {

    private String ownerId;

    @Column(name = "name", length = 16, nullable = false)
    private String name;

    String ownerId() {
        return ownerId;
    }

    String name() {
        return name;
    }
}
