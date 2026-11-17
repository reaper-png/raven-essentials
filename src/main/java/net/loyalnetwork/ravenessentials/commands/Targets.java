package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/** Resolve o alvo de comandos com {@code [player]} opcional. */
final class Targets {

    private Targets() {
    }

    /**
     * Nome vazio = quem executou. Outro jogador exige {@code othersPermission}
     * (quando não é nulo).
     */
    static ServerPlayer resolve(CommandSourceStack source, String name, @Nullable String othersPermission) {
        ServerPlayer self = source.getPlayer();

        if (name.isEmpty()) {
            if (self == null) {
                throw new CommandException("Informe um jogador quando usar pelo console.");
            }
            return self;
        }

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            throw new CommandException("Jogador não encontrado: " + name);
        }

        if (target != self && othersPermission != null && !Vanish.has(source, othersPermission)) {
            throw new CommandException("Você não tem permissão para usar isso em outros jogadores.");
        }
        return target;
    }

    static Collection<String> onlineNames() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? List.of() : List.of(server.getPlayerNames());
    }
}
