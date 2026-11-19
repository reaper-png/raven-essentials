package net.loyalnetwork.ravenessentials.database;

import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.CoffeeHibernate;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import org.hibernate.SessionFactory;

import java.nio.file.Path;

/** Abre o banco e o Hibernate, e guarda os repositórios. Entidade nova: adicione em {@link #ENTITIES}. */
public final class RavenDatabase {

    // database.yml e o arquivo do banco ficam aqui.
    private static final Path DIRECTORY = FMLPaths.GAMEDIR.get().resolve("raven/essentials");

    private static final Class<?>[] ENTITIES = {PlayerEntity.class};

    private static Database database;
    private static SessionFactory sessions;
    private static PlayerRepository playerRepository;

    private RavenDatabase() {
    }

    public static void open(ModContainer container) {
        database = CoffeeLib.database(container, DIRECTORY).open();
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
