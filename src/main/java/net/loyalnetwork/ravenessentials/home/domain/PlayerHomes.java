package net.loyalnetwork.ravenessentials.home.domain;

import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Raiz do agregado: as homes de um jogador. É aqui que vale o limite, que é uma regra
 * sobre todas elas juntas, e que um nome não se repete. Imutável.
 */
public final class PlayerHomes {

    private static final Comparator<HomeName> BY_NAME = Comparator.comparing(HomeName::value);

    public static final PlayerHomes NONE = new PlayerHomes(Map.of());

    private final Map<HomeName, Home> homes;

    private PlayerHomes(Map<HomeName, Home> homes) {
        this.homes = homes;
    }

    public static PlayerHomes of(List<Home> homes) {
        Map<HomeName, Home> byName = new TreeMap<>(BY_NAME);
        for (Home home : homes) {
            byName.put(home.name(), home);
        }
        return new PlayerHomes(byName);
    }

    public Optional<Home> find(HomeName name) {
        return Optional.ofNullable(homes.get(name));
    }

    /** Em ordem de nome. */
    public List<Home> all() {
        return new ArrayList<>(homes.values());
    }

    public int count() {
        return homes.size();
    }

    /**
     * O {@code /sethome}: muda de lugar a home que já existe; senão cria uma nova, se o
     * limite deixar. Mudar de lugar não conta para o limite.
     */
    public SetHome set(HomeName name, CoffeeLocation location, HomeLimit limit) {
        Optional<Home> existing = find(name);
        if (existing.isPresent()) {
            return new SetHome.Moved(existing.get().movedTo(location));
        }
        if (!limit.allowsAnother(count())) {
            return new SetHome.LimitReached(limit);
        }
        return new SetHome.Created(Home.create(name, location));
    }

    /** A home {@code from} com o nome {@code to}; falha se não existe ou se o nome já é de outra. */
    public Home rename(HomeName from, HomeName to) {
        Home home = require(from);
        if (!from.equals(to) && homes.containsKey(to)) {
            throw new HomeException("Você já tem uma home chamada " + to + ".");
        }
        return home.renamed(to);
    }

    public Home require(HomeName name) {
        return find(name).orElseThrow(() -> new HomeException("Você não tem uma home chamada " + name + "."));
    }

    /** Uma cópia com {@code home} (nova ou no lugar da de mesmo nome). */
    public PlayerHomes with(Home home) {
        Map<HomeName, Home> copy = new TreeMap<>(BY_NAME);
        copy.putAll(homes);
        copy.put(home.name(), home);
        return new PlayerHomes(copy);
    }

    public PlayerHomes without(HomeName name) {
        Map<HomeName, Home> copy = new TreeMap<>(BY_NAME);
        copy.putAll(homes);
        copy.remove(name);
        return new PlayerHomes(copy);
    }
}
