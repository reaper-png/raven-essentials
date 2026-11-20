package net.loyalnetwork.ravenessentials.kit.infrastructure;

import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.HibernateRepository;
import org.hibernate.SessionFactory;

import java.util.concurrent.CompletableFuture;

/** A tabela {@code kits}. Quem usa é o {@link CachedKitCatalog}. */
public class KitRepository extends HibernateRepository<KitEntity, String> {

    public KitRepository(SessionFactory sessions, Database database) {
        super(sessions, database, KitEntity.class);
    }

    /**
     * Troca o id de um kit numa transação só: cria a linha nova, passa os registros de
     * {@code kit_claims} para ela e apaga a antiga. Nessa ordem a chave estrangeira sempre
     * tem para onde apontar, e o ON DELETE CASCADE da antiga não leva nenhum registro.
     */
    public CompletableFuture<Void> rename(String from, KitEntity renamed) {
        return write(session -> {
            session.persist(renamed);
            session.flush();
            session.createNativeMutationQuery("update kit_claims set kit_id = :to where kit_id = :from")
                    .setParameter("to", renamed.id())
                    .setParameter("from", from)
                    .executeUpdate();
            KitEntity old = session.find(KitEntity.class, from);
            if (old != null) {
                session.remove(old);
            }
            return null;
        });
    }
}
