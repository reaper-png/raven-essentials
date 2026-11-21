package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeBook;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Casos de uso das homes de outros jogadores: ver as públicas e ir até elas. Quem tem a
 * permissão de ver todas ({@code bypass}) vê e vai também nas particulares.
 */
public final class HomeVisits {

    private final HomeBook book;
    private final PlayerLookup players;
    private final HomeService homes;
    private final Scheduler scheduler;
    private final Predicate<ServerPlayer> bypass;

    public HomeVisits(HomeBook book, PlayerLookup players, HomeService homes,
                      Scheduler scheduler, Predicate<ServerPlayer> bypass) {
        this.book = book;
        this.players = players;
        this.homes = homes;
        this.scheduler = scheduler;
        this.bypass = bypass;
    }

    /** As homes de {@code ownerName} que {@code visitor} pode ver; vazio se o jogador não existe. */
    public CompletableFuture<Optional<OwnerHomes>> homesOf(ServerPlayer visitor, String ownerName) {
        return players.uuidOf(ownerName).thenCompose(owner -> owner
                .map(id -> homesOf(id).thenApply(found ->
                        Optional.of(new OwnerHomes(id, ownerName, visibleTo(visitor, id, found)))))
                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
    }

    /** O {@code /home dono:nome}. */
    public CompletableFuture<HomeTrip> visit(ServerPlayer visitor, String ownerName, HomeName name) {
        return players.uuidOf(ownerName).thenCompose(owner -> owner
                .map(id -> homesOf(id).thenApply(found -> visit(visitor, id, found, name)))
                .orElseGet(() -> CompletableFuture.completedFuture(new HomeTrip.NoSuchPlayer(ownerName))));
    }

    /** Ir pela lista de outro jogador: a home já veio dela, mas a regra vale de novo. */
    public HomeTrip visit(ServerPlayer visitor, UUID owner, Home home) {
        if (!owner.equals(visitor.getUUID()) && !home.visitableBy(bypass.test(visitor))) {
            return new HomeTrip.Private(home);
        }
        return homes.travel(visitor, home);
    }

    private HomeTrip visit(ServerPlayer visitor, UUID owner, PlayerHomes found, HomeName name) {
        return found.find(name)
                .map(home -> visit(visitor, owner, home))
                .orElseGet(() -> new HomeTrip.NoSuchHome(name));
    }

    private List<Home> visibleTo(ServerPlayer visitor, UUID owner, PlayerHomes found) {
        boolean all = owner.equals(visitor.getUUID()) || bypass.test(visitor);
        return found.all().stream().filter(home -> home.visitableBy(all)).toList();
    }

    private CompletableFuture<PlayerHomes> homesOf(UUID owner) {
        return scheduler.onMain(book.of(owner));
    }
}
