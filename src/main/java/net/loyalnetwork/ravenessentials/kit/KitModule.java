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
import net.loyalnetwork.ravenessentials.kit.infrastructure.InventoryDelivery;
import net.loyalnetwork.ravenessentials.kit.infrastructure.ItemCodec;
import net.loyalnetwork.ravenessentials.kit.infrastructure.KitClaimRepository;
import net.loyalnetwork.ravenessentials.kit.infrastructure.KitRepository;
import net.loyalnetwork.ravenessentials.kit.menu.ClaimFeedback;
import net.loyalnetwork.ravenessentials.kit.menu.KitListMenu;
import net.loyalnetwork.ravenessentials.kit.menu.KitPreviewMenu;
import net.loyalnetwork.ravenessentials.kit.menu.admin.ChatPrompts;
import net.loyalnetwork.ravenessentials.kit.menu.admin.KitAdminMenu;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitsConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.time.Clock;

/**
 * Monta o sistema de kits (a raiz de composição): liga domínio, banco, config e menus, e
 * entrega aos comandos o que eles recebem por {@code @Receive}.
 */
public final class KitModule {

    private final KitClaimService claims;
    private final KitListMenu list;
    private final KitAdminMenu admin;
    private final ClaimFeedback feedback;

    private KitModule(KitClaimService claims, KitListMenu list, KitAdminMenu admin, ClaimFeedback feedback) {
        this.claims = claims;
        this.list = list;
        this.admin = admin;
        this.feedback = feedback;
    }

    /** Chame do construtor do mod, com o banco já aberto. */
    public static KitModule install(ModContainer container, PermissionManager permissions, Scheduler scheduler) {
        KitsConfig config = CoffeeLib.forMod(container, RavenPaths.ROOT).load(KitsConfig.class);
        ItemCodec codec = new ItemCodec(() -> ServerLifecycleHooks.getCurrentServer().registryAccess());
        CachedKitCatalog catalog = new CachedKitCatalog(
                new KitRepository(RavenDatabase.sessions(), RavenDatabase.database()), codec, scheduler);
        KitClaimRepository history = new KitClaimRepository(RavenDatabase.sessions(), RavenDatabase.database());

        KitClaimService claims = new KitClaimService(catalog, history,
                new InventoryDelivery(() -> config.inventoryFull), scheduler, permissions::has, Clock.systemUTC());
        KitAdminService administration = new KitAdminService(catalog);

        NeoForgeMenus menus = CoffeeLib.menus(container);
        ClaimFeedback feedback = new ClaimFeedback(config);
        KitPreviewMenu preview = new KitPreviewMenu(claims, config, menus, feedback);
        ChatPrompts prompts = new ChatPrompts(scheduler);

        // Os itens precisam dos registros do servidor: os kits só carregam com ele de pé.
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> catalog.load());
        NeoForge.EVENT_BUS.register(prompts);

        return new KitModule(claims,
                new KitListMenu(claims, config, menus, preview, feedback),
                new KitAdminMenu(administration, menus, prompts, scheduler),
                feedback);
    }

    @Provide
    KitClaimService claims() {
        return claims;
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
