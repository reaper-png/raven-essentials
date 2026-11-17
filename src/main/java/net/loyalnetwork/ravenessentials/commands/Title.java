package net.loyalnetwork.ravenessentials.commands;

import com.mojang.brigadier.tree.CommandNode;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Greedy;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.RavenPermissions;
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
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public class Title {

    private static final String ALL = "todos";
    private static final String SUBTITLE_SEPARATOR = "\\n";

    private static final int FADE_IN = 10;
    private static final int STAY = 70;
    private static final int FADE_OUT = 20;

    // /title <jogador|todos> <título>[\n<subtítulo>]
    @Command(value = "title", permission = RavenPermissions.TITLE)
    void title(
            CommandSourceStack source,
            @Arg("target") @Suggest(provider = "targets") String target,
            @Arg("message") @Greedy String message
    ) {
        List<ServerPlayer> players;

        if (target.equalsIgnoreCase(ALL)) {
            players = source.getServer().getPlayerList().getPlayers();
        } else {
            ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(target);
            if (player == null) {
                throw new CommandException("Jogador não encontrado: " + target);
            }
            players = List.of(player);
        }

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

        source.sendSystemMessage(Component.literal("Title enviado para " + players.size() + " jogador(es)."));
    }

    Collection<String> targets() {
        List<String> names = new ArrayList<>();
        names.add(ALL);
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            names.addAll(List.of(server.getPlayerNames()));
        }
        return names;
    }

    /**
     * Tira o /title vanilla antes da CoffeeLib registrar o nosso; senão o Brigadier juntaria
     * os dois nós e os argumentos do vanilla competiriam com os nossos.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void removeVanillaTitle(RegisterCommandsEvent event) {
        removeChild(event.getDispatcher().getRoot(), "title");
    }

    private static void removeChild(CommandNode<?> node, String name) {
        for (String fieldName : new String[]{"children", "literals", "arguments"}) {
            try {
                Field field = CommandNode.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                ((Map<?, ?>) field.get(node)).remove(name);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Não foi possível remover o /" + name + " vanilla", e);
            }
        }
    }
}
