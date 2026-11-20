package net.loyalnetwork.ravenessentials.kit;

import net.loyalnetwork.coffeelib.api.di.annotation.Provide;
import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.core.RavenPaths;
import net.loyalnetwork.ravenessentials.database.RavenDatabase;
import net.loyalnetwork.ravenessentials.kit.application.KitAdminService;
import net.loyalnetwork.ravenessentials.kit.application.KitClaimService;
import net.loyalnetwork.ravenessentials.kit.infrastructure.CachedKitCatalog;
import net.loyalnetwork.ravenessentials.kit.infrastructure.CachedKitClaims;
import net.loyalnetwork.ravenessentials.kit.infrastructure.InventoryDelivery;
import net.loyalnetwork.ravenessentials.kit.infrastructure.ItemCodec;
import net.loyalnetwork.ravenessentials.kit.infrastructure.KitClaimRepository;
import net.loyalnetwork.ravenessentials.kit.infrastructure.KitRepository;
import net.loyalnetwork.ravenessentials.kit.menu.ClaimFeedback;
import net.loyalnetwork.ravenessentials.kit.menu.KitListMenu;
import net.loyalnetwork.ravenessentials.kit.menu.KitPlaceholders;
import net.loyalnetwork.ravenessentials.kit.menu.KitPreviewMenu;
import net.loyalnetwork.ravenessentials.kit.menu.admin.ChatPrompts;
import net.loyalnetwork.ravenessentials.kit.menu.admin.KitAdminMenu;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitsConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.time.Clock;

/**
 * Monta o sistema de kits (a raiz de composição): liga domínio, banco, config e menus, e
 * entrega aos comandos o que eles recebem por {@code @Receive}.
 */
public final class KitModule {

    private final KitClaimService claims;
    private final KitAdminService administration;
    private final KitListMenu list;
    private final KitPreviewMenu preview;
    private final KitAdminMenu admin;
    private final ClaimFeedback feedback;

    private KitModule(KitClaimService claims, KitAdminService administration, KitListMenu list,
                      KitPreviewMenu preview, KitAdminMenu admin, ClaimFeedback feedback) {
        this.claims = claims;
        this.administration = administration;
        this.list = list;
        this.preview = preview;
        this.admin = admin;
        this.feedback = feedback;
    }

    /** Chame do construtor do mod, com o banco já aberto. */
    public static KitModule install(ModContainer container, PermissionManager permissions, Scheduler scheduler) {
        KitsConfig config = CoffeeLib.forMod(container, RavenPaths.ROOT).load(KitsConfig.class);
        ItemCodec codec = new ItemCodec(() -> ServerLifecycleHooks.getCurrentServer().registryAccess());
        CachedKitCatalog catalog = new CachedKitCatalog(
                new KitRepository(RavenDatabase.sessions(), RavenDatabase.database()), codec, scheduler);
        // Os registros passam pela cópia em memória de quem está online (para os placeholders).
        CachedKitClaims history = new CachedKitClaims(
                new KitClaimRepository(RavenDatabase.sessions(), RavenDatabase.database()));

        InventoryDelivery delivery = new InventoryDelivery(() -> config.inventoryFull);
        KitClaimService claims = new KitClaimService(catalog, history, history, delivery, scheduler,
                permissions::has, Clock.systemUTC());
        KitAdminService administration = new KitAdminService(catalog, history, delivery, scheduler);

        NeoForgeMenus menus = CoffeeLib.menus(container);
        ClaimFeedback feedback = new ClaimFeedback(config);
        KitPreviewMenu preview = new KitPreviewMenu(claims, config, menus, feedback);
        ChatPrompts prompts = new ChatPrompts(scheduler);

        // Os itens precisam dos registros do servidor: os kits só carregam com ele de pé.
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> catalog.load());
        NeoForge.EVENT_BUS.register(prompts);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) ->
                history.preload(event.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                history.evict(event.getEntity().getUUID()));

        // %ravenessentials_kits_<placeholder>_<kit>% (namespace = id do mod).
        CoffeeLib.placeholders(container).register(new KitPlaceholders(claims, config));

        return new KitModule(claims, administration,
                new KitListMenu(claims, config, menus, preview, feedback),
                preview,
                new KitAdminMenu(administration, menus, prompts, scheduler),
                feedback);
    }

    @Provide
    KitClaimService claims() {
        return claims;
    }

    @Provide
    KitAdminService administration() {
        return administration;
    }

    @Provide
    KitPreviewMenu preview() {
        return preview;
    }

    @Provide
    KitListMenu list() {
        return list;
    }

    @Provide
    KitAdminMenu admin() {
        return admin;
    }

    @Provide
    ClaimFeedback feedback() {
        return feedback;
    }
}
