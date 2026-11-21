package net.loyalnetwork.ravenessentials.cache;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Um cache que só serve para dar desempenho: guarda o que o {@code loader} trouxe do banco
 * e esquece cada valor depois de {@code idle} sem ninguém usar. Quando o movimento cai, a
 * próxima leitura vai ao banco de novo e pega o que outro servidor tiver mudado - sem
 * nenhuma consulta periódica: nada roda em segundo plano, a expiração é vista na hora de ler.
 * <p>
 * Para cachear algo novo: um {@code ExpiringCache.loading(idle, repositorio::buscar)} e,
 * depois de cada gravação no banco, um {@link #update} (ou {@link #invalidate}).
 * <ul>
 *   <li>{@link #get} - o valor, do cache ou do banco (para quem pode esperar).</li>
 *   <li>{@link #getNow} - só o que já está no cache; se não está, começa a carregar e
 *       devolve vazio (para quem precisa da resposta na hora: placeholders, sugestões).</li>
 *   <li>{@link #update} - muda o valor guardado depois de gravar; se não há valor, não faz
 *       nada (a próxima leitura já vem do banco com a mudança).</li>
 * </ul>
 * Uma leitura que estava a caminho quando houve um {@link #update} ou {@link #invalidate}
 * não é guardada ao chegar: ela saiu do banco antes da mudança e desfaria a mudança.
 * Seguro para usar de qualquer thread.
 */
public final class ExpiringCache<K, V> {

    /** O tempo sem uso que os caches do mod usam, se não tiverem motivo para outro. */
    public static final Duration DEFAULT_IDLE = Duration.ofMinutes(10);

    private final Function<K, CompletableFuture<V>> loader;
    private final long idleMillis;
    private final Clock clock;
    private final Map<K, Entry<V>> entries = new ConcurrentHashMap<>();
    private final Map<K, CompletableFuture<V>> loading = new ConcurrentHashMap<>();
    private final AtomicLong lastSweep;

    private ExpiringCache(Duration idle, Function<K, CompletableFuture<V>> loader, Clock clock) {
        if (idle.isNegative() || idle.isZero()) {
            throw new IllegalArgumentException("idle must be positive: " + idle);
        }
        this.loader = Objects.requireNonNull(loader, "loader");
        this.idleMillis = idle.toMillis();
        this.clock = Objects.requireNonNull(clock, "clock");
        this.lastSweep = new AtomicLong(clock.millis());
    }

    public static <K, V> ExpiringCache<K, V> loading(Duration idle, Function<K, CompletableFuture<V>> loader) {
        return new ExpiringCache<>(idle, loader, Clock.systemUTC());
    }

    /** {@link #loading(Duration, Function)} com o relógio dado, para testes. */
    public static <K, V> ExpiringCache<K, V> loading(Duration idle, Function<K, CompletableFuture<V>> loader,
                                                     Clock clock) {
        return new ExpiringCache<>(idle, loader, clock);
    }

    /** O valor: do cache, se ainda vale; senão, do {@code loader}, que fica guardado. */
    public CompletableFuture<V> get(K key) {
        return fresh(key).map(CompletableFuture::completedFuture).orElseGet(() -> load(key));
    }

    /** Só o que já está no cache. Se não está, começa a carregar (uma vez só) e devolve vazio. */
    public Optional<V> getNow(K key) {
        Optional<V> cached = fresh(key);
        if (cached.isEmpty()) {
            load(key);
        }
        return cached;
    }

    /** Só o que já está no cache, sem carregar nada. */
    public Optional<V> peek(K key) {
        return fresh(key);
    }

    /** Guarda {@code value} como o valor atual (por exemplo, o que acabou de ser gravado). */
    public void put(K key, V value) {
        loading.remove(key);
        store(key, value);
    }

    /** Muda o valor guardado, se houver um que ainda vale; senão, não faz nada. */
    public void update(K key, UnaryOperator<V> change) {
        loading.remove(key);
        long now = clock.millis();
        entries.computeIfPresent(key, (ignored, entry) ->
                entry.isExpired(now, idleMillis) ? null : new Entry<>(change.apply(entry.value()), now));
    }

    /** Esquece o valor: a próxima leitura vai ao banco. */
    public void invalidate(K key) {
        loading.remove(key);
        entries.remove(key);
    }

    /** Esquece tudo (um "recarregar do banco"): cada chave vai ao banco na próxima leitura. */
    public void invalidateAll() {
        loading.clear();
        entries.clear();
    }

    /** Quantos valores estão guardados agora (contando os que já expiraram e ainda não foram vistos). */
    public int size() {
        return entries.size();
    }

    private Optional<V> fresh(K key) {
        long now = clock.millis();
        sweepIfDue(now);
        Entry<V> entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired(now, idleMillis)) {
            entries.remove(key, entry);
            return Optional.empty();
        }
        entry.touch(now);
        return Optional.of(entry.value());
    }

    // Uma leitura por chave de cada vez. Se a chave mudou enquanto ela vinha (update, invalidate,
    // put), o resultado vai para quem pediu mas não é guardado.
    private CompletableFuture<V> load(K key) {
        CompletableFuture<V> existing = loading.get(key);
        if (existing != null) {
            return existing;
        }
        CompletableFuture<V> started = new CompletableFuture<>();
        CompletableFuture<V> raced = loading.putIfAbsent(key, started);
        if (raced != null) {
            return raced;
        }
        CompletableFuture<V> source;
        try {
            source = loader.apply(key);
        } catch (RuntimeException e) {
            loading.remove(key, started);
            started.completeExceptionally(e);
            return started;
        }
        source.whenComplete((value, error) -> {
            boolean current = loading.remove(key, started);
            if (error != null) {
                started.completeExceptionally(error);
                return;
            }
            if (current && value != null) {
                store(key, value);
            }
            started.complete(value);
        });
        return started;
    }

    private void store(K key, V value) {
        long now = clock.millis();
        sweepIfDue(now);
        entries.put(key, new Entry<>(value, now));
    }

    // Limpa os expirados de vez em quando, na carona de uma leitura ou gravação: sem isso, a
    // chave de quem nunca mais foi lida ficaria na memória para sempre.
    private void sweepIfDue(long now) {
        long last = lastSweep.get();
        if (now - last >= idleMillis && lastSweep.compareAndSet(last, now)) {
            entries.values().removeIf(entry -> entry.isExpired(now, idleMillis));
        }
    }

    private static final class Entry<V> {
        private final V value;
        private volatile long touched;

        private Entry(V value, long touched) {
            this.value = Objects.requireNonNull(value, "value");
            this.touched = touched;
        }

        V value() {
            return value;
        }

        void touch(long now) {
            touched = now;
        }

        boolean isExpired(long now, long idleMillis) {
            return now - touched >= idleMillis;
        }
    }
}
