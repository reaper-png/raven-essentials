package net.loyalnetwork.ravenessentials.home.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * O nome de uma home: minúsculo, sem espaço, curto. É o que se digita no
 * {@code /home <nome>} e, com o dono, a chave no banco. Sem nome, é {@link #DEFAULT}.
 */
public record HomeName(String value) {

    private static final Pattern VALID = Pattern.compile("[a-z0-9][a-z0-9_-]{0,15}");

    /** O {@code /home} e o {@code /sethome} sem nome. */
    public static final HomeName DEFAULT = new HomeName("home");

    public HomeName {
        Objects.requireNonNull(value, "value");
        if (!VALID.matcher(value).matches()) {
            throw new HomeException("Nome de home inválido: até 16 letras minúsculas, números, - ou _, "
                    + "começando com letra ou número.");
        }
    }

    public static HomeName parse(String input) {
        return new HomeName(input.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return value;
    }
}
