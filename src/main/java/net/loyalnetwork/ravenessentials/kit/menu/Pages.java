package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.ravenessentials.kit.menu.config.MenuAction;

/** Qual parte de uma lista cabe numa página do menu. */
public final class Pages {

    private final int total;
    private final int perPage;
    private final int current;

    /** {@code requested} fora do intervalo vai para a primeira ou a última página. */
    public Pages(int total, int perPage, int requested) {
        this.total = total;
        this.perPage = Math.max(1, perPage);
        this.current = Math.clamp(requested, 0, count() - 1);
    }

    private int count() {
        return Math.max(1, (total + perPage - 1) / perPage);
    }

    public int current() {
        return current;
    }

    public int first() {
        return current * perPage;
    }

    public int itemsOnPage() {
        return Math.max(0, Math.min(perPage, total - first()));
    }

    public boolean hasPrevious() {
        return current > 0;
    }

    public boolean hasNext() {
        return current < count() - 1;
    }

    /** Os botões de página só aparecem quando há para onde ir. */
    public boolean shows(MenuAction action) {
        return switch (action) {
            case PREVIOUS_PAGE -> hasPrevious();
            case NEXT_PAGE -> hasNext();
            default -> true;
        };
    }
}
