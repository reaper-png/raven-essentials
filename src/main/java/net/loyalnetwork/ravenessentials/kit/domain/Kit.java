package net.loyalnetwork.ravenessentials.kit.domain;

import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Raiz do agregado: um kit, com o que entrega ({@link KitItems}), como aparece
 * ({@link KitAppearance}) e quem pega e quando ({@link KitRules}).
 * <p>
 * Imutável: cada mudança devolve outro kit, e quem guarda (o {@link KitCatalog}) decide
 * quando ele passa a valer.
 */
public final class Kit {

    private final KitId id;
    private final KitAppearance appearance;
    private final KitRules rules;
    private final KitItems items;

    private Kit(KitId id, KitAppearance appearance, KitRules rules, KitItems items) {
        this.id = Objects.requireNonNull(id, "id");
        this.appearance = Objects.requireNonNull(appearance, "appearance");
        this.rules = Objects.requireNonNull(rules, "rules");
        this.items = Objects.requireNonNull(items, "items");
    }

    /** Um kit novo: sem itens, sem cooldown, liberado para todos e com o id como nome. */
    public static Kit create(KitId id) {
        return new Kit(id, KitAppearance.defaultFor(id), KitRules.FREE, KitItems.EMPTY);
    }

    /** Um kit como estava guardado. */
    public static Kit restore(KitId id, KitAppearance appearance, KitRules rules, KitItems items) {
        return new Kit(id, appearance, rules, items);
    }

    public KitId id() {
        return id;
    }

    public KitAppearance appearance() {
        return appearance;
    }

    public KitRules rules() {
        return rules;
    }

    public KitItems items() {
        return items;
    }

    public Kit rename(String name) {
        return new Kit(id, appearance.rename(name), rules, items);
    }

    public Kit withIcon(ItemStack icon) {
        return new Kit(id, appearance.withIcon(icon), rules, items);
    }

    public Kit withItems(KitItems newItems) {
        return new Kit(id, appearance, rules, newItems);
    }

    public Kit withCooldown(Cooldown cooldown) {
        return new Kit(id, appearance, rules.withCooldown(cooldown), items);
    }

    public Kit toggleRestriction() {
        return new Kit(id, appearance, rules.toggleRestriction(), items);
    }

    /** Se quem tem as permissões de {@code hasPermission} pode usar este kit. */
    public boolean allows(Predicate<String> hasPermission) {
        return !rules.restricted() || hasPermission.test(id.permission());
    }

    /** Se pode pegar agora, dado o último registro do jogador para este kit. */
    public ClaimDecision claim(Optional<KitClaim> previous, Instant now) {
        return rules.decide(previous, now);
    }

    /** {@link #allows} e {@link #claim} juntos, para mostrar no menu. */
    public KitStatus statusFor(Predicate<String> hasPermission, Optional<KitClaim> previous, Instant now) {
        if (!allows(hasPermission)) {
            return new KitStatus.Locked();
        }
        return switch (claim(previous, now)) {
            case ClaimDecision.Granted granted -> new KitStatus.Available();
            case ClaimDecision.OnCooldown cooldown -> new KitStatus.OnCooldown(cooldown.remaining());
            case ClaimDecision.AlreadyClaimed claimed -> new KitStatus.Claimed();
        };
    }
}
