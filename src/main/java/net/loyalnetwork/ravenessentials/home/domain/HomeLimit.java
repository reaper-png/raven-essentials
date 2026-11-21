package net.loyalnetwork.ravenessentials.home.domain;

/** Quantas homes um jogador pode ter. {@link #UNLIMITED} não tem teto. */
public record HomeLimit(int max) {

    private static final int NO_LIMIT = -1;

    public static final HomeLimit UNLIMITED = new HomeLimit(NO_LIMIT);

    public HomeLimit {
        if (max < NO_LIMIT) {
            throw new IllegalArgumentException("max must be -1 (unlimited) or more: " + max);
        }
    }

    public boolean isUnlimited() {
        return max == NO_LIMIT;
    }

    /** Se quem já tem {@code count} homes pode criar mais uma. */
    boolean allowsAnother(int count) {
        return isUnlimited() || count < max;
    }
}
