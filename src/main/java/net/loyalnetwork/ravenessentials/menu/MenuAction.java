package net.loyalnetwork.ravenessentials.menu;

/** O que um item de menu faz ao ser clicado. Cada menu atende só as que fazem sentido nele. */
public enum MenuAction {
    NONE,
    CLOSE,
    /** Volta para o menu anterior (na prévia, a lista de kits). */
    BACK,
    /** Pega o kit (na prévia). */
    CLAIM,
    PREVIOUS_PAGE,
    NEXT_PAGE
}
