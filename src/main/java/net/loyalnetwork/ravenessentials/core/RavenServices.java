package net.loyalnetwork.ravenessentials.core;

import net.loyalnetwork.coffeelib.api.di.annotation.Provide;
import net.loyalnetwork.ravenessentials.database.PlayerRepository;
import net.loyalnetwork.ravenessentials.database.RavenDatabase;

/**
 * O que os comandos do mod recebem por {@code @Receive}, além do que a CoffeeLib já
 * fornece ({@code PermissionManager}, {@code Scheduler}, {@code DatabaseManager},
 * {@code PlayerDirectory}). Repositório novo: um {@code @Provide} aqui.
 */
final class RavenServices {

    @Provide
    PlayerRepository players() {
        return RavenDatabase.playerRepository();
    }
}
