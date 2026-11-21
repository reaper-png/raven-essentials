package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeBook;
import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.loyalnetwork.ravenessentials.home.domain.SetHome;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * Casos de uso das homes do próprio jogador: criar, ir, mudar e apagar. Chame na thread do
 * servidor; os futuros devolvidos completam nela também.
 */
public final class HomeService {

    private final HomeBook book;
    private final KnownHomes known;
    private final Positions positions;
    private final HomeLimits limits;
    private final Scheduler scheduler;

    public HomeService(HomeBook book, KnownHomes known, Positions positions, HomeLimits limits, Scheduler scheduler) {
        this.book = book;
        this.known = known;
        this.positions = positions;
        this.limits = limits;
        this.scheduler = scheduler;
    }

    /** As homes do jogador (o {@link HomeBook} decide se vêm da memória ou do banco). */
    public CompletableFuture<PlayerHomes> homes(ServerPlayer player) {
        return scheduler.onMain(book.of(player.getUUID()));
    }

    /** Só o que já está em memória, na hora (sugestões de comando, placeholders); sem nada, vazio. */
    public PlayerHomes knownHomes(ServerPlayer player) {
        return loadedHomes(player).orElse(PlayerHomes.NONE);
    }

    /**
     * Como {@link #knownHomes}, mas vazio enquanto não carregou, em vez de "nenhuma home":
     * para quem não pode confundir as duas coisas (placeholders). Sem nada, começa a carregar.
     */
    public Optional<PlayerHomes> loadedHomes(ServerPlayer player) {
        return known.known(player.getUUID());
    }

    public HomeLimit limit(ServerPlayer player) {
        return limits.of(player);
    }

    /** O {@code /sethome}: aqui, onde o jogador está. */
    public CompletableFuture<SetHome> set(ServerPlayer player, HomeName name) {
        return scheduler.onMain(book.set(player.getUUID(), name, positions.where(player), limits.of(player)));
    }

    public CompletableFuture<HomeTrip> go(ServerPlayer player, HomeName name) {
        return homes(player).thenApply(homes -> homes.find(name)
                .map(home -> travel(player, home))
                .orElseGet(() -> new HomeTrip.NoSuchHome(name)));
    }

    /** Leva {@code player} até {@code home}, de quem quer que seja. */
    public HomeTrip travel(ServerPlayer player, Home home) {
        if (!positions.travel(player, home.location())) {
            return new HomeTrip.WorldMissing(home);
        }
        return new HomeTrip.Arrived(home);
    }

    /** Aplica {@code change} (ícone, pública) à home e grava. */
    public CompletableFuture<Home> change(ServerPlayer player, HomeName name, UnaryOperator<Home> change) {
        return homes(player).thenCompose(homes -> {
            Home changed = change.apply(homes.require(name));
            return scheduler.onMain(book.save(player.getUUID(), changed)).thenApply(ignored -> changed);
        });
    }

    public CompletableFuture<Home> rename(ServerPlayer player, HomeName from, HomeName to) {
        return homes(player).thenCompose(homes -> {
            Home renamed = homes.rename(from, to);
            return scheduler.onMain(book.rename(player.getUUID(), from, renamed)).thenApply(ignored -> renamed);
        });
    }

    /** O futuro diz se a home existia. */
    public CompletableFuture<Boolean> delete(ServerPlayer player, HomeName name) {
        return scheduler.onMain(book.delete(player.getUUID(), name));
    }
}
