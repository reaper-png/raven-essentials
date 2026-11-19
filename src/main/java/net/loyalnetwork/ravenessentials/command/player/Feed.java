package net.loyalnetwork.ravenessentials.command.player;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;

public class Feed {

    @Command(value = "feed", permission = RavenPermissions.FEED)
    void feed(ServerPlayer player) {
        Players.feed(player);
    }
}
