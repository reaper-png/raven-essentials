package net.loyalnetwork.ravenessentials.vanish;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.OrSelf;
import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VanishCommand {

    // /vanish [jogador]
    @Command(value = "vanish", aliases = "v", permission = RavenPermissions.VANISH)
    void vanish(CommandSourceStack source, @OrSelf(others = RavenPermissions.VANISH_OTHERS) ServerPlayer target) {
        boolean vanished = !Vanish.isVanished(target);
        Vanish.setVanished(target, vanished);
        Messages.toggled(source, target, "Vanish", vanished);
    }

    // Substitui o /list vanilla para não revelar quem está em vanish. De todos, como o vanilla.
    @Command(value = "list", permission = RavenPermissions.LIST, replaces = true)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /list")
    void list(CommandSourceStack source) {
        List<ServerPlayer> players = listedFor(source);
        long visibleCount = players.stream().filter(player -> !Vanish.isVanished(player)).count();

        Messages.send(source, "Há " + visibleCount + " de " + source.getServer().getMaxPlayers()
                + " jogadores online: " + join(players, Players::name));
    }

    // Substitui o /list uuids vanilla, com a mesma regra do /list.
    @Command(value = "list uuids", permission = RavenPermissions.LIST)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /list")
    void listUuids(CommandSourceStack source) {
        Messages.send(source, "Jogadores online: "
                + join(listedFor(source), player -> Players.name(player) + " (" + player.getStringUUID() + ")"));
    }

    /** Quem aparece na lista para quem rodou: o console e quem tem a permissão veem todos. */
    private static List<ServerPlayer> listedFor(CommandSourceStack source) {
        ServerPlayer viewer = source.getPlayer();
        return source.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> viewer == null || Vanish.canSee(viewer, player))
                .toList();
    }

    private static String join(List<ServerPlayer> players, Function<ServerPlayer, String> label) {
        return players.stream()
                .map(player -> label.apply(player) + (Vanish.isVanished(player) ? " (vanish)" : ""))
                .collect(Collectors.joining(", "));
    }
}
