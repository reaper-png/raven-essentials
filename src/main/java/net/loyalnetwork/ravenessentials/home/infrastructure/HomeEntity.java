package net.loyalnetwork.ravenessentials.home.infrastructure;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Optional;

/**
 * Uma {@link Home} como linha da tabela {@code homes}. O {@code owner_id} é chave
 * estrangeira para {@code players}: apagar o jogador apaga as homes dele.
 */
@Entity
@Table(name = "homes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeEntity {

    @EmbeddedId
    private HomeKey key;

    @MapsId("ownerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_homes_owner"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerEntity owner;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "world", column = @Column(name = "dimension", nullable = false)),
            @AttributeOverride(name = "x", column = @Column(name = "x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "y", nullable = false)),
            @AttributeOverride(name = "z", column = @Column(name = "z", nullable = false)),
            @AttributeOverride(name = "yaw", column = @Column(name = "yaw", nullable = false)),
            @AttributeOverride(name = "pitch", column = @Column(name = "pitch", nullable = false)),
            @AttributeOverride(name = "savedAt", column = @Column(name = "saved_at")),
    })
    private CoffeeLocation location;

    // Id do item do ícone; null usa o padrão da config.
    @Column(length = 128)
    private String icon;

    // "public" é palavra reservada em alguns bancos.
    @Column(name = "public_home", nullable = false)
    private boolean publicHome;

    HomeEntity(HomeKey key, PlayerEntity owner, Home home) {
        this.key = key;
        this.owner = owner;
        update(home);
    }

    /** Copia lugar, ícone e se é pública (o nome é a chave e não muda aqui). */
    void update(Home home) {
        this.location = home.location();
        this.icon = home.icon().orElse(null);
        this.publicHome = home.isPublic();
    }

    Home toHome() {
        return new Home(new HomeName(key.name()), location, Optional.ofNullable(icon), publicHome);
    }
}
