package net.loyalnetwork.ravenessentials.home.infrastructure;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.ravenessentials.cache.ExpiringCache;
import net.loyalnetwork.ravenessentials.home.application.KnownHomes;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeBook;
import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.loyalnetwork.ravenessentials.home.domain.SetHome;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link HomeBook} com um {@link ExpiringCache} na frente, só para desempenho: sugestões,
 * placeholders e o {@code /home} leem da memória, e quem fica um tempo sem usar volta a ler
 * do banco. Vale também para as homes de quem está offline (um {@code /homes dono}): ficam
 * até ninguém mais olhar. As gravações passam pelo banco e atualizam a cópia.
 */
public final class CachedHomeBook implements HomeBook, KnownHomes {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.homes");

    private final HomeBook database;
    private final ExpiringCache<UUID, PlayerHomes> cache;

    public CachedHomeBook(HomeBook database, Duration idle) {
        this.database = database;
        this.cache = ExpiringCache.loading(idle, database::of);
    }

    /** No login: já deixa carregado, para as sugestões do {@code /home} aparecerem logo. */
    public void preload(UUID owner) {
        cache.get(owner).exceptionally(error -> {
            LOGGER.log(Level.WARNING, "Não deu para carregar as homes de " + owner, error);
            return null;
        });
    }

    /** No logout: se ele mexer nas homes em outro servidor, aqui não fica a cópia velha. */
    public void evict(UUID owner) {
        cache.invalidate(owner);
    }

    @Override
    public Optional<PlayerHomes> known(UUID owner) {
        return cache.getNow(owner);
    }

    @Override
    public CompletableFuture<PlayerHomes> of(UUID owner) {
        return cache.get(owner);
    }

    @Override
    public CompletableFuture<SetHome> set(UUID owner, HomeName name, CoffeeLocation location, HomeLimit limit) {
        return database.set(owner, name, location, limit).thenApply(decision -> {
            switch (decision) {
                case SetHome.Created created -> cache.update(owner, current -> current.with(created.home()));
                case SetHome.Moved moved -> cache.update(owner, current -> current.with(moved.home()));
                case SetHome.LimitReached reached -> {
                }
            }
            return decision;
        });
    }

    @Override
    public CompletableFuture<Void> save(UUID owner, Home home) {
        return database.save(owner, home).thenApply(ignored -> {
            cache.update(owner, current -> current.with(home));
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> rename(UUID owner, HomeName from, Home renamed) {
        return database.rename(owner, from, renamed).thenApply(ignored -> {
            cache.update(owner, current -> current.without(from).with(renamed));
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID owner, HomeName name) {
        return database.delete(owner, name).thenApply(found -> {
            cache.update(owner, current -> current.without(name));
            return found;
        });
    }
}
