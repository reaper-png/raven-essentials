package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class Slime {

    // Mesmo sal que o vanilla usa para decidir onde slimes nascem.
    private static final long SLIME_SALT = 987234911L;

    @Command(value = "slime", permission = RavenPermissions.SLIME)
    void slime(ServerPlayer player) {
        var level = player.serverLevel();

        if (level.dimension() != Level.OVERWORLD) {
            player.sendSystemMessage(Component.literal("Slime chunks só existem no overworld.").withStyle(ChatFormatting.RED));
            return;
        }

        var chunk = player.chunkPosition();
        boolean slimeChunk = WorldgenRandom.seedSlimeChunk(chunk.x, chunk.z, level.getSeed(), SLIME_SALT).nextInt(10) == 0;

        player.sendSystemMessage(slimeChunk
                ? Component.literal("Você está em uma slime chunk!").withStyle(ChatFormatting.GREEN)
                : Component.literal("Você não está em uma slime chunk.").withStyle(ChatFormatting.RED));
    }
}
