package net.loyalnetwork.ravenessentials.kit.command;

/**
 * O que o {@code /kit view} pediu, pela quantidade de argumentos: nenhum é a lista, um é o
 * kit (para quem digitou), dois são o jogador e o kit.
 */
sealed interface ViewRequest {

    /** O valor de um argumento que não foi digitado. */
    String NONE = "-";

    record List() implements ViewRequest {
    }

    record Own(String kit) implements ViewRequest {
    }

    record ForPlayer(String player, String kit) implements ViewRequest {
    }

    static ViewRequest of(String first, String second) {
        if (first.equals(NONE)) {
            return new List();
        }
        if (second.equals(NONE)) {
            return new Own(first);
        }
        return new ForPlayer(first, second);
    }
}
