package net.loyalnetwork.ravenessentials.kit.infrastructure;

import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.HibernateRepository;
import org.hibernate.SessionFactory;

/** A tabela {@code kits}. Quem usa é o {@link CachedKitCatalog}. */
public class KitRepository extends HibernateRepository<KitEntity, String> {

    public KitRepository(SessionFactory sessions, Database database) {
        super(sessions, database, KitEntity.class);
    }
}
