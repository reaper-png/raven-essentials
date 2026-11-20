package net.loyalnetwork.ravenessentials.kit.domain;

/** Uma regra do domínio de kits foi violada. A mensagem é para quem fez a ação. */
public class KitException extends RuntimeException {

    public KitException(String message) {
        super(message);
    }
}
