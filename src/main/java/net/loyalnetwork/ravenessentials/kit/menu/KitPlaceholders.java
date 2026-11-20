package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.coffeelib.api.placeholder.annotation.Context;
import net.loyalnetwork.coffeelib.api.placeholder.annotation.Placeholder;
import net.loyalnetwork.ravenessentials.kit.application.KitClaimService;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitStatusTexts;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitsConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Function;

/**
 * Os placeholders de kit na CoffeeLib, no namespace {@code ravenessentials}. Valem em
 * qualquer texto que passe pela CoffeeLib, de qualquer mod (e no PlaceholderAPI, no Paper).
 * <ul>
 *   <li>Plural, com o id do kit no fim: {@code %ravenessentials_kits_<placeholder>_<kit>%}.</li>
 *   <li>Singular, sobre o kit do texto (o {@code TextContext} da CoffeeLib: o ícone, a
 *       prévia, a mensagem de pegar): {@code %ravenessentials_kit_<placeholder>%}. Fora de um
 *       texto de kit, ficam como estão escritos.</li>
 * </ul>
 * Os do jogador respondem pelo que já está em memória: até os registros dele carregarem
 * (logo depois de entrar), ficam como estão escritos. Um id que não é de kit, também.
 */
public final class KitPlaceholders {

    private final KitClaimService claims;
    private final KitsConfig config;

    public KitPlaceholders(KitClaimService claims, KitsConfig config) {
        this.claims = claims;
        this.config = config;
    }

    // ---- plural: %ravenessentials_kits_<placeholder>_<kit>% ----

    @Placeholder("kits_count")
    int count() {
        return claims.kits().size();
    }

    @Placeholder("kits_name")
    String name(String kit) {
        return byId(kit, this::name);
    }

    @Placeholder("kits_id")
    String id(String kit) {
        return byId(kit, this::id);
    }

    @Placeholder("kits_items")
    String items(String kit) {
        return byId(kit, this::items);
    }

    @Placeholder("kits_cooldown")
    String cooldown(String kit) {
        return byId(kit, this::cooldown);
    }

    @Placeholder("kits_uses")
    String uses(String kit) {
        return byId(kit, this::uses);
    }

    @Placeholder("kits_status")
    String status(ServerPlayer player, String kit) {
        return byId(kit, found -> status(player, found));
    }

    @Placeholder("kits_remaining")
    String remaining(ServerPlayer player, String kit) {
        return byId(kit, found -> remaining(player, found));
    }

    @Placeholder("kits_times")
    String times(ServerPlayer player, String kit) {
        return byId(kit, found -> times(player, found));
    }

    // ---- singular: %ravenessentials_kit_<placeholder>%, sobre o kit do texto ----
    // O kit vem do TextContext de quem montou o texto (ícone, prévia, mensagens). Sem kit no
    // contexto, a CoffeeLib nem chama o método, e o placeholder fica como está escrito.

    @Placeholder("kit_name")
    String currentName(@Context Kit kit) {
        return name(kit);
    }

    @Placeholder("kit_id")
    String currentId(@Context Kit kit) {
        return id(kit);
    }

    @Placeholder("kit_items")
    String currentItems(@Context Kit kit) {
        return items(kit);
    }

    @Placeholder("kit_cooldown")
    String currentCooldown(@Context Kit kit) {
        return cooldown(kit);
    }

    @Placeholder("kit_uses")
    String currentUses(@Context Kit kit) {
        return uses(kit);
    }

    @Placeholder("kit_status")
    String currentStatus(ServerPlayer player, @Context Kit kit) {
        return status(player, kit);
    }

    @Placeholder("kit_remaining")
    String currentRemaining(ServerPlayer player, @Context Kit kit) {
        return remaining(player, kit);
    }

    @Placeholder("kit_times")
    String currentTimes(ServerPlayer player, @Context Kit kit) {
        return times(player, kit);
    }

    // ---- o valor de cada um, dado o kit (null deixa o placeholder como está) ----

    private String name(Kit kit) {
        return kit.appearance().name();
    }

    private String id(Kit kit) {
        return kit.id().value();
    }

    private String items(Kit kit) {
        return String.valueOf(kit.items().count());
    }

    private String cooldown(Kit kit) {
        return KitTexts.cooldown(kit.rules().cooldown());
    }

    private String uses(Kit kit) {
        return KitTexts.uses(kit.rules().oneTime());
    }

    private String status(ServerPlayer player, Kit kit) {
        return claims.offerNow(player, kit.id()).map(offer -> statusText(offer.status())).orElse(null);
    }

    private String remaining(ServerPlayer player, Kit kit) {
        return claims.offerNow(player, kit.id()).map(offer -> KitTexts.remaining(offer.status())).orElse(null);
    }

    private String times(ServerPlayer player, Kit kit) {
        return claims.timesNow(player, kit.id()).map(String::valueOf).orElse(null);
    }

    private String statusText(KitStatus status) {
        KitStatusTexts texts = config.status;
        return switch (status) {
            case KitStatus.Available available -> texts.available;
            case KitStatus.OnCooldown cooldown -> texts.cooldown;
            case KitStatus.Locked locked -> texts.locked;
            case KitStatus.Claimed claimed -> texts.claimed;
        };
    }

    // ---- o kit do plural, pelo argumento ----

    // O argumento vem do texto: um id inválido só deixa o placeholder como está.
    private String byId(String kit, Function<Kit, String> value) {
        return parse(kit).flatMap(claims::find).map(value).orElse(null);
    }

    private static Optional<KitId> parse(String kit) {
        try {
            return Optional.of(KitId.parse(kit));
        } catch (KitException e) {
            return Optional.empty();
        }
    }
}
