package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;

/** Como terminou um pedido para ir até uma home. Quem mostra ao jogador é a apresentação. */
public sealed interface HomeTrip {

    record Arrived(Home home) implements HomeTrip {
    }

    record NoSuchHome(HomeName name) implements HomeTrip {
    }

    /** O dono da home ({@code /home dono:nome}) nunca entrou no servidor. */
    record NoSuchPlayer(String name) implements HomeTrip {
    }

    /** A home é particular e quem pediu não é o dono nem tem a permissão de ver todas. */
    record Private(Home home) implements HomeTrip {
    }

    /** A dimensão da home não existe mais (mundo apagado, mod removido). */
    record WorldMissing(Home home) implements HomeTrip {
    }
}
