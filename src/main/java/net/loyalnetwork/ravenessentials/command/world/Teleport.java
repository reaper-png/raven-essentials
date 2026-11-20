package net.loyalnetwork.ravenessentials.command.world;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.database.PlayerTracker;
import net.loyalnetwork.ravenessentials.util.CurrentDimensionArgument;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class Teleport {

    // /tp <x> <y> <z> [dimensão]   ex.: /tp ~ 100 ~ nether
    // replaces: tira o /tp vanilla (o /teleport vanilla continua). As coordenadas são o argumento
    // nativo do Minecraft: o cliente valida, entende ~ e ^ e sugere a posição sozinho.
    @Command(value = "tp", permission = RavenPermissions.TP, replaces = true)
    void tp(ServerPlayer player,
            @Arg("position") Vec3 position,
            @Arg("dimension") @Default(CurrentDimensionArgument.CURRENT) ServerLevel dimension) {
        PlayerTracker.rememberLocation(player);
        Players.teleport(player, dimension, position.x, position.y, position.z);

        Messages.send(player, String.format(Locale.ROOT, "Teleportado para %.1f %.1f %.1f em %s.",
                position.x, position.y, position.z, dimension.dimension().location()));
    }
}
