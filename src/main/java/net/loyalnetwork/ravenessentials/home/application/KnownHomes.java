package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta: as homes de quem está online, já em memória, para responder na hora (sugestões
 * de comando, placeholders, o {@code /home} sem esperar o banco). Vazio enquanto não carregou.
 */
public interface KnownHomes {

    Optional<PlayerHomes> known(UUID owner);
}
