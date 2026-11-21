package net.loyalnetwork.ravenessentials.kit.domain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Os kits do servidor. Usado só na thread do servidor. */
public interface KitCatalog {

    Optional<Kit> find(KitId id);

    /** Todos, em ordem de id. */
    List<Kit> all();

    /** Cria ou substitui o kit com o mesmo id. */
    void save(Kit kit);

    /** Troca o id de {@code from} para o de {@code renamed}; os registros de quem já pegou vão junto. */
    void rename(KitId from, Kit renamed);

    /**
     * Lê de novo do armazenamento (o que outro servidor mudou passa a valer aqui). O futuro
     * diz quantos kits há e completa na thread do servidor.
     */
    CompletableFuture<Integer> reload();

    /** Exclui o kit; os registros de quem já pegou vão junto. */
    void remove(KitId id);
}
