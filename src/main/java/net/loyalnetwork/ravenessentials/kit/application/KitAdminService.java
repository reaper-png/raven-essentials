package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitCatalog;
import net.loyalnetwork.ravenessentials.kit.domain.KitClaims;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * Casos de uso de quem administra kits: criar, mudar, excluir, dar e zerar o cooldown.
 * Só na thread do servidor; os futuros devolvidos completam nela também.
 */
public final class KitAdminService {

    private final KitCatalog catalog;
    private final KitClaims claims;
    private final KitDelivery delivery;
    private final Scheduler scheduler;

    public KitAdminService(KitCatalog catalog, KitClaims claims, KitDelivery delivery, Scheduler scheduler) {
        this.catalog = catalog;
        this.claims = claims;
        this.delivery = delivery;
        this.scheduler = scheduler;
    }

    public List<Kit> all() {
        return catalog.all();
    }

    public Optional<Kit> find(KitId id) {
        return catalog.find(id);
    }

    public Kit create(KitId id) {
        if (catalog.find(id).isPresent()) {
            throw new KitException("Já existe um kit chamado " + id + ".");
        }
        Kit kit = Kit.create(id);
        catalog.save(kit);
        return kit;
    }

    /** Aplica {@code change} (uma das mudanças do {@link Kit}) e guarda o resultado. */
    public Kit change(KitId id, UnaryOperator<Kit> change) {
        Kit changed = change.apply(require(id));
        catalog.save(changed);
        return changed;
    }

    /**
     * Troca o id do kit. Quem já pegou continua contando (os registros vão junto), mas o nó
     * de permissão de um kit restrito muda com o id.
     */
    public Kit changeId(KitId from, KitId to) {
        Kit kit = require(from);
        if (from.equals(to)) {
            return kit;
        }
        if (catalog.find(to).isPresent()) {
            throw new KitException("Já existe um kit chamado " + to + ".");
        }
        Kit moved = kit.withId(to);
        catalog.rename(from, moved);
        return moved;
    }

    /**
     * Exclui o kit. Os registros de quem já pegou vão junto (o {@link KitCatalog} garante),
     * então um kit novo com o mesmo nome começa do zero.
     */
    public void delete(KitId id) {
        catalog.remove(id);
    }

    /**
     * Entrega o kit a {@code target} por fora das regras: sem cooldown, sem permissão e sem
     * registro (o jogador continua podendo pegar o kit normalmente). Só respeita o
     * inventário cheio, se a entrega estiver configurada para recusar.
     */
    public Kit give(ServerPlayer target, KitId id) {
        Kit kit = require(id);
        if (!delivery.accepts(target, kit.items())) {
            throw new KitException("O kit " + id + " não cabe no inventário de "
                    + target.getGameProfile().getName() + ".");
        }
        delivery.deliver(target, kit.items());
        return kit;
    }

    /** Zera o cooldown de {@code player} para o kit. O futuro diz se ele já tinha pegado. */
    public CompletableFuture<Boolean> reset(UUID player, KitId id) {
        require(id);
        return scheduler.onMain(claims.reset(player, id));
    }

    private Kit require(KitId id) {
        return catalog.find(id).orElseThrow(() -> new KitException("Não existe um kit chamado " + id + "."));
    }
}
