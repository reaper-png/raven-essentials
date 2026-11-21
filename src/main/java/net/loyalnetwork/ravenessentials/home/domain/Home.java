package net.loyalnetwork.ravenessentials.home.domain;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Uma home: onde fica, como aparece no menu e se outros podem visitar.
 * Imutável: cada mudança devolve outra home, e o {@link PlayerHomes} do dono decide se vale.
 *
 * @param icon o id do item do ícone ({@code minecraft:red_bed}); vazio usa o padrão da config
 */
public record Home(HomeName name, CoffeeLocation location, Optional<String> icon, boolean visible) {

    public Home {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(icon, "icon");
    }

    /** Uma home nova: particular e com o ícone padrão. */
    static Home create(HomeName name, CoffeeLocation location) {
        return new Home(name, location, Optional.empty(), false);
    }

    Home movedTo(CoffeeLocation newLocation) {
        return new Home(name, newLocation, icon, visible);
    }

    Home renamed(HomeName newName) {
        return new Home(newName, location, icon, visible);
    }

    public Home withIcon(String itemId) {
        return new Home(name, location, Optional.of(itemId), visible);
    }

    /** Pública: qualquer um pode ir com {@code /home dono:nome}. Particular: só o dono (e a staff). */
    public Home togglePublic() {
        return new Home(name, location, icon, !visible);
    }

    public boolean isPublic() {
        return visible;
    }

    /** Se quem não é o dono pode ir até ela; {@code bypass} é a permissão de ver as de todos. */
    public boolean visitableBy(boolean bypass) {
        return visible || bypass;
    }
}
