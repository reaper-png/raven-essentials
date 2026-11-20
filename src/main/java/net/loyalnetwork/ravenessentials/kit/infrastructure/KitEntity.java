package net.loyalnetwork.ravenessentials.kit.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.loyalnetwork.ravenessentials.kit.domain.Cooldown;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitAppearance;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.KitRules;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Um {@link Kit} como linha da tabela {@code kits}. Só a infraestrutura conhece esta
 * classe: o resto do mod usa o {@link Kit}. Os itens vão em SNBT ({@link ItemCodec}).
 */
@Entity
@Table(name = "kits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KitEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    @Column(nullable = false)
    private String icon;

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    @Column(nullable = false)
    private String items;

    // -1 = uma vez só, 0 = sem cooldown (Cooldown).
    @Column(name = "cooldown_seconds", nullable = false)
    private long cooldownSeconds;

    @Column(nullable = false)
    private boolean restricted;

    static KitEntity from(Kit kit, ItemCodec codec) {
        KitEntity entity = new KitEntity();
        entity.id = kit.id().value();
        entity.displayName = kit.appearance().name();
        entity.icon = codec.encode(kit.appearance().icon());
        entity.items = codec.encode(kit.items());
        entity.cooldownSeconds = kit.rules().cooldown().seconds();
        entity.restricted = kit.rules().restricted();
        return entity;
    }

    Kit toKit(ItemCodec codec) {
        return Kit.restore(
                new KitId(id),
                new KitAppearance(displayName, codec.decode(icon)),
                new KitRules(new Cooldown(cooldownSeconds), restricted),
                codec.decode(items, id));
    }

    String id() {
        return id;
    }
}
