package net.loyalnetwork.ravenessentials.util;

import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Resolve e sugere jogadores-alvo dos comandos ({@code [player]} opcional, "all"...). */
public final class Targets {

    /** Alvo especial dos comandos que aceitam todo mundo de uma vez. */
    public static final String ALL = "all";

    private Targets() {
    }

    /** Jogador online pelo nome, ou erro de comando. */
    public static ServerPlayer find(MinecraftServer server, String name) {
        ServerPlayer target = server.getPlayerList().getPlayerByName(name);
        if (target == null) {
            throw new CommandException("Jogador não encontrado: " + name);
        }
        return target;
    }

    /**
     * Nome vazio = quem executou. Outro jogador exige {@code othersPermission}
     * (quando não é nulo).
     */
    public static ServerPlayer resolve(CommandSourceStack source, String name, @Nullable String othersPermission) {
        ServerPlayer self = source.getPlayer();

        if (name.isEmpty()) {
            if (self == null) {
                throw new CommandException("Informe um jogador quando usar pelo console.");
            }
            return self;
        }

        ServerPlayer target = find(source.getServer(), name);
        if (target != self && othersPermission != null && !Permissions.has(source, othersPermission)) {
            throw new CommandException("Você não tem permissão para usar isso em outros jogadores.");
        }
        return target;
    }

    /** {@value #ALL} = todos os online; senão, o jogador com esse nome. */
    public static List<ServerPlayer> resolveMany(CommandSourceStack source, String name) {
        if (name.equalsIgnoreCase(ALL)) {
            return source.getServer().getPlayerList().getPlayers();
        }
        return List.of(find(source.getServer(), name));
    }

    /** Se o alvo é quem executou o comando. */
    public static boolean isSelf(CommandSourceStack source, ServerPlayer target) {
        return target == source.getPlayer();
    }

    /**
     * Nomes online para sugestão. Quem está em vanish só aparece para quem pode vê-lo,
     * senão o autocompletar entregaria o vanish.
     */
    public static Collection<String> onlineNames(@Nullable CommandSource viewer) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return List.of();

        ServerPlayer player = viewer != null && viewer.player() instanceof ServerPlayer p ? p : null;
        return server.getPlayerList().getPlayers().stream()
                .filter(target -> player == null || Vanish.canSee(player, target))
                .map(Players::name)
                .toList();
    }

    /** {@link #onlineNames} com {@value #ALL} na frente. */
    public static Collection<String> onlineNamesAndAll(@Nullable CommandSource viewer) {
        List<String> names = new ArrayList<>();
        names.add(ALL);
        names.addAll(onlineNames(viewer));
        return names;
    }
}
