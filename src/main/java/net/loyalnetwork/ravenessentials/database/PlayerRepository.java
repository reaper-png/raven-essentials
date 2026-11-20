package net.loyalnetwork.ravenessentials.database;

import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.HibernateRepository;
import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import org.hibernate.SessionFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository extends HibernateRepository<PlayerEntity, String> {

    public PlayerRepository(SessionFactory factory, Database database) {
        super(factory, database, PlayerEntity.class);
    }

    /**
     * Registra uma entrada: cria o jogador se for a primeira vez e atualiza sessão, nome e IP,
     * numa transação só e com a linha travada ({@code upsert} da CoffeeLib).
     */
    public CompletableFuture<PlayerEntity> recordLogin(String uuid, String name, String ip, Instant now) {
        return upsert(uuid, () -> new PlayerEntity(uuid), player -> player.onLogin(name, ip, now));
    }

    public CompletableFuture<Boolean> recordLogout(String uuid, Instant now) {
        return update(uuid, player -> player.onLogout(now));
    }

    /**
     * Posição antes de um teleporte, para o /back. Como todo {@code update}, a mudança roda na
     * thread do banco: a posição tem de ser capturada antes, na do servidor ({@code Locations.of}).
     */
    public CompletableFuture<Boolean> saveLastLocation(String uuid, CoffeeLocation location) {
        return update(uuid, player -> player.setLastLocation(location));
    }

    public CompletableFuture<Boolean> saveDeathLocation(String uuid, CoffeeLocation location) {
        return update(uuid, player -> player.setDeathLocation(location));
    }

    /** O jogador pelo último nome usado (para o /seen de quem está offline). */
    public CompletableFuture<Optional<PlayerEntity>> findByName(String name) {
        return read(session -> session
                .createSelectionQuery("from PlayerEntity where lower(name) = lower(:name)", PlayerEntity.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .uniqueResultOptional());
    }

    /** Todos os jogadores que já entraram com {@code ip} (para achar contas alternativas). */
    public CompletableFuture<List<PlayerEntity>> findAllByIp(String ip) {
        return read(session -> session
                .createSelectionQuery(
                        "select distinct p from PlayerEntity p join p.ips usage where key(usage) = :ip",
                        PlayerEntity.class)
                .setParameter("ip", ip)
                .list());
    }

    /** Todos os jogadores que já usaram {@code name} (quem troca de nick para fugir de punição). */
    public CompletableFuture<List<PlayerEntity>> findAllByPastName(String name) {
        return read(session -> session
                .createSelectionQuery(
                        "select distinct p from PlayerEntity p join p.names usage where lower(key(usage)) = lower(:name)",
                        PlayerEntity.class)
                .setParameter("name", name)
                .list());
    }
}
