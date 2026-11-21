package net.loyalnetwork.ravenessentials.home.command;

import net.loyalnetwork.coffeelib.api.command.SuggestContext;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;
import net.loyalnetwork.coffeelib.api.player.PlayerDirectory;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.home.application.HomeService;
import net.loyalnetwork.ravenessentials.home.application.HomeVisits;
import net.loyalnetwork.ravenessentials.home.domain.HomeException;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.menu.HomeFeedback;
import net.loyalnetwork.ravenessentials.home.menu.HomeListMenu;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@code /home [home|dono:home]}, {@code /sethome [home]}, {@code /delhome <home>} e
 * {@code /homes [jogador]}. O básico fica no comando; o resto (renomear, ícone, pública) no menu.
 */
public class HomeCommand {

    // Sem argumento: a lista das suas homes. Nome de jogador nunca é "-".
    private static final String NONE = "-";

    @Receive
    HomeService homes;

    @Receive
    HomeVisits visits;

    @Receive
    HomeListMenu menu;

    @Receive
    HomeFeedback feedback;

    @Receive
    PlayerDirectory players;

    // /home: a "home". /home casa: a sua casa. /home Renan:casa: a casa (pública) do Renan.
    @Command(value = "home", permission = RavenPermissions.HOME)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /home")
    void home(ServerPlayer player, @Arg("home") @Default("home") @Suggest(provider = "ownHomes") String target) {
        switch (orCommandError(() -> HomeTarget.parse(target))) {
            case HomeTarget.Own own -> homes.go(player, own.name())
                    .thenAccept(trip -> feedback.trip(player, trip, Optional.empty()))
                    .exceptionally(error -> feedback.failed(player, error));
            case HomeTarget.Other other -> visits.visit(player, other.owner(), other.name())
                    .thenAccept(trip -> feedback.trip(player, trip, Optional.of(other.owner())))
                    .exceptionally(error -> feedback.failed(player, error));
        }
    }

    // Uma home que já existe muda de lugar (não conta para o limite).
    @Command(value = "sethome", permission = RavenPermissions.SETHOME)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /sethome")
    void sethome(ServerPlayer player, @Arg("home") @Default("home") @Suggest(provider = "ownHomes") String name) {
        homes.set(player, parse(name))
                .thenAccept(result -> feedback.set(player, result))
                .exceptionally(error -> feedback.failed(player, error));
    }

    @Command(value = "delhome", permission = RavenPermissions.DELHOME)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /delhome")
    void delhome(ServerPlayer player, @Arg("home") @Suggest(provider = "ownHomes") String name) {
        HomeName home = parse(name);
        homes.delete(player, home)
                .thenAccept(found -> feedback.deleted(player, home, found))
                .exceptionally(error -> feedback.failed(player, error));
    }

    // /homes: as suas. /homes <jogador>: as públicas dele (todas, com ravenessentials.home.others).
    @Command(value = "homes", permission = RavenPermissions.HOMES)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /homes")
    void list(ServerPlayer player, @Arg("jogador") @Default(NONE) @Suggest(provider = "onlinePlayers") String owner) {
        if (owner.equals(NONE) || owner.equalsIgnoreCase(player.getGameProfile().getName())) {
            menu.openOwn(player);
            return;
        }
        menu.openOf(player, owner);
    }

    private static HomeName parse(String name) {
        return orCommandError(() -> HomeName.parse(name));
    }

    /** As regras das homes falam em {@link HomeException}; o comando responde em {@link CommandException}. */
    private static <T> T orCommandError(Supplier<T> action) {
        try {
            return action.get();
        } catch (HomeException e) {
            throw new CommandException(e.getMessage());
        }
    }

    // ---- sugestões ----

    Collection<String> ownHomes(SuggestContext context) {
        if (!(context.source().player() instanceof ServerPlayer player)) {
            return List.of();
        }
        return homes.knownHomes(player).all().stream().map(home -> home.name().value()).toList();
    }

    Collection<String> onlinePlayers(SuggestContext context) {
        List<String> names = new ArrayList<>();
        for (Object player : players.visibleTo(context.source().player())) {
            names.add(((ServerPlayer) player).getGameProfile().getName());
        }
        return names;
    }
}
