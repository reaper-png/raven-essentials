package net.loyalnetwork.ravenessentials.kit.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * O identificador de um kit: minúsculo, sem espaço, curto. É o que se digita no
 * {@code /kit claim <kit>}, a chave no banco e o fim do nó de permissão do kit.
 */
public record KitId(String value) {

    private static final Pattern VALID = Pattern.compile("[a-z0-9][a-z0-9_-]{0,31}");
    private static final String PERMISSION_PREFIX = "ravenessentials.kits.";

    public KitId {
        Objects.requireNonNull(value, "value");
        if (!VALID.matcher(value).matches()) {
            throw new KitException("Nome de kit inválido: até 32 letras minúsculas, números, - ou _, começando com letra ou número.");
        }
    }

    /** O que o jogador digitou, normalizado. */
    public static KitId parse(String input) {
        return new KitId(input.trim().toLowerCase(Locale.ROOT));
    }

    /** O nó que libera um kit restrito. */
    public String permission() {
        return PERMISSION_PREFIX + value;
    }

    @Override
    public String toString() {
        return value;
    }
}
