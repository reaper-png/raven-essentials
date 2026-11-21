package net.loyalnetwork.ravenessentials.teleport.command;

import net.loyalnetwork.coffeelib.api.command.SuggestContext;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.teleport.application.TeleportService;
import net.loyalnetwork.ravenessentials.teleport.message.TeleportFeedback;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * {@code /tpa <jogador>}, {@code /tpaccept [jogador]}, {@code /tpdeny [jogador]},
 * {@code /tpcancel [jogador]} e {@code /tptoggle}. Só lê os argumentos e repassa ao serviço.
 */
public class TeleportRequestCommand {

    // Argumento não informado. Nenhum nome de jogador é "-".
    private static final String NONE = "-";

    @Receive
    TeleportService teleports;

    @Receive
    TeleportFeedback feedback;

    @Command(value = "tpa", permission = RavenPermissions.TPA)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /tpa")
    void tpa(ServerPlayer player, @Arg("player") ServerPlayer target) {
        feedback.send(player, teleports.request(player, target));
    }

    @Command(value = "tpaccept", permission = RavenPermissions.TPACCEPT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /tpaccept")
    void accept(ServerPlayer player,
                @Arg("jogador") @Default(NONE) @Suggest(provider = "incoming") String requester) {
        feedback.send(player, teleports.accept(player, optional(requester)));
    }

    @Command(value = "tpdeny", permission = RavenPermissions.TPDENY)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /tpdeny")
    void deny(ServerPlayer player,
              @Arg("jogador") @Default(NONE) @Suggest(provider = "incoming") String requester) {
        feedback.send(player, teleports.deny(player, optional(requester)));
    }

    @Command(value = "tpcancel", permission = RavenPermissions.TPCANCEL)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /tpcancel")
    void cancel(ServerPlayer player,
                @Arg("jogador") @Default(NONE) @Suggest(provider = "outgoing") String target) {
        feedback.send(player, teleports.cancel(player, optional(target)));
    }

    @Command(value = "tptoggle", permission = RavenPermissions.TPTOGGLE)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /tptoggle")
    void toggle(ServerPlayer player) {
        feedback.send(player, teleports.toggle(player));
    }

    private static Optional<String> optional(String name) {
        return name.equals(NONE) ? Optional.empty() : Optional.of(name);
    }

    Collection<String> incoming(SuggestContext context) {
        return self(context).map(teleports::incomingNames).orElse(List.of());
    }

    Collection<String> outgoing(SuggestContext context) {
        return self(context).map(teleports::outgoingNames).orElse(List.of());
    }

    private static Optional<ServerPlayer> self(SuggestContext context) {
        return context.source().player() instanceof ServerPlayer player ? Optional.of(player) : Optional.empty();
    }
}
