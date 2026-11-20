package net.loyalnetwork.ravenessentials.kit.application;

import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitCatalog;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/** Casos de uso de quem administra kits: criar, mudar e excluir. Só na thread do servidor. */
public final class KitAdminService {

    private final KitCatalog catalog;

    public KitAdminService(KitCatalog catalog) {
        this.catalog = catalog;
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
        Kit kit = catalog.find(id).orElseThrow(() -> new KitException("O kit " + id + " não existe mais."));
        Kit changed = change.apply(kit);
        catalog.save(changed);
        return changed;
    }

    /**
     * Exclui o kit. Os registros de quem já pegou vão junto (o {@link KitCatalog} garante),
     * então um kit novo com o mesmo nome começa do zero.
     */
    public void delete(KitId id) {
        catalog.remove(id);
    }
}
