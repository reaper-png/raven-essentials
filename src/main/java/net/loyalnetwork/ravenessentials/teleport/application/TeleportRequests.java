package net.loyalnetwork.ravenessentials.teleport.application;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.teleport.domain.TeleportRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * Pedidos pendentes, só em memória: aceitos, negados, cancelados ou expirados saem daqui.
 * Chame na thread do servidor; a expiração também roda nela.
 */
public final class TeleportRequests {

    private final Map<UUID, List<TeleportRequest>> incoming = new HashMap<>();
    private final Map<UUID, List<TeleportRequest>> outgoing = new HashMap<>();
    private final Scheduler scheduler;
    private final IntSupplier lifetimeSeconds;
    private final Consumer<TeleportRequest> expired;

    public TeleportRequests(Scheduler scheduler, IntSupplier lifetimeSeconds, Consumer<TeleportRequest> expired) {
        this.scheduler = scheduler;
        this.lifetimeSeconds = lifetimeSeconds;
        this.expired = expired;
    }

    public void add(TeleportRequest request) {
        incoming.computeIfAbsent(request.target(), id -> new ArrayList<>()).add(request);
        outgoing.computeIfAbsent(request.requester(), id -> new ArrayList<>()).add(request);
        scheduler.later(lifetimeTicks(), () -> expire(request));
    }

    public boolean has(UUID requester, UUID target) {
        List<TeleportRequest> requests = outgoing.get(requester);
        if (requests == null) {
            return false;
        }
        for (TeleportRequest request : requests) {
            if (request.target().equals(target)) {
                return true;
            }
        }
        return false;
    }

    public Optional<TeleportRequest> latestIncoming(UUID target) {
        return last(incoming.get(target));
    }

    public Optional<TeleportRequest> incomingFrom(UUID target, String name) {
        return find(incoming.get(target), request -> request.requesterName().equalsIgnoreCase(name));
    }

    public Optional<TeleportRequest> latestOutgoing(UUID requester) {
        return last(outgoing.get(requester));
    }

    public Optional<TeleportRequest> outgoingTo(UUID requester, String name) {
        return find(outgoing.get(requester), request -> request.targetName().equalsIgnoreCase(name));
    }

    public List<TeleportRequest> incoming(UUID target) {
        return copy(incoming.get(target));
    }

    public List<TeleportRequest> outgoing(UUID requester) {
        return copy(outgoing.get(requester));
    }

    public boolean remove(TeleportRequest request) {
        boolean removed = removeFrom(incoming, request.target(), request);
        removed |= removeFrom(outgoing, request.requester(), request);
        return removed;
    }

    /** Esquece tudo que envolve o jogador (ao sair do servidor). */
    public void forget(UUID player) {
        List<TeleportRequest> involved = new ArrayList<>(incoming.getOrDefault(player, List.of()));
        involved.addAll(outgoing.getOrDefault(player, List.of()));
        involved.forEach(this::remove);
    }

    private void expire(TeleportRequest request) {
        if (remove(request)) {
            expired.accept(request);
        }
    }

    private long lifetimeTicks() {
        return Math.max(1, lifetimeSeconds.getAsInt()) * 20L;
    }

    private static Optional<TeleportRequest> last(List<TeleportRequest> requests) {
        return requests == null || requests.isEmpty()
                ? Optional.empty()
                : Optional.of(requests.get(requests.size() - 1));
    }

    private static Optional<TeleportRequest> find(List<TeleportRequest> requests, Predicate<TeleportRequest> match) {
        if (requests == null) {
            return Optional.empty();
        }
        for (int i = requests.size() - 1; i >= 0; i--) {
            TeleportRequest request = requests.get(i);
            if (match.test(request)) {
                return Optional.of(request);
            }
        }
        return Optional.empty();
    }

    private static List<TeleportRequest> copy(List<TeleportRequest> requests) {
        return requests == null ? List.of() : List.copyOf(requests);
    }

    private static boolean removeFrom(Map<UUID, List<TeleportRequest>> requests, UUID player,
                                      TeleportRequest request) {
        List<TeleportRequest> list = requests.get(player);
        if (list == null) {
            return false;
        }
        boolean removed = list.remove(request);
        if (list.isEmpty()) {
            requests.remove(player);
        }
        return removed;
    }
}
