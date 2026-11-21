package net.loyalnetwork.ravenessentials.teleport.message;

import net.loyalnetwork.coffeelib.api.text.TextFormatter;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.menu.Placeholders;
import net.loyalnetwork.ravenessentials.teleport.application.TeleportResult;
import net.loyalnetwork.ravenessentials.teleport.config.TeleportConfig;
import net.loyalnetwork.ravenessentials.teleport.config.TeleportMessages;
import net.loyalnetwork.ravenessentials.teleport.domain.TeleportRequest;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Diz ao jogador, com as mensagens da config, como terminou o pedido de teleporte. */
public final class TeleportFeedback {

    private static final TextFormatter<MutableComponent> TEXT = CoffeeLib.text(RavenEssentials.MODID);

    private final TeleportConfig config;

    public TeleportFeedback(TeleportConfig config) {
        this.config = config;
    }

    public void send(ServerPlayer player, TeleportResult result) {
        TeleportMessages messages = config.messages;
        switch (result) {
            case TeleportResult.Sent sent -> {
                tell(player, messages.sent, about(sent.request().targetName()));
                online(sent.request().target()).ifPresent(target -> received(target, sent.request()));
            }
            case TeleportResult.Accepted accepted -> {
                tell(player, messages.accepted, about(accepted.request().requesterName()));
                online(accepted.request().requester()).ifPresent(requester ->
                        tell(requester, messages.acceptedOther, about(accepted.request().targetName())));
            }
            case TeleportResult.Denied denied -> {
                tell(player, messages.denied, about(denied.request().requesterName()));
                online(denied.request().requester()).ifPresent(requester ->
                        tell(requester, messages.deniedOther, about(denied.request().targetName())));
            }
            case TeleportResult.Cancelled cancelled -> {
                tell(player, messages.cancelled, about(cancelled.request().targetName()));
                online(cancelled.request().target()).ifPresent(target ->
                        tell(target, messages.cancelledOther, about(cancelled.request().requesterName())));
            }
            case TeleportResult.Toggled toggled ->
                    tell(player, toggled.enabled() ? messages.toggleOn : messages.toggleOff, Placeholders.NONE);
            case TeleportResult.Self ignored -> tell(player, messages.self, Placeholders.NONE);
            case TeleportResult.Blocked blocked -> tell(player, messages.blocked, about(blocked.target()));
            case TeleportResult.AlreadySent sent -> tell(player, messages.alreadySent, about(sent.target()));
            case TeleportResult.NoIncoming no -> tell(player,
                    no.player().isPresent() ? messages.noRequestFrom : messages.noRequests,
                    no.player().map(TeleportFeedback::about).orElse(Placeholders.NONE));
            case TeleportResult.NoOutgoing no -> tell(player,
                    no.player().isPresent() ? messages.noSentTo : messages.noSent,
                    no.player().map(TeleportFeedback::about).orElse(Placeholders.NONE));
            case TeleportResult.Failed failed -> tell(player, messages.failed, Placeholders.NONE);
        }
    }

    /** O pedido expirou: avisa quem pediu e quem receberia, se ainda estiverem online. */
    public void expired(TeleportRequest request) {
        online(request.requester()).ifPresent(player ->
                tell(player, config.messages.expiredOther, about(request.targetName())));
        online(request.target()).ifPresent(player ->
                tell(player, config.messages.expired, about(request.requesterName())));
    }

    public void self(ServerPlayer player) {
        tell(player, config.messages.self, Placeholders.NONE);
    }

    public void failed(ServerPlayer player) {
        tell(player, config.messages.failed, Placeholders.NONE);
    }

    public void here(ServerPlayer actor, String targetName, UUID targetId) {
        tell(actor, config.messages.here, about(targetName));
        online(targetId).ifPresent(target ->
                tell(target, config.messages.hereOther, about(Players.name(actor))));
    }

    public void all(ServerPlayer actor, List<UUID> moved) {
        if (moved.isEmpty()) {
            tell(actor, config.messages.allEmpty, Placeholders.NONE);
            return;
        }

        tell(actor, config.messages.all, Placeholders.NONE.with("count", String.valueOf(moved.size())));
        String actorName = Players.name(actor);
        for (UUID id : moved) {
            online(id).ifPresent(target -> tell(target, config.messages.allOther, about(actorName)));
        }
    }

    private void received(ServerPlayer target, TeleportRequest request) {
        Placeholders values = about(request.requesterName());
        tell(target, config.messages.received, values);

        MutableComponent accept = button(target, config.messages.acceptButton, config.messages.acceptHover,
                values, "/tpaccept " + request.requesterName());
        MutableComponent deny = button(target, config.messages.denyButton, config.messages.denyHover,
                values, "/tpdeny " + request.requesterName());
        target.sendSystemMessage(accept.append(" ").append(deny));
    }

    private static MutableComponent button(ServerPlayer viewer, String text, String hover, Placeholders values,
                                           String command) {
        MutableComponent label = TEXT.parse(values.apply(text), viewer, values.context());
        Component tooltip = TEXT.parse(values.apply(hover), viewer, values.context());
        return label.withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }

    private static Placeholders about(String player) {
        return Placeholders.NONE.with("player", player);
    }

    private static void tell(ServerPlayer player, String message, Placeholders values) {
        if (player.hasDisconnected()) {
            return;
        }
        player.sendSystemMessage(TEXT.parse(values.apply(message), player, values.context()));
    }

    private static Optional<ServerPlayer> online(UUID id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return Optional.empty();
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(id)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }
}
