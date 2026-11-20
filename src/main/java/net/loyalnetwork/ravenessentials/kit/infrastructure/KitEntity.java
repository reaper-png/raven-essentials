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

    // 0 = sem cooldown. Antes do one_time, -1 queria dizer "uma vez só": ver toKit.
    @Column(name = "cooldown_seconds", nullable = false)
    private long cooldownSeconds;

    @Column(nullable = false)
    private boolean restricted;

    // Coluna nova: nas linhas que já existiam, o MySQL preenche com 0 (falso).
    @Column(name = "one_time", nullable = false)
    private boolean oneTime;

    private static final long LEGACY_ONE_TIME = -1;

    static KitEntity from(Kit kit, ItemCodec codec) {
        KitEntity entity = new KitEntity();
        entity.id = kit.id().value();
        entity.displayName = kit.appearance().name();
        entity.icon = codec.encode(kit.appearance().icon());
        entity.items = codec.encode(kit.items());
        entity.cooldownSeconds = kit.rules().cooldown().seconds();
        entity.restricted = kit.rules().restricted();
        entity.oneTime = kit.rules().oneTime();
        return entity;
    }

    Kit toKit(ItemCodec codec) {
        return Kit.restore(
                new KitId(id),
                new KitAppearance(displayName, codec.decode(icon)),
                rules(),
                codec.decode(items, id));
    }

    // Uma linha de antes do one_time com -1 vira "uma vez só, sem cooldown"; a próxima
    // gravação do kit já sai no formato novo.
    private KitRules rules() {
        if (cooldownSeconds == LEGACY_ONE_TIME) {
            return new KitRules(Cooldown.NONE, restricted, true);
        }
        return new KitRules(new Cooldown(cooldownSeconds), restricted, oneTime);
    }

    String id() {
        return id;
    }
}
