package net.loyalnetwork.ravenessentials.command.chat;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class Colors {

    @Command(value = "colors", permission = RavenPermissions.COLORS)
    void colors(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("========== CORES DO MINECRAFT ==========")
                        .withStyle(ChatFormatting.GOLD)
        );

        sendColors(player);

        player.sendSystemMessage(
                Component.literal("========== FORMATAÇÕES ==========")
                        .withStyle(ChatFormatting.GOLD)
        );

        sendFormatting(player);

        player.sendSystemMessage(
                Component.literal("========== HEX COLORS ==========")
                        .withStyle(ChatFormatting.GOLD)
        );

        player.sendSystemMessage(
                Component.literal("Formato: &#RRGGBB")
                        .withStyle(ChatFormatting.YELLOW)
        );

        player.sendSystemMessage(
                Component.literal("Exemplo: &#ff0000")
                        .withStyle(style -> style.withColor(0xFF0000))
        );

        player.sendSystemMessage(
                Component.literal("O formato HEX permite utilizar")
                        .withStyle(ChatFormatting.GRAY)
        );

        player.sendSystemMessage(
                Component.literal("16.777.216 cores diferentes!")
                        .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void sendColors(ServerPlayer player) {
        ChatFormatting[] colors = {
                ChatFormatting.BLACK,
                ChatFormatting.DARK_BLUE,
                ChatFormatting.DARK_GREEN,
                ChatFormatting.DARK_AQUA,
                ChatFormatting.DARK_RED,
                ChatFormatting.DARK_PURPLE,
                ChatFormatting.GOLD,
                ChatFormatting.GRAY,
                ChatFormatting.DARK_GRAY,
                ChatFormatting.BLUE,
                ChatFormatting.GREEN,
                ChatFormatting.AQUA,
                ChatFormatting.RED,
                ChatFormatting.LIGHT_PURPLE,
                ChatFormatting.YELLOW,
                ChatFormatting.WHITE
        };

        String[] codes = {
                "&0", "&1", "&2", "&3",
                "&4", "&5", "&6", "&7",
                "&8", "&9", "&a", "&b",
                "&c", "&d", "&e", "&f"
        };

        for (int i = 0; i < colors.length; i++) {
            MutableComponent line = Component.literal(codes[i] + " ")
                    .withStyle(ChatFormatting.GRAY);

            line.append(
                    Component.literal(colors[i].getName())
                            .withStyle(colors[i])
            );

            player.sendSystemMessage(line);
        }
    }

    private static void sendFormatting(ServerPlayer player) {
        ChatFormatting[] formats = {
                ChatFormatting.OBFUSCATED,
                ChatFormatting.BOLD,
                ChatFormatting.STRIKETHROUGH,
                ChatFormatting.UNDERLINE,
                ChatFormatting.ITALIC,
                ChatFormatting.RESET
        };

        String[] codes = {
                "&k", "&l", "&m", "&n", "&o", "&r"
        };

        for (int i = 0; i < formats.length; i++) {
            MutableComponent line = Component.literal(codes[i] + " ")
                    .withStyle(ChatFormatting.GRAY);

            line.append(
                    Component.literal(formats[i].getName())
                            .withStyle(formats[i])
            );

            player.sendSystemMessage(line);
        }
    }
}
