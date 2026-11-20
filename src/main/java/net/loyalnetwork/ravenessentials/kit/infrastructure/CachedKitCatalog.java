package net.loyalnetwork.ravenessentials.kit.infrastructure;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitCatalog;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Os kits em memória, gravados no banco por trás. Os menus leem daqui sem esperar o
 * banco; cada mudança vale na hora e é gravada em seguida.
 * <p>
 * Só a thread do servidor mexe no mapa. As gravações de um mesmo kit vão em fila, para
 * uma mudança antiga nunca terminar depois de uma nova e desfazê-la.
 */
public final class CachedKitCatalog implements KitCatalog {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");

    private final KitRepository repository;
    private final ItemCodec codec;
    private final Scheduler scheduler;
    private final Map<KitId, Kit> kits = new TreeMap<>((a, b) -> a.value().compareTo(b.value()));
    private final Map<KitId, CompletableFuture<?>> writes = new HashMap<>();

    public CachedKitCatalog(KitRepository repository, ItemCodec codec, Scheduler scheduler) {
        this.repository = repository;
        this.codec = codec;
        this.scheduler = scheduler;
    }

    /** Lê os kits do banco. Chame com o servidor já de pé (os itens precisam dos registros). */
    public CompletableFuture<Void> load() {
        return scheduler.onMain(repository.findAll())
                .thenAccept(this::replaceWith)
                .exceptionally(error -> {
                    LOGGER.log(Level.SEVERE, "Falha ao carregar os kits", error);
                    return null;
                });
    }

    private void replaceWith(List<KitEntity> rows) {
        kits.clear();
        for (KitEntity row : rows) {
            restore(row);
        }
        LOGGER.info("Kits carregados: " + kits.size());
    }

    // Um kit quebrado não impede os outros de carregar.
    private void restore(KitEntity row) {
        try {
            Kit kit = row.toKit(codec);
            kits.put(kit.id(), kit);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Kit " + row.id() + " ignorado: não deu para ler do banco", e);
        }
    }

    @Override
    public Optional<Kit> find(KitId id) {
        return Optional.ofNullable(kits.get(id));
    }

    @Override
    public List<Kit> all() {
        return new ArrayList<>(kits.values());
    }

    @Override
    public void save(Kit kit) {
        kits.put(kit.id(), kit);
        KitEntity row = KitEntity.from(kit, codec);
        enqueue(kit.id(), () -> repository.save(row));
    }

    @Override
    public void remove(KitId id) {
        kits.remove(id);
        enqueue(id, () -> repository.deleteById(id.value()));
    }

    private void enqueue(KitId id, Supplier<CompletableFuture<?>> write) {
        CompletableFuture<?> previous = writes.getOrDefault(id, CompletableFuture.completedFuture(null));
        CompletableFuture<?> next = previous
                .handle((ignored, error) -> null)
                .thenCompose(ignored -> write.get())
                .whenComplete((ignored, error) -> logFailure(id, error));
        writes.put(id, next);
        // Tira da fila quando termina, se ninguém entrou atrás (na thread do servidor, como o resto).
        next.whenComplete((ignored, error) -> scheduler.onMain(() -> writes.remove(id, next)));
    }

    private static void logFailure(KitId id, Throwable error) {
        if (error != null) {
            LOGGER.log(Level.SEVERE, "Falha ao gravar o kit " + id, error);
        }
    }
}
