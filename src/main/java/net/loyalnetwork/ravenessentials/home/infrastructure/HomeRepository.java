package net.loyalnetwork.ravenessentials.home.infrastructure;

import jakarta.persistence.LockModeType;
import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.HibernateRepository;
import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeBook;
import net.loyalnetwork.ravenessentials.home.domain.HomeException;
import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.loyalnetwork.ravenessentials.home.domain.SetHome;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** {@link HomeBook} na tabela {@code homes}. */
public class HomeRepository extends HibernateRepository<HomeEntity, HomeKey> implements HomeBook {

    public HomeRepository(SessionFactory sessions, Database database) {
        super(sessions, database, HomeEntity.class);
    }

    @Override
    public CompletableFuture<PlayerHomes> of(UUID owner) {
        return read(session -> homes(rows(session, owner)));
    }

    // A linha do dono em players fica travada até o fim: outro /sethome do mesmo jogador (outro
    // servidor) espera e já vê a home nova, então os dois juntos não passam do limite.
    @Override
    public CompletableFuture<SetHome> set(UUID owner, HomeName name, CoffeeLocation location, HomeLimit limit) {
        return write(session -> {
            PlayerEntity player = session.find(PlayerEntity.class, owner.toString(), LockModeType.PESSIMISTIC_WRITE);
            if (player == null) {
                throw new HomeException("Seus dados ainda estão carregando. Tente de novo em instantes.");
            }
            List<HomeEntity> rows = rows(session, owner);
            SetHome decision = homes(rows).set(name, location, limit);
            switch (decision) {
                case SetHome.Created created -> session.persist(new HomeEntity(key(owner, name), player, created.home()));
                case SetHome.Moved moved -> row(rows, name).update(moved.home());
                case SetHome.LimitReached reached -> {
                }
            }
            return decision;
        });
    }

    @Override
    public CompletableFuture<Void> save(UUID owner, Home home) {
        return write(session -> {
            HomeEntity row = session.find(HomeEntity.class, key(owner, home.name()));
            if (row == null) {
                throw new HomeException("A home " + home.name() + " não existe mais.");
            }
            row.update(home);
            return null;
        });
    }

    // A chave é (dono, nome): trocar o nome é criar a linha nova e apagar a antiga, na mesma transação.
    @Override
    public CompletableFuture<Void> rename(UUID owner, HomeName from, Home renamed) {
        return write(session -> {
            HomeEntity old = session.find(HomeEntity.class, key(owner, from));
            if (old == null) {
                throw new HomeException("A home " + from + " não existe mais.");
            }
            PlayerEntity player = session.getReference(PlayerEntity.class, owner.toString());
            session.remove(old);
            session.flush();
            session.persist(new HomeEntity(key(owner, renamed.name()), player, renamed));
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID owner, HomeName name) {
        return write(session -> {
            HomeEntity row = session.find(HomeEntity.class, key(owner, name));
            if (row == null) {
                return false;
            }
            session.remove(row);
            return true;
        });
    }

    private static List<HomeEntity> rows(Session session, UUID owner) {
        return session.createSelectionQuery("from HomeEntity where key.ownerId = :owner", HomeEntity.class)
                .setParameter("owner", owner.toString())
                .list();
    }

    private static PlayerHomes homes(List<HomeEntity> rows) {
        return PlayerHomes.of(rows.stream().map(HomeEntity::toHome).toList());
    }

    private static HomeEntity row(List<HomeEntity> rows, HomeName name) {
        return rows.stream().filter(row -> row.toHome().name().equals(name)).findFirst().orElseThrow();
    }

    private static HomeKey key(UUID owner, HomeName name) {
        return new HomeKey(owner.toString(), name.value());
    }
}
