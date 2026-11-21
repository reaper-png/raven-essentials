package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.ravenessentials.kit.domain.PlayerClaims;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta: os registros de um jogador que já estão em memória, para responder na hora
 * (placeholders) sem ir ao banco. Vazio enquanto não carregou. Serve só para mostrar: quem
 * decide se pode pegar é sempre o banco ({@code KitClaims.claim}).
 */
public interface KnownClaims {

    Optional<PlayerClaims> known(UUID player);

    /** Esquece tudo o que está em memória: cada jogador volta a ser lido do banco. */
    void forgetAll();
}
