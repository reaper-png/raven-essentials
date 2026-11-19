package net.loyalnetwork.ravenessentials.command.chat;

import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Greedy;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.CommandNodes;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Targets;
import net.loyalnetwork.ravenessentials.util.TextFormat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public class Title {

    private static final String SUBTITLE_SEPARATOR = "\\n";

    private static final int FADE_IN = 10;
    private static final int STAY = 70;
    private static final int FADE_OUT = 20;

    // /title <jogador|all> <título>[\n<subtítulo>]
    @Command(value = "title", permission = RavenPermissions.TITLE)
    void title(
            CommandSourceStack source,
            @Arg("target") @Suggest(provider = "targets") String target,
            @Arg("message") @Greedy String message
    ) {
        List<ServerPlayer> players = Targets.resolveMany(source, target);

        // O jogador digita "\n" literal (barra + n) no chat para separar o subtítulo.
        int split = message.indexOf(SUBTITLE_SEPARATOR);
        Component title = TextFormat.parse(split < 0 ? message : message.substring(0, split));
        Component subtitle = split < 0 ? null
                : TextFormat.parse(message.substring(split + SUBTITLE_SEPARATOR.length()));

        for (ServerPlayer player : players) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(FADE_IN, STAY, FADE_OUT));
            if (subtitle != null) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            }
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }

        Messages.send(source, "Title enviado para " + players.size() + " jogador(es).");
    }

    Collection<String> targets(CommandSource source) {
        return Targets.onlineNamesAndAll(source);
    }

    /**
     * Tira o /title vanilla antes da CoffeeLib registrar o nosso; senão o Brigadier juntaria
     * os dois nós e os argumentos do vanilla competiriam com os nossos.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void removeVanillaTitle(RegisterCommandsEvent event) {
        CommandNodes.remove(event.getDispatcher().getRoot(), "title");
    }
}
