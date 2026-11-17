package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class GameModeCommand {

    @Command(value = "gm", permission = RavenPermissions.GAMEMODE)
    void gameMode(ServerPlayer player, @Arg("mode") @Suggest({"0", "1", "2", "3"}) int mode) {
        GameType gameType = switch (mode) {
            case 0 -> GameType.SURVIVAL;
            case 1 -> GameType.CREATIVE;
            case 2 -> GameType.ADVENTURE;
            case 3 -> GameType.SPECTATOR;
            default -> throw new CommandException("Modo inválido: " + mode + ". Use 0, 1, 2 ou 3.");
        };

        if (player.setGameMode(gameType)) {
            player.sendSystemMessage(
                    Component.literal("Modo de jogo alterado para " + gameType.getName() + ".")
            );
        }
    }
}
