package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.ravenessentials.kit.menu.config.KitListMenuConfig;

/** O que um clique num kit da lista faz. */
public enum KitListMode {
    /** Abre a prévia ({@code /kit view}). */
    VIEW,
    /** Pega na hora ({@code /kit claim}). */
    CLAIM;

    String hint(KitListMenuConfig config) {
        return this == VIEW ? config.viewHint : config.claimHint;
    }
}
