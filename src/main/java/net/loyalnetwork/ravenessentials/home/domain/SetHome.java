package net.loyalnetwork.ravenessentials.home.domain;

/** O que acontece com um {@code /sethome}. */
public sealed interface SetHome {

    /** Home nova. */
    record Created(Home home) implements SetHome {
    }

    /** A home já existia: só mudou de lugar (ícone e pública continuam). */
    record Moved(Home home) implements SetHome {
    }

    /** Seria uma home nova, mas o jogador já está no limite. */
    record LimitReached(HomeLimit limit) implements SetHome {
    }
}
