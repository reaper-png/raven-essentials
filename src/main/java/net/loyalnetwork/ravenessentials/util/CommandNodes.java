package net.loyalnetwork.ravenessentials.util;

import com.mojang.brigadier.tree.CommandNode;

import java.lang.reflect.Field;
import java.util.Map;

/** Mexe na árvore do Brigadier, que não tem API para tirar nós. */
public final class CommandNodes {

    private static final String[] CHILD_MAPS = {"children", "literals", "arguments"};

    private CommandNodes() {
    }

    /** Tira o filho {@code name} de {@code node} (ex.: o /tp vanilla da raiz). */
    public static void remove(CommandNode<?> node, String name) {
        for (String fieldName : CHILD_MAPS) {
            try {
                Field field = CommandNode.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                ((Map<?, ?>) field.get(node)).remove(name);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Não foi possível remover o nó de comando " + name, e);
            }
        }
    }
}
