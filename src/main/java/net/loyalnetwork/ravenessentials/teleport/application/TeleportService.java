package net.loyalnetwork.ravenessentials.teleport.application;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.coffeelib.neoforge.location.Locations;
import net.loyalnetwork.ravenessentials.database.PlayerTracker;
import net.loyalnetwork.ravenessentials.teleport.TeleportToggle;
import net.loyalnetwork.ravenessentials.teleport.domain.TeleportRequest;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Casos de uso dos pedidos de teleporte e dos teleportes diretos da staff. Chame na thread
 * do servidor.
 */
public final class TeleportService {

    private final TeleportRequests requests;

    public TeleportService(TeleportRequests requests) {
        this.requests = requests;
    }

    public TeleportResult request(ServerPlayer requester, ServerPlayer target) {
        if (requester.getUUID().equals(target.getUUID())) {
            return new TeleportResult.Self();
        }
        if (!TeleportToggle.acceptsRequests(target)) {
            return new TeleportResult.Blocked(Players.name(target));
        }
        if (requests.has(requester.getUUID(), target.getUUID())) {
            return new TeleportResult.AlreadySent(Players.name(target));
        }

        TeleportRequest request = new TeleportRequest(requester.getUUID(), Players.name(requester),
                target.getUUID(), Players.name(target));
        requests.add(request);
        return new TeleportResult.Sent(request);
    }

    public TeleportResult accept(ServerPlayer target, Optional<String> requesterName) {
        Optional<TeleportRequest> found = findIncoming(target, requesterName);
        if (found.isEmpty()) {
            return new TeleportResult.NoIncoming(requesterName);
        }

        TeleportRequest request = found.get();
        requests.remove(request);

        ServerPlayer requester = online(target, request.requester());
        if (requester == null) {
            return new TeleportResult.NoIncoming(requesterName);
        }
        if (!move(requester, target)) {
            return new TeleportResult.Failed(request);
        }
        return new TeleportResult.Accepted(request);
    }

    public TeleportResult deny(ServerPlayer target, Optional<String> requesterName) {
        Optional<TeleportRequest> found = findIncoming(target, requesterName);
        if (found.isEmpty()) {
            return new TeleportResult.NoIncoming(requesterName);
        }
        requests.remove(found.get());
        return new TeleportResult.Denied(found.get());
    }

    public TeleportResult cancel(ServerPlayer requester, Optional<String> targetName) {
        Optional<TeleportRequest> found = findOutgoing(requester, targetName);
        if (found.isEmpty()) {
            return new TeleportResult.NoOutgoing(targetName);
        }
        requests.remove(found.get());
        return new TeleportResult.Cancelled(found.get());
    }

    public TeleportResult toggle(ServerPlayer player) {
        return new TeleportResult.Toggled(TeleportToggle.toggle(player));
    }

    /** Leva {@code target} até {@code actor}; falso se não deu para teleportar. */
    public boolean here(ServerPlayer actor, ServerPlayer target) {
        if (actor.getUUID().equals(target.getUUID())) {
            return false;
        }
        return move(target, actor);
    }

    /** Leva todos os jogadores visíveis até {@code actor} e devolve os UUIDs dos que foram. */
    public List<UUID> all(ServerPlayer actor) {
        List<UUID> moved = new ArrayList<>();
        var server = actor.getServer();
        if (server == null) {
            return moved;
        }

        CoffeeLocation destination = Locations.of(actor);
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            if (target.getUUID().equals(actor.getUUID()) || !Vanish.canSee(actor, target)) {
                continue;
            }
            PlayerTracker.rememberLocation(target);
            if (Locations.teleport(target, destination)) {
                moved.add(target.getUUID());
            }
        }
        return moved;
    }

    public List<String> incomingNames(ServerPlayer player) {
        return requests.incoming(player.getUUID()).stream().map(TeleportRequest::requesterName).toList();
    }

    public List<String> outgoingNames(ServerPlayer player) {
        return requests.outgoing(player.getUUID()).stream().map(TeleportRequest::targetName).toList();
    }

    private Optional<TeleportRequest> findIncoming(ServerPlayer target, Optional<String> requesterName) {
        return requesterName
                .map(name -> requests.incomingFrom(target.getUUID(), name))
                .orElseGet(() -> requests.latestIncoming(target.getUUID()));
    }

    private Optional<TeleportRequest> findOutgoing(ServerPlayer requester, Optional<String> targetName) {
        return targetName
                .map(name -> requests.outgoingTo(requester.getUUID(), name))
                .orElseGet(() -> requests.latestOutgoing(requester.getUUID()));
    }

    private static boolean move(ServerPlayer player, ServerPlayer destination) {
        PlayerTracker.rememberLocation(player);
        return Locations.teleport(player, Locations.of(destination));
    }

    private static ServerPlayer online(ServerPlayer context, UUID id) {
        var server = context.getServer();
        if (server == null) {
            return null;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(id)) {
                return player;
            }
        }
        return null;
    }
}
