package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Quando cada jogador pegou cada kit. Os futuros completam fora da thread do servidor. */
public interface KitClaims {

    /** Os registros de {@code player}, para montar o menu. */
    CompletableFuture<PlayerClaims> of(UUID player);

    /**
     * Pergunta ao {@link Kit#claim} e, se ele deixar, grava o novo registro, tudo de uma
     * vez: dois pedidos ao mesmo tempo (dois cliques, dois servidores) não pegam duas vezes.
     */
    CompletableFuture<ClaimDecision> claim(UUID player, Kit kit, Instant now);

    /** Desfaz um {@link ClaimDecision.Granted} que não chegou a ser entregue. */
    CompletableFuture<Void> undo(UUID player, KitId kit, ClaimDecision.Granted granted);

    /**
     * Esquece que {@code player} pegou {@code kit}: o cooldown (ou o "uma vez só") recomeça.
     * O futuro diz se havia registro.
     */
    CompletableFuture<Boolean> reset(UUID player, KitId kit);
}
