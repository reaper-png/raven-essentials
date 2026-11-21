package net.loyalnetwork.ravenessentials.home.application;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Porta: quem é o jogador com esse nome, online ou não. O futuro completa na thread do servidor. */
public interface PlayerLookup {

    CompletableFuture<Optional<UUID>> uuidOf(String name);
}
