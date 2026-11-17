package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class VanishCommand {

    // /vanish [jogador]
    @Command(value = "vanish", aliases = "v", permission = RavenPermissions.VANISH)
    void vanish(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String targetName) {
        ServerPlayer target;

        if (targetName.isEmpty()) {
            target = source.getPlayer();
            if (target == null) {
                throw new CommandException("Use /vanish <jogador> pelo console.");
            }
        } else {
            target = source.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                throw new CommandException("Jogador não encontrado: " + targetName);
            }
        }

        boolean self = target == source.getPlayer();
        if (!self && !Vanish.has(source, RavenPermissions.VANISH_OTHERS)) {
            throw new CommandException("Você não tem permissão para alterar o vanish de outros jogadores.");
        }

        boolean vanished = !Vanish.isVanished(target);
        Vanish.setVanished(target, vanished);

        Component status = vanished
                ? Component.literal("Vanish ativado").withStyle(ChatFormatting.GREEN)
                : Component.literal("Vanish desativado").withStyle(ChatFormatting.RED);

        target.sendSystemMessage(status);
        if (!self) {
            source.sendSystemMessage(status.copy().append(Component.literal(" para " + target.getGameProfile().getName())));
        }
    }

    // Substitui o /list vanilla para não revelar quem está em vanish.
    @Command(value = "list", permission = RavenPermissions.LIST)
    void list(CommandSourceStack source) {
        ServerPlayer viewer = source.getPlayer();
        boolean seesAll = viewer == null || Vanish.has(viewer, RavenPermissions.VANISH_SEE);

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> seesAll || !Vanish.isVanished(player))
                .toList();

        String names = players.stream()
                .map(player -> player.getGameProfile().getName() + (Vanish.isVanished(player) ? " (vanish)" : ""))
                .collect(Collectors.joining(", "));

        int visibleCount = (int) players.stream().filter(player -> !Vanish.isVanished(player)).count();

        source.sendSystemMessage(Component.literal(
                "Há " + visibleCount + " de " + source.getServer().getMaxPlayers() + " jogadores online: " + names
        ));
    }

    // Substitui o /list uuids vanilla, com a mesma regra do /list.
    @Command(value = "list uuids", permission = RavenPermissions.LIST)
    void listUuids(CommandSourceStack source) {
        ServerPlayer viewer = source.getPlayer();
        boolean seesAll = viewer == null || Vanish.has(viewer, RavenPermissions.VANISH_SEE);

        String names = source.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> seesAll || !Vanish.isVanished(player))
                .map(player -> player.getGameProfile().getName() + " (" + player.getStringUUID() + ")"
                        + (Vanish.isVanished(player) ? " (vanish)" : ""))
                .collect(Collectors.joining(", "));

        source.sendSystemMessage(Component.literal("Jogadores online: " + names));
    }

    Collection<String> online() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? List.of() : List.of(server.getPlayerNames());
    }
}
