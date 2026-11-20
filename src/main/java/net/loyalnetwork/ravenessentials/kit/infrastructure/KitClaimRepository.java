package net.loyalnetwork.ravenessentials.kit.infrastructure;

import jakarta.persistence.LockModeType;
import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.HibernateRepository;
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.kit.domain.ClaimDecision;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaim;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaims;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.PlayerClaims;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/** {@link KitClaims} na tabela {@code kit_claims}. */
public class KitClaimRepository extends HibernateRepository<KitClaimEntity, KitClaimKey> implements KitClaims {

    public KitClaimRepository(SessionFactory sessions, Database database) {
        super(sessions, database, KitClaimEntity.class);
    }

    @Override
    public CompletableFuture<PlayerClaims> of(UUID player) {
        return read(session -> session
                .createSelectionQuery("from KitClaimEntity where key.playerId = :player", KitClaimEntity.class)
                .setParameter("player", player.toString())
                .stream()
                .collect(Collectors.toMap(KitClaimEntity::kit, KitClaimEntity::toClaim)))
                .thenApply(PlayerClaims::new);
    }

    // A linha fica travada da leitura à gravação: outro pedido (outro clique, outro servidor)
    // espera e já vê o registro novo. Se dois tentarem criar a primeira linha juntos, um falha
    // com chave duplicada, e esse pedido não entrega nada.
    @Override
    public CompletableFuture<ClaimDecision> claim(UUID player, Kit kit, Instant now) {
        KitClaimKey key = key(player, kit.id());
        return write(session -> {
            KitClaimEntity row = locked(session, key);
            ClaimDecision decision = kit.claim(Optional.ofNullable(row).map(KitClaimEntity::toClaim), now);
            if (decision instanceof ClaimDecision.Granted granted) {
                store(session, key, row, granted.claim());
            }
            return decision;
        });
    }

    @Override
    public CompletableFuture<Void> undo(UUID player, KitId kit, ClaimDecision.Granted granted) {
        KitClaimKey key = key(player, kit);
        return write(session -> {
            KitClaimEntity row = locked(session, key);
            if (row != null) {
                restore(session, row, granted.previous());
            }
            return null;
        });
    }

    private static KitClaimEntity locked(Session session, KitClaimKey key) {
        return session.find(KitClaimEntity.class, key, LockModeType.PESSIMISTIC_WRITE);
    }

    private static void store(Session session, KitClaimKey key, KitClaimEntity row, KitClaim claim) {
        if (row == null) {
            // Referências, sem SELECT: as chaves estrangeiras garantem que jogador e kit existem.
            PlayerEntity player = session.getReference(PlayerEntity.class, key.playerId());
            KitEntity kit = session.getReference(KitEntity.class, key.kitId());
            session.persist(new KitClaimEntity(key, player, kit, claim));
            return;
        }
        row.record(claim);
    }

    private static void restore(Session session, KitClaimEntity row, Optional<KitClaim> previous) {
        previous.ifPresentOrElse(row::record, () -> session.remove(row));
    }

    private static KitClaimKey key(UUID player, KitId kit) {
        return new KitClaimKey(player.toString(), kit.value());
    }
}
