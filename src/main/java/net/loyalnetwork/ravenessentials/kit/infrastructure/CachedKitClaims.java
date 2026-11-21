package net.loyalnetwork.ravenessentials.kit.infrastructure;

import net.loyalnetwork.ravenessentials.cache.ExpiringCache;
import net.loyalnetwork.ravenessentials.kit.application.KnownClaims;
import net.loyalnetwork.ravenessentials.kit.domain.ClaimDecision;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaims;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.PlayerClaims;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link KitClaims} com um {@link ExpiringCache} na frente, só para desempenho: o menu e os
 * placeholders leem da memória, e quem fica um tempo sem usar volta a ler do banco (o que
 * outro servidor gravou aparece aí). As gravações passam pelo banco e atualizam a cópia.
 * Quem decide se pode pegar é sempre o banco ({@link #claim}).
 */
public final class CachedKitClaims implements KitClaims, KnownClaims {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");

    private final KitClaims database;
    private final ExpiringCache<UUID, PlayerClaims> cache;

    public CachedKitClaims(KitClaims database, Duration idle) {
        this.database = database;
        this.cache = ExpiringCache.loading(idle, database::of);
    }

    /** No login: já deixa carregado, para os placeholders responderem logo. */
    public void preload(UUID player) {
        cache.get(player).exceptionally(error -> {
            LOGGER.log(Level.WARNING, "Não deu para carregar os kits pegos por " + player, error);
            return null;
        });
    }

    /** No logout: se ele entrar em outro servidor e pegar kits lá, aqui não fica a cópia velha. */
    public void evict(UUID player) {
        cache.invalidate(player);
    }

    @Override
    public void forgetAll() {
        cache.invalidateAll();
    }

    @Override
    public Optional<PlayerClaims> known(UUID player) {
        return cache.getNow(player);
    }

    @Override
    public CompletableFuture<PlayerClaims> of(UUID player) {
        return cache.get(player);
    }

    @Override
    public CompletableFuture<ClaimDecision> claim(UUID player, Kit kit, Instant now) {
        return database.claim(player, kit, now).thenApply(decision -> {
            if (decision instanceof ClaimDecision.Granted granted) {
                cache.update(player, claims -> claims.with(kit.id(), granted.claim()));
            }
            return decision;
        });
    }

    @Override
    public CompletableFuture<Void> undo(UUID player, KitId kit, ClaimDecision.Granted granted) {
        return database.undo(player, kit, granted).thenApply(ignored -> {
            cache.update(player, claims -> granted.previous()
                    .map(previous -> claims.with(kit, previous))
                    .orElseGet(() -> claims.without(kit)));
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> reset(UUID player, KitId kit) {
        return database.reset(player, kit).thenApply(found -> {
            cache.update(player, claims -> claims.without(kit));
            return found;
        });
    }
}
