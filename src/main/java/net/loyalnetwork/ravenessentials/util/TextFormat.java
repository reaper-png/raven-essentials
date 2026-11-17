package net.loyalnetwork.ravenessentials.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Converte texto com códigos de cor em Component:
 * {@code &0-&f} cores, {@code &k-&o} formatações, {@code &r} reset e {@code &#RRGGBB} hex.
 * Aceita {@code §} no lugar de {@code &}. Como no vanilla, uma cor nova limpa as formatações.
 */
public final class TextFormat {

    private TextFormat() {
    }

    public static MutableComponent parse(String text) {
        MutableComponent result = Component.empty();
        StringBuilder buffer = new StringBuilder();
        Style style = Style.EMPTY;

        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);

            if ((c == '&' || c == '§') && i + 1 < text.length()) {
                char code = text.charAt(i + 1);

                // &#RRGGBB
                if (code == '#' && i + 8 <= text.length()) {
                    Integer rgb = parseHex(text.substring(i + 2, i + 8));
                    if (rgb != null) {
                        flush(result, buffer, style);
                        style = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
                        i += 8;
                        continue;
                    }
                }

                ChatFormatting format = ChatFormatting.getByCode(Character.toLowerCase(code));
                if (format != null) {
                    flush(result, buffer, style);
                    if (format == ChatFormatting.RESET) {
                        style = Style.EMPTY;
                    } else if (format.isColor()) {
                        style = Style.EMPTY.withColor(format);
                    } else {
                        style = style.applyFormat(format);
                    }
                    i += 2;
                    continue;
                }
            }

            buffer.append(c);
            i++;
        }

        flush(result, buffer, style);
        return result;
    }

    private static Integer parseHex(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void flush(MutableComponent result, StringBuilder buffer, Style style) {
        if (buffer.isEmpty()) return;
        result.append(Component.literal(buffer.toString()).setStyle(style));
        buffer.setLength(0);
    }
}
