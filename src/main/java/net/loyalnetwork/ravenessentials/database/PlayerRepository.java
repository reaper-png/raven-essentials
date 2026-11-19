package net.loyalnetwork.ravenessentials.database;

import net.loyalnetwork.coffeelib.api.database.Database;
import net.loyalnetwork.coffeelib.database.hibernate.HibernateRepository;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository extends HibernateRepository<PlayerEntity, String> {

    public PlayerRepository(SessionFactory factory, Database database) {
        super(factory, database, PlayerEntity.class);
    }

    /**
     * Registra uma entrada: cria o jogador se for a primeira vez, atualiza o nome e o último IP,
     * e acrescenta o IP ao histórico.
     * <p>
     * Tudo numa transação só (write): ler, mudar e salvar em chamadas separadas (findById e depois
     * save) deixaria duas entradas ao mesmo tempo sobrescreverem o histórico uma da outra.
     */
    public CompletableFuture<PlayerEntity> recordLogin(String uuid, String name, String ip) {
        return write(session -> {
            PlayerEntity player = session.find(PlayerEntity.class, uuid);
            if (player == null) {
                player = new PlayerEntity();
                player.setId(uuid);
                session.persist(player);
            }

            // A entidade está gerenciada pela sessão: o Hibernate vê as mudanças e grava no commit,
            // sem precisar chamar save/merge.
            player.setName(name);
            player.recordIp(ip);
            return player;
        });
    }

    /** Todos os jogadores que já entraram com {@code ip} (para achar contas alternativas). */
    public CompletableFuture<List<PlayerEntity>> findAllByUsedIp(String ip) {
        return read(session -> session
                .createSelectionQuery(
                        "select distinct p from PlayerEntity p join p.usedIps usedIp where usedIp = :ip",
                        PlayerEntity.class)
                .setParameter("ip", ip)
                .list());
    }
}
