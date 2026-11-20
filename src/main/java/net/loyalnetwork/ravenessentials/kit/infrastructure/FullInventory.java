package net.loyalnetwork.ravenessentials.kit.infrastructure;

/** O que fazer quando o kit não cabe no inventário. */
public enum FullInventory {
    /** Entrega assim mesmo; o que não couber cai no chão. */
    DROP,
    /** Não entrega até o jogador abrir espaço para todos os itens. */
    DENY
}
