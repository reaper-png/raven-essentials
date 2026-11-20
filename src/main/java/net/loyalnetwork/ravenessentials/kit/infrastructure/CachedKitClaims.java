package net.loyalnetwork.ravenessentials.kit.infrastructure;

import net.loyalnetwork.ravenessentials.kit.application.KnownClaims;
import net.loyalnetwork.ravenessentials.kit.domain.ClaimDecision;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaims;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.PlayerClaims;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link KitClaims} com os registros de quem está online em memória: tudo passa pelo
 * banco, e o que volta atualiza a cópia, que os placeholders leem na hora.
 * <p>
 * A cópia carrega no login ({@link #preload}), a cada {@link #of} (abrir o menu) e sai no
 * logout ({@link #evict}). O que outro servidor gravar só aparece no próximo {@link #of}.
 */
public final class CachedKitClaims implements KitClaims, KnownClaims {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");

    private final KitClaims database;
    private final Map<UUID, PlayerClaims> online = new ConcurrentHashMap<>();

    public CachedKitClaims(KitClaims database) {
        this.database = database;
    }

    public void preload(UUID player) {
        of(player).exceptionally(error -> {
            LOGGER.log(Level.WARNING, "Não deu para carregar os kits pegos por " + player, error);
            return null;
        });
    }

    public void evict(UUID player) {
        online.remove(player);
    }

    @Override
    public Optional<PlayerClaims> known(UUID player) {
        return Optional.ofNullable(online.get(player));
    }

    @Override
    public CompletableFuture<PlayerClaims> of(UUID player) {
        return database.of(player).thenApply(claims -> {
            online.put(player, claims);
            return claims;
        });
    }

    @Override
    public CompletableFuture<ClaimDecision> claim(UUID player, Kit kit, Instant now) {
        return database.claim(player, kit, now).thenApply(decision -> {
            if (decision instanceof ClaimDecision.Granted granted) {
                update(player, claims -> claims.with(kit.id(), granted.claim()));
            }
            return decision;
        });
    }

    @Override
    public CompletableFuture<Void> undo(UUID player, KitId kit, ClaimDecision.Granted granted) {
        return database.undo(player, kit, granted).thenApply(ignored -> {
            update(player, claims -> granted.previous()
                    .map(previous -> claims.with(kit, previous))
                    .orElseGet(() -> claims.without(kit)));
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> reset(UUID player, KitId kit) {
        return database.reset(player, kit).thenApply(found -> {
            update(player, claims -> claims.without(kit));
            return found;
        });
    }

    // Só atualiza quem está carregado: um jogador offline não ganha cópia por um reset.
    private void update(UUID player, java.util.function.UnaryOperator<PlayerClaims> change) {
        online.computeIfPresent(player, (id, claims) -> change.apply(claims));
    }
}
