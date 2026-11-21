package net.loyalnetwork.ravenessentials.home.infrastructure;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.coffeelib.neoforge.location.Locations;
import net.loyalnetwork.ravenessentials.database.PlayerTracker;
import net.loyalnetwork.ravenessentials.home.application.Positions;
import net.minecraft.server.level.ServerPlayer;

/** {@link Positions} com o {@code Locations} da CoffeeLib, guardando a volta para o {@code /back}. */
public final class LocationPositions implements Positions {

    @Override
    public CoffeeLocation where(ServerPlayer player) {
        return Locations.of(player);
    }

    // Só guarda o /back quando dá para ir: um mundo que sumiu não apaga o último lugar.
    @Override
    public boolean travel(ServerPlayer player, CoffeeLocation destination) {
        if (player.getServer() == null || Locations.world(player.getServer(), destination).isEmpty()) {
            return false;
        }
        PlayerTracker.rememberLocation(player);
        return Locations.teleport(player, destination);
    }
}
