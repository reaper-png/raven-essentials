package net.loyalnetwork.ravenessentials.home;

import net.loyalnetwork.coffeelib.api.di.annotation.Provide;
import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.cache.ExpiringCache;
import net.loyalnetwork.ravenessentials.core.RavenPaths;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.database.RavenDatabase;
import net.loyalnetwork.ravenessentials.home.application.HomeService;
import net.loyalnetwork.ravenessentials.home.application.HomeVisits;
import net.loyalnetwork.ravenessentials.home.infrastructure.CachedHomeBook;
import net.loyalnetwork.ravenessentials.home.infrastructure.HomeRepository;
import net.loyalnetwork.ravenessentials.home.infrastructure.LocationPositions;
import net.loyalnetwork.ravenessentials.home.infrastructure.PermissionHomeLimits;
import net.loyalnetwork.ravenessentials.home.infrastructure.StoredPlayerLookup;
import net.loyalnetwork.ravenessentials.home.menu.HomeFeedback;
import net.loyalnetwork.ravenessentials.home.menu.HomeListMenu;
import net.loyalnetwork.ravenessentials.home.menu.HomePlaceholders;
import net.loyalnetwork.ravenessentials.home.menu.HomeSettingsMenu;
import net.loyalnetwork.ravenessentials.home.menu.config.HomesConfig;
import net.loyalnetwork.ravenessentials.menu.ChatPrompts;
import net.loyalnetwork.ravenessentials.menu.ConfirmMenu;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Monta o sistema de homes (a raiz de composição): liga domínio, banco, config e menus, e
 * entrega aos comandos o que eles recebem por {@code @Receive}.
 */
public final class HomeModule {

    private final HomeService homes;
    private final HomeVisits visits;
    private final HomeListMenu menu;
    private final HomeFeedback feedback;

    private HomeModule(HomeService homes, HomeVisits visits, HomeListMenu menu, HomeFeedback feedback) {
        this.homes = homes;
        this.visits = visits;
        this.menu = menu;
        this.feedback = feedback;
    }

    /** Chame do construtor do mod, com o banco já aberto. */
    public static HomeModule install(ModContainer container, PermissionManager permissions, Scheduler scheduler,
                                     ChatPrompts prompts) {
        HomesConfig config = CoffeeLib.forMod(container, RavenPaths.ROOT).load(HomesConfig.class);
        // As homes passam por um cache que expira sem uso (sugestões, placeholders, /home na hora).
        CachedHomeBook book = new CachedHomeBook(
                new HomeRepository(RavenDatabase.sessions(), RavenDatabase.database()), ExpiringCache.DEFAULT_IDLE);
        PermissionHomeLimits limits = new PermissionHomeLimits(permissions, () -> config.defaultLimit);
        limits.declare();

        HomeService homes = new HomeService(book, book, new LocationPositions(), limits, scheduler);
        HomeVisits visits = new HomeVisits(book,
                new StoredPlayerLookup(RavenDatabase.playerRepository(), scheduler), homes, scheduler,
                player -> permissions.has(player, RavenPermissions.HOME_OTHERS));

        NeoForgeMenus menus = CoffeeLib.menus(container);
        HomeFeedback feedback = new HomeFeedback(config, homes);
        HomeSettingsMenu settings = new HomeSettingsMenu(homes, config, menus, prompts, new ConfirmMenu(menus), feedback);
        HomeListMenu menu = new HomeListMenu(homes, visits, config, menus, feedback, settings);

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> book.preload(event.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> book.evict(event.getEntity().getUUID()));

        // %ravenessentials_homes_*% e %ravenessentials_home_*% (namespace = id do mod).
        CoffeeLib.placeholders(container).register(new HomePlaceholders(homes));

        return new HomeModule(homes, visits, menu, feedback);
    }

    @Provide
    HomeService homes() {
        return homes;
    }

    @Provide
    HomeVisits visits() {
        return visits;
    }

    @Provide
    HomeListMenu menu() {
        return menu;
    }

    @Provide
    HomeFeedback feedback() {
        return feedback;
    }
}
