package net.loyalnetwork.ravenessentials.kit.menu;

import java.util.HashMap;
import java.util.Map;

/** Valores de {@code {chave}} para os textos da config. Imutável. */
public final class Placeholders {

    public static final Placeholders NONE = new Placeholders(Map.of());

    private final Map<String, String> values;

    private Placeholders(Map<String, String> values) {
        this.values = values;
    }

    public Placeholders with(String key, String value) {
        Map<String, String> copy = new HashMap<>(values);
        copy.put(key, value);
        return new Placeholders(Map.copyOf(copy));
    }

    public String apply(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
