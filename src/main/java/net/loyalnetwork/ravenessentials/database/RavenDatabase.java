package net.loyalnetwork.ravenessentials.database;

import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.CoffeeHibernate;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.core.RavenPaths;
import net.loyalnetwork.ravenessentials.home.infrastructure.HomeEntity;
import net.loyalnetwork.ravenessentials.kit.infrastructure.KitClaimEntity;
import net.loyalnetwork.ravenessentials.kit.infrastructure.KitEntity;
import net.neoforged.fml.ModContainer;
import org.hibernate.SessionFactory;

/** Abre o banco e o Hibernate, e guarda os repositórios. Entidade nova: adicione em {@link #ENTITIES}. */
public final class RavenDatabase {

    private static final Class<?>[] ENTITIES = {PlayerEntity.class, KitEntity.class, KitClaimEntity.class,
            HomeEntity.class};

    private static Database database;
    private static SessionFactory sessions;
    private static PlayerRepository playerRepository;

    private RavenDatabase() {
    }

    public static void open(ModContainer container) {
        database = CoffeeLib.database(container, RavenPaths.ROOT).open();
        sessions = CoffeeHibernate.sessionFactory(database, ENTITIES);
        playerRepository = new PlayerRepository(sessions, database);
    }

    public static Database database() {
        return database;
    }

    public static SessionFactory sessions() {
        return sessions;
    }

    public static PlayerRepository playerRepository() {
        return playerRepository;
    }
}
