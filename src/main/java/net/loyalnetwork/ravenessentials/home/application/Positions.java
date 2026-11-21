package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.minecraft.server.level.ServerPlayer;

/** Porta: onde o jogador está e como levá-lo a outro lugar. Só na thread do servidor. */
public interface Positions {

    CoffeeLocation where(ServerPlayer player);

    /** Leva o jogador (guardando de onde saiu, para o {@code /back}); falso se o mundo não existe mais. */
    boolean travel(ServerPlayer player, CoffeeLocation destination);
}
