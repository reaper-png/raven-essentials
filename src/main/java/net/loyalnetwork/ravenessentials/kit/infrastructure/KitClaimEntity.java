package net.loyalnetwork.ravenessentials.kit.infrastructure;

import jakarta.persistence.Column;
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
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaim;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Um {@link KitClaim} como linha da tabela {@code kit_claims}. As duas colunas da chave são
 * chaves estrangeiras: {@code player_id} para {@code players} e {@code kit_id} para
 * {@code kits}. Só há registro de jogador e kit que existem, e apagar um dos dois apaga os
 * registros dele no próprio banco ({@code ON DELETE CASCADE}).
 */
@Entity
@Table(name = "kit_claims")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KitClaimEntity {

    @EmbeddedId
    private KitClaimKey key;

    // @MapsId: as colunas da chave são as próprias chaves estrangeiras, não colunas a mais.
    // LAZY: o registro nunca precisa do jogador ou do kit carregado, só da referência.
    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", foreignKey = @ForeignKey(name = "fk_kit_claims_player"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerEntity player;

    @MapsId("kitId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kit_id", foreignKey = @ForeignKey(name = "fk_kit_claims_kit"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private KitEntity kit;

    @Column(name = "last_claim", nullable = false)
    private Instant lastClaim;

    @Column(nullable = false)
    private int times;

    KitClaimEntity(KitClaimKey key, PlayerEntity player, KitEntity kit, KitClaim claim) {
        this.key = key;
        this.player = player;
        this.kit = kit;
        record(claim);
    }

    void record(KitClaim claim) {
        this.lastClaim = claim.lastClaim();
        this.times = claim.times();
    }

    KitId kit() {
        return new KitId(key.kitId());
    }

    KitClaim toClaim() {
        return new KitClaim(lastClaim, times);
    }
}
