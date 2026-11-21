package net.loyalnetwork.ravenessentials.home.domain;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** As homes de todos os jogadores. Os futuros completam fora da thread do servidor. */
public interface HomeBook {

    CompletableFuture<PlayerHomes> of(UUID owner);

    /**
     * Pergunta ao {@link PlayerHomes#set} e grava o que ele decidir, tudo de uma vez: dois
     * {@code /sethome} juntos (dois servidores) não passam do limite.
     */
    CompletableFuture<SetHome> set(UUID owner, HomeName name, CoffeeLocation location, HomeLimit limit);

    /** Grava uma home que já existe, com o mesmo nome (ícone, pública, lugar). */
    CompletableFuture<Void> save(UUID owner, Home home);

    /** Troca o nome de uma home: {@code renamed} já tem o nome novo. */
    CompletableFuture<Void> rename(UUID owner, HomeName from, Home renamed);

    /** O futuro diz se a home existia. */
    CompletableFuture<Boolean> delete(UUID owner, HomeName name);
}
