package net.loyalnetwork.ravenessentials.home.domain;

/** Uma regra das homes foi violada. A mensagem é para quem fez a ação. */
public class HomeException extends RuntimeException {

    public HomeException(String message) {
        super(message);
    }
}
