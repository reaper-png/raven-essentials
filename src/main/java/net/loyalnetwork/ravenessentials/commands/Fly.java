package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.server.level.ServerPlayer;

public class Fly {

    @Command(value = "fly", permission = RavenPermissions.FLY)
    void fly(ServerPlayer player) {
        var abilities = player.getAbilities();
        abilities.mayfly = !abilities.mayfly;
        player.onUpdateAbilities();
    }
}
