package net.loyalnetwork.ravenessentials.command.chat;

import net.loyalnetwork.coffeelib.api.command.SuggestContext;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Greedy;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.coffeelib.api.player.PlayerDirectory;
import net.loyalnetwork.coffeelib.api.text.TextFormatter;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Title {

    private static final String ALL = "all";
    private static final String SUBTITLE_SEPARATOR = "\\n";

    private static final int FADE_IN = 10;
    private static final int STAY = 70;
    private static final int FADE_OUT = 20;

    private static final TextFormatter<MutableComponent> TEXT = CoffeeLib.text(RavenEssentials.MODID);

    @Receive
    PlayerDirectory players;

    // /title <jogador|all> <título>[\n<subtítulo>]
    // replaces: tira o /title vanilla, cujos argumentos competiriam com os nossos.
    @Command(value = "title", permission = RavenPermissions.TITLE, replaces = true)
    void title(
            CommandSourceStack source,
            @Arg("target") @Suggest(provider = "targets") String target,
            @Arg("message") @Greedy String message
    ) {
        List<ServerPlayer> recipients = recipients(source, target);

        // O jogador digita "\n" literal (barra + n) no chat para separar o subtítulo.
        int split = message.indexOf(SUBTITLE_SEPARATOR);
        Component title = TEXT.parse(split < 0 ? message : message.substring(0, split));
        Component subtitle = split < 0 ? null : TEXT.parse(message.substring(split + SUBTITLE_SEPARATOR.length()));

        for (ServerPlayer player : recipients) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(FADE_IN, STAY, FADE_OUT));
            if (subtitle != null) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            }
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }

        Messages.send(source, "Title enviado para " + recipients.size() + " jogador(es).");
    }

    /** {@value #ALL} = todos os online; senão o jogador, entre os que quem rodou pode ver. */
    private List<ServerPlayer> recipients(CommandSourceStack source, String target) {
        List<ServerPlayer> online = source.getServer().getPlayerList().getPlayers();
        if (target.equalsIgnoreCase(ALL)) {
            return online;
        }
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(target);
        if (player == null || !players.canSee(source.getPlayer(), player)) {
            throw new CommandException("Jogador não encontrado: " + target);
        }
        return List.of(player);
    }

    Collection<String> targets(SuggestContext context) {
        List<String> names = new ArrayList<>();
        names.add(ALL);
        for (Object player : players.visibleTo(context.source().player())) {
            names.add(((ServerPlayer) player).getGameProfile().getName());
        }
        return names;
    }
}
