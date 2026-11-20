package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.kit.domain.ClaimDecision;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitCatalog;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaims;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;
import net.loyalnetwork.ravenessentials.kit.domain.PlayerClaims;
import net.minecraft.server.level.ServerPlayer;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Casos de uso de quem pega kits: ver o que pode pegar e pegar. Chame na thread
 * do servidor; os futuros devolvidos completam nela também.
 */
public final class KitClaimService {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");

    private final KitCatalog catalog;
    private final KitClaims claims;
    private final KitDelivery delivery;
    private final Scheduler scheduler;
    private final BiPredicate<ServerPlayer, String> permissions;
    private final Clock clock;
    // Um pedido por jogador: dois cliques seguidos não abrem duas transações.
    private final Set<UUID> claiming = ConcurrentHashMap.newKeySet();

    public KitClaimService(KitCatalog catalog, KitClaims claims, KitDelivery delivery, Scheduler scheduler,
                      BiPredicate<ServerPlayer, String> permissions, Clock clock) {
        this.catalog = catalog;
        this.claims = claims;
        this.delivery = delivery;
        this.scheduler = scheduler;
        this.permissions = permissions;
        this.clock = clock;
    }

    public Optional<Kit> find(KitId id) {
        return catalog.find(id);
    }

    /** Se {@code player} tem acesso a {@code kit} (os restritos pedem a permissão do kit). */
    public boolean canUse(ServerPlayer player, Kit kit) {
        return kit.allows(node -> permissions.test(player, node));
    }

    /** Todos os kits (para sugestões de comando, por exemplo). */
    public List<Kit> kits() {
        return catalog.all();
    }

    /** Todos os kits, com o que {@code player} pode fazer com cada um. */
    public CompletableFuture<List<KitOffer>> offersFor(ServerPlayer player) {
        return scheduler.onMain(claims.of(player.getUUID()))
                .thenApply(history -> offers(player, history));
    }

    private List<KitOffer> offers(ServerPlayer player, PlayerClaims history) {
        Predicate<String> has = node -> permissions.test(player, node);
        return catalog.all().stream()
                .map(kit -> new KitOffer(kit, kit.statusFor(has, history.of(kit.id()), clock.instant())))
                .toList();
    }

    /** Entrega {@code id} a {@code player}, se as regras do kit deixarem. */
    public CompletableFuture<ClaimResult> claim(ServerPlayer player, KitId id) {
        Optional<Kit> found = catalog.find(id);
        if (found.isEmpty()) {
            return done(new ClaimResult.NotFound(id));
        }
        Kit kit = found.get();
        if (!canUse(player, kit)) {
            return done(new ClaimResult.Refused(kit, new KitStatus.Locked()));
        }
        if (!delivery.accepts(player, kit.items())) {
            return done(new ClaimResult.InventoryFull(kit));
        }
        UUID uuid = player.getUUID();
        if (!claiming.add(uuid)) {
            return done(new ClaimResult.Busy());
        }
        return scheduler.onMain(claims.claim(uuid, kit, clock.instant()))
                .thenApply(decision -> deliver(player, kit, decision))
                .whenComplete((result, error) -> claiming.remove(uuid));
    }

    private ClaimResult deliver(ServerPlayer player, Kit kit, ClaimDecision decision) {
        return switch (decision) {
            case ClaimDecision.Granted granted -> handOver(player, kit, granted);
            case ClaimDecision.OnCooldown cooldown ->
                    new ClaimResult.Refused(kit, new KitStatus.OnCooldown(cooldown.remaining()));
            case ClaimDecision.AlreadyClaimed claimed -> new ClaimResult.Refused(kit, new KitStatus.Claimed());
        };
    }

    // O registro já foi gravado: se o jogador saiu enquanto o banco respondia, devolve o kit para ele.
    private ClaimResult handOver(ServerPlayer player, Kit kit, ClaimDecision.Granted granted) {
        if (player.hasDisconnected()) {
            claims.undo(player.getUUID(), kit.id(), granted).exceptionally(error -> log("desfazer", kit, error));
            return new ClaimResult.Busy();
        }
        delivery.deliver(player, kit.items());
        return new ClaimResult.Claimed(kit);
    }

    private static Void log(String action, Kit kit, Throwable error) {
        LOGGER.log(Level.SEVERE, "Falha ao " + action + " o registro do kit " + kit.id(), error);
        return null;
    }

    private static CompletableFuture<ClaimResult> done(ClaimResult result) {
        return CompletableFuture.completedFuture(result);
    }
}
