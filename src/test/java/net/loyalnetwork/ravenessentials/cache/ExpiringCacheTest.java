package net.loyalnetwork.ravenessentials.cache;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpiringCacheTest {

    private static final Duration IDLE = Duration.ofMinutes(10);

    /** Um relógio que só anda quando o teste manda. */
    private static final class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    /** Um "banco" que conta as leituras e responde quando o teste manda (ou na hora). */
    private static final class TestDatabase {
        final Map<String, String> rows = new HashMap<>();
        final AtomicInteger reads = new AtomicInteger();
        CompletableFuture<String> pending;
        boolean answerLater;

        CompletableFuture<String> load(String key) {
            reads.incrementAndGet();
            if (answerLater) {
                pending = new CompletableFuture<>();
                return pending;
            }
            return CompletableFuture.completedFuture(rows.get(key));
        }
    }

    private final TestClock clock = new TestClock();
    private final TestDatabase database = new TestDatabase();
    private final ExpiringCache<String, String> cache = ExpiringCache.loading(IDLE, database::load, clock);

    @Test
    void primeiraLeituraVaiAoBancoEAsOutrasNao() {
        database.rows.put("a", "1");
        assertEquals("1", cache.get("a").join());
        assertEquals("1", cache.get("a").join());
        assertEquals(1, database.reads.get());
    }

    @Test
    void expiraDepoisDoTempoSemUsoEVoltaAoBanco() {
        database.rows.put("a", "1");
        cache.get("a").join();

        database.rows.put("a", "2"); // outro servidor mudou
        clock.advance(IDLE);
        assertEquals("2", cache.get("a").join(), "sem uso por 10 min: lê o banco de novo");
        assertEquals(2, database.reads.get());
    }

    // O tempo conta do último uso, não de quando entrou: quem está sendo usado não expira.
    @Test
    void cadaUsoAdiaAExpiracao() {
        database.rows.put("a", "1");
        cache.get("a").join();
        for (int i = 0; i < 5; i++) {
            clock.advance(IDLE.minusMinutes(1));
            assertEquals(Optional.of("1"), cache.getNow("a"));
        }
        assertEquals(1, database.reads.get());
    }

    @Test
    void getNowSemValorComecaACarregarEDevolveVazio() {
        database.rows.put("a", "1");
        assertEquals(Optional.empty(), cache.getNow("a"));
        assertEquals(1, database.reads.get());
        assertEquals(Optional.of("1"), cache.getNow("a"), "na próxima já está");
    }

    @Test
    void peekNaoCarrega() {
        assertEquals(Optional.empty(), cache.peek("a"));
        assertEquals(0, database.reads.get());
    }

    @Test
    void duasLeiturasJuntasViramUmaSo() {
        database.answerLater = true;
        CompletableFuture<String> first = cache.get("a");
        CompletableFuture<String> second = cache.get("a");
        cache.getNow("a");
        assertSame(first, second);
        assertEquals(1, database.reads.get());

        database.pending.complete("1");
        assertEquals("1", first.join());
        assertEquals(Optional.of("1"), cache.peek("a"));
    }

    @Test
    void updateMudaOGuardado() {
        database.rows.put("a", "1");
        cache.get("a").join();
        cache.update("a", value -> value + "+");
        assertEquals("1+", cache.get("a").join());
        assertEquals(1, database.reads.get());
    }

    @Test
    void updateSemValorNaoCriaNada() {
        cache.update("a", value -> "inventado");
        assertEquals(0, cache.size());
    }

    @Test
    void updateDeValorExpiradoTiraEmVezDeMudar() {
        database.rows.put("a", "1");
        cache.get("a").join();
        clock.advance(IDLE);
        cache.update("a", value -> value + "+");
        assertEquals(Optional.empty(), cache.peek("a"));
    }

    // A leitura saiu do banco antes da gravação: guardá-la desfaria a gravação.
    @Test
    void leituraQueChegaDepoisDeUmaGravacaoNaoEGuardada() {
        database.answerLater = true;
        CompletableFuture<String> stale = cache.get("a");
        cache.put("a", "novo");

        database.pending.complete("velho");
        assertEquals("velho", stale.join(), "quem pediu recebe o que veio");
        assertEquals(Optional.of("novo"), cache.peek("a"), "mas o cache fica com o novo");
    }

    @Test
    void leituraQueChegaDepoisDeInvalidarNaoEGuardada() {
        database.answerLater = true;
        cache.get("a");
        cache.invalidate("a");
        database.pending.complete("velho");
        assertEquals(Optional.empty(), cache.peek("a"));
    }

    @Test
    void invalidateFazLerDeNovo() {
        database.rows.put("a", "1");
        cache.get("a").join();
        cache.invalidate("a");
        cache.get("a").join();
        assertEquals(2, database.reads.get());
    }

    @Test
    void invalidateAllEsqueceTudoEUmaLeituraACaminhoNaoVolta() {
        database.rows.put("a", "1");
        cache.get("a").join();
        database.answerLater = true;
        cache.get("b");

        cache.invalidateAll();
        database.pending.complete("2");
        assertEquals(0, cache.size());
    }

    @Test
    void falhaDoBancoNaoFicaGuardada() {
        database.answerLater = true;
        CompletableFuture<String> failed = cache.get("a");
        database.pending.completeExceptionally(new IllegalStateException("banco fora"));
        assertTrue(failed.isCompletedExceptionally());

        database.answerLater = false;
        database.rows.put("a", "1");
        assertEquals("1", cache.get("a").join(), "a próxima tenta de novo");
    }

    @Test
    void valorNuloNaoEGuardado() {
        assertEquals(null, cache.get("a").join());
        assertEquals(0, cache.size());
    }

    // Sem ninguém ler a chave de novo, a limpeza que vem na carona de outra leitura tira ela.
    @Test
    void expiradosSaemDaMemoriaSemPolling() {
        database.rows.put("a", "1");
        database.rows.put("b", "2");
        cache.get("a").join();
        clock.advance(IDLE);
        cache.get("b").join();
        assertEquals(1, cache.size());
    }

    @Test
    void tempoTemQueSerPositivo() {
        assertThrows(IllegalArgumentException.class, () -> ExpiringCache.loading(Duration.ZERO, database::load));
    }
}
