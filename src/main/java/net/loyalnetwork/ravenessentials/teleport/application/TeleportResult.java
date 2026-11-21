package net.loyalnetwork.ravenessentials.teleport.application;

import net.loyalnetwork.ravenessentials.teleport.domain.TeleportRequest;

import java.util.Optional;

/** Como terminou um pedido de teleporte. Quem mostra ao jogador é a apresentação. */
public sealed interface TeleportResult {

    record Sent(TeleportRequest request) implements TeleportResult {
    }

    record Accepted(TeleportRequest request) implements TeleportResult {
    }

    record Denied(TeleportRequest request) implements TeleportResult {
    }

    record Cancelled(TeleportRequest request) implements TeleportResult {
    }

    record Toggled(boolean enabled) implements TeleportResult {
    }

    record Self() implements TeleportResult {
    }

    /** O alvo desativou o recebimento de pedidos. */
    record Blocked(String target) implements TeleportResult {
    }

    /** Já existe um pedido pendente para esse alvo. */
    record AlreadySent(String target) implements TeleportResult {
    }

    /** Nenhum pedido recebido; {@code player} é o nome pedido, ou vazio para "o mais recente". */
    record NoIncoming(Optional<String> player) implements TeleportResult {
    }

    /** Nenhum pedido enviado; {@code player} é o nome pedido, ou vazio para "o mais recente". */
    record NoOutgoing(Optional<String> player) implements TeleportResult {
    }

    /** O mundo do destino sumiu ou o jogador saiu no meio do caminho. */
    record Failed(TeleportRequest request) implements TeleportResult {
    }
}
