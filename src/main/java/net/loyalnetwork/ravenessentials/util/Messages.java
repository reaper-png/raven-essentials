package net.loyalnetwork.ravenessentials.util;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/** Mensagens de chat no padrão do mod: normal, sucesso (verde), erro (vermelho) e aviso (cinza). */
public final class Messages {

    private Messages() {
    }

    public static MutableComponent success(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GREEN);
    }

    public static MutableComponent error(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }

    public static MutableComponent info(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    public static void send(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }

    public static void send(CommandSourceStack source, String text) {
        source.sendSystemMessage(Component.literal(text));
    }

    /**
     * "{feature} ativado/desativado" para o alvo e, se quem rodou foi outro, o mesmo
     * com " para {alvo}" para quem rodou.
     */
    public static void toggled(CommandSourceStack source, ServerPlayer target, String feature, boolean enabled) {
        Component status = enabled ? success(feature + " ativado") : error(feature + " desativado");
        target.sendSystemMessage(status);
        if (!Targets.isSelf(source, target)) {
            source.sendSystemMessage(status.copy().append(" para " + Players.name(target)));
        }
    }

    /** {@code selfMessage} para o alvo e, se quem rodou foi outro, {@code othersMessage} para quem rodou. */
    public static void notify(CommandSourceStack source, ServerPlayer target, String selfMessage, String othersMessage) {
        send(target, selfMessage);
        if (!Targets.isSelf(source, target)) {
            send(source, othersMessage);
        }
    }
}
