package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Spawn {

    // Spawn do mundo (overworld), o mesmo de /setworldspawn.
    @Command(value = "spawn", permission = RavenPermissions.SPAWN)
    void spawn(ServerPlayer player) {
        var level = player.server.overworld();
        var pos = level.getSharedSpawnPos();

        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getSharedSpawnAngle(), 0F);
        player.sendSystemMessage(Component.literal("Teleportado para o spawn."));
    }
}
