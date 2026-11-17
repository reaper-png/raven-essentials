package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClearChat {

    private static final int CLEAR_LINES = 100;

    @Command(value = "clearchat", permission = RavenPermissions.CLEARCHAT)
    void clearChat(CommandSourceStack source) {
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {

            // Envia linhas vazias para limpar o histórico visual.
            for (int i = 0; i < CLEAR_LINES; i++) {
                player.sendSystemMessage(Component.empty());
            }

            player.sendSystemMessage(Component.literal("O chat foi limpo pela equipe."));
        }
    }
}
