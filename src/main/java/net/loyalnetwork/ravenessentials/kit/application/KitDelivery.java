package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.ravenessentials.kit.domain.KitItems;
import net.minecraft.server.level.ServerPlayer;

/** Porta: como os itens de um kit chegam ao jogador. Só na thread do servidor. */
public interface KitDelivery {

    /** Se a entrega pode acontecer agora (por exemplo, se cabe no inventário). */
    boolean accepts(ServerPlayer player, KitItems items);

    void deliver(ServerPlayer player, KitItems items);
}
