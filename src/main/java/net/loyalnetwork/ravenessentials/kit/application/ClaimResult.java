package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;

/** Como terminou um pedido de kit. Quem mostra ao jogador é a apresentação. */
public sealed interface ClaimResult {

    record Claimed(Kit kit) implements ClaimResult {
    }

    /** As regras do kit não deixaram: {@code status} diz por quê. */
    record Refused(Kit kit, KitStatus status) implements ClaimResult {
    }

    record NotFound(KitId id) implements ClaimResult {
    }

    /** Não cabe no inventário (com a entrega configurada para recusar). */
    record InventoryFull(Kit kit) implements ClaimResult {
    }

    /** Já há um pedido deste jogador em andamento, ou ele saiu antes de receber. */
    record Busy() implements ClaimResult {
    }
}
