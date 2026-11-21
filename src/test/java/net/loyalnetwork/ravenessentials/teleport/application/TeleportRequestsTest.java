package net.loyalnetwork.ravenessentials.teleport.application;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.api.scheduler.Task;
import net.loyalnetwork.ravenessentials.teleport.domain.TeleportRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportRequestsTest {

    private static final UUID RENAN = UUID.randomUUID();
    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BIA = UUID.randomUUID();

    private final FakeScheduler scheduler = new FakeScheduler();
    private final List<TeleportRequest> expired = new ArrayList<>();
    private final TeleportRequests requests = new TeleportRequests(scheduler, () -> 60, expired::add);

    @Test
    void addGuardaNosDoisLados() {
        TeleportRequest request = request(RENAN, "Renan", ANA, "Ana");
        requests.add(request);

        assertEquals(List.of(request), requests.incoming(ANA));
        assertEquals(List.of(request), requests.outgoing(RENAN));
        assertTrue(requests.has(RENAN, ANA));
    }

    @Test
    void semPedidoNaoHaNada() {
        assertTrue(requests.latestIncoming(ANA).isEmpty());
        assertTrue(requests.latestOutgoing(RENAN).isEmpty());
        assertEquals(List.of(), requests.incoming(ANA));
        assertFalse(requests.has(RENAN, ANA));
    }

    @Test
    void latestPegaOUltimoEPorNomeIgnoraCaixa() {
        TeleportRequest first = request(RENAN, "Renan", ANA, "Ana");
        TeleportRequest second = request(BIA, "Bia", ANA, "Ana");
        requests.add(first);
        requests.add(second);

        assertEquals(Optional.of(second), requests.latestIncoming(ANA));
        assertEquals(Optional.of(first), requests.incomingFrom(ANA, "rEnAn"));
        assertEquals(Optional.of(second), requests.incomingFrom(ANA, "Bia"));
        assertEquals(Optional.empty(), requests.incomingFrom(ANA, "Carla"));
    }

    @Test
    void removeTiraDosDoisLados() {
        TeleportRequest request = request(RENAN, "Renan", ANA, "Ana");
        requests.add(request);

        assertTrue(requests.remove(request));
        assertFalse(requests.has(RENAN, ANA));
        assertEquals(List.of(), requests.incoming(ANA));
        assertEquals(List.of(), requests.outgoing(RENAN));
        assertFalse(requests.remove(request));
    }

    @Test
    void forgetTiraTudoQueEnvolveOJogador() {
        TeleportRequest received = request(RENAN, "Renan", ANA, "Ana");
        TeleportRequest sent = request(ANA, "Ana", BIA, "Bia");
        TeleportRequest other = request(BIA, "Bia", RENAN, "Renan");
        requests.add(received);
        requests.add(sent);
        requests.add(other);

        requests.forget(ANA);

        assertEquals(List.of(), requests.incoming(ANA));
        assertEquals(List.of(), requests.outgoing(ANA));
        assertTrue(requests.has(BIA, RENAN));
    }

    @Test
    void expiraNoTempoDaConfigERemove() {
        TeleportRequest request = request(RENAN, "Renan", ANA, "Ana");
        requests.add(request);

        assertEquals(60 * 20L, scheduler.delay);
        scheduler.runPending();

        assertEquals(List.of(request), expired);
        assertFalse(requests.has(RENAN, ANA));
    }

    @Test
    void pedidoJaRemovidoNaoExpiraDeNovo() {
        TeleportRequest request = request(RENAN, "Renan", ANA, "Ana");
        requests.add(request);
        requests.remove(request);

        scheduler.runPending();

        assertEquals(List.of(), expired);
    }

    private static TeleportRequest request(UUID requester, String requesterName, UUID target, String targetName) {
        return new TeleportRequest(requester, requesterName, target, targetName);
    }

    private static final class FakeScheduler implements Scheduler {

        private final List<Runnable> pending = new ArrayList<>();
        private long delay;

        @Override
        public void onMain(Runnable task) {
            task.run();
        }

        @Override
        public Executor main() {
            return Runnable::run;
        }

        @Override
        public Task later(long delay, Runnable task) {
            this.delay = delay;
            pending.add(task);
            return new Task() {
                private boolean cancelled;

                @Override
                public void cancel() {
                    cancelled = true;
                }

                @Override
                public boolean isCancelled() {
                    return cancelled;
                }
            };
        }

        @Override
        public Task every(long delay, long period, Runnable task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<T> onMain(CompletableFuture<T> future) {
            return future;
        }

        void runPending() {
            List<Runnable> tasks = List.copyOf(pending);
            pending.clear();
            tasks.forEach(Runnable::run);
        }
    }
}
