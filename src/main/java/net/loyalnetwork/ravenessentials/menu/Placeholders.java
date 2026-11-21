package net.loyalnetwork.ravenessentials.menu;

import net.loyalnetwork.coffeelib.api.placeholder.TextContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Valores de {@code {chave}} para os textos da config, e o {@link TextContext} da CoffeeLib
 * com aquilo de que eles falam (o kit, para os {@code %ravenessentials_kit_*%}). Imutável.
 */
public final class Placeholders {

    public static final Placeholders NONE = new Placeholders(Map.of(), TextContext.EMPTY);

    private final Map<String, String> values;
    private final TextContext context;

    private Placeholders(Map<String, String> values, TextContext context) {
        this.values = values;
        this.context = context;
    }

    public Placeholders with(String key, String value) {
        Map<String, String> copy = new HashMap<>(values);
        copy.put(key, value);
        return new Placeholders(Map.copyOf(copy), context);
    }

    /** Os mesmos valores, falando também de {@code subject} (o kit, por exemplo). */
    public Placeholders about(Object subject) {
        return new Placeholders(values, context.with(subject));
    }

    /**
     * Estes valores por cima dos de {@code base} (os daqui ganham num nome repetido), com o
     * contexto daqui, ou o de {@code base} se este não tiver.
     */
    public Placeholders mergedOver(Placeholders base) {
        Map<String, String> copy = new HashMap<>(base.values);
        copy.putAll(values);
        return new Placeholders(Map.copyOf(copy), context.isEmpty() ? base.context : context);
    }

    public String apply(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /** Para o {@code parse} da CoffeeLib: é o que chega nos parâmetros {@code @Context}. */
    public TextContext context() {
        return context;
    }
}
