package net.loyalnetwork.ravenessentials.home.menu;

import net.loyalnetwork.coffeelib.api.menu.Menu;
import net.loyalnetwork.coffeelib.api.menu.MenuClick;
import net.loyalnetwork.coffeelib.api.menu.MenuClickType;
import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.home.application.HomeService;
import net.loyalnetwork.ravenessentials.home.application.HomeTrip;
import net.loyalnetwork.ravenessentials.home.application.HomeVisits;
import net.loyalnetwork.ravenessentials.home.application.OwnerHomes;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.loyalnetwork.ravenessentials.home.menu.config.HomeListMenuConfig;
import net.loyalnetwork.ravenessentials.home.menu.config.HomesConfig;
import net.loyalnetwork.ravenessentials.menu.MenuAction;
import net.loyalnetwork.ravenessentials.menu.MenuItems;
import net.loyalnetwork.ravenessentials.menu.Pages;
import net.loyalnetwork.ravenessentials.menu.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * A lista de homes, com o layout do {@code homes.yml}. As suas: botão esquerdo vai, direito
 * abre as configurações. As de outro jogador (só as públicas, ou todas com a permissão): clique vai.
 */
public final class HomeListMenu {

    private static final Set<MenuClickType> CLICKS = EnumSet.of(
            MenuClickType.LEFT, MenuClickType.RIGHT, MenuClickType.SHIFT_LEFT, MenuClickType.SHIFT_RIGHT);

    private final HomeService homes;
    private final HomeVisits visits;
    private final HomesConfig config;
    private final NeoForgeMenus menus;
    private final HomeFeedback feedback;
    private final HomeSettingsMenu settings;

    public HomeListMenu(HomeService homes, HomeVisits visits, HomesConfig config, NeoForgeMenus menus,
                        HomeFeedback feedback, HomeSettingsMenu settings) {
        this.homes = homes;
        this.visits = visits;
        this.config = config;
        this.menus = menus;
        this.feedback = feedback;
        this.settings = settings;
    }

    /** As homes de quem abriu ({@code /homes}). */
    public void openOwn(ServerPlayer player) {
        openOwn(player, 0);
    }

    private void openOwn(ServerPlayer player, int page) {
        homes.homes(player)
                .thenAccept(found -> showOwn(player, found, page))
                .exceptionally(error -> feedback.failed(player, error));
    }

    /** As homes de {@code ownerName} que {@code viewer} pode ver ({@code /homes <jogador>}). */
    public void openOf(ServerPlayer viewer, String ownerName) {
        openOf(viewer, ownerName, 0);
    }

    private void openOf(ServerPlayer viewer, String ownerName, int page) {
        visits.homesOf(viewer, ownerName)
                .thenAccept(found -> found.ifPresentOrElse(
                        owner -> showOthers(viewer, owner, page),
                        () -> feedback.trip(viewer, new HomeTrip.NoSuchPlayer(ownerName), Optional.of(ownerName))))
                .exceptionally(error -> feedback.failed(viewer, error));
    }

    private void showOwn(ServerPlayer player, PlayerHomes found, int page) {
        HomeListMenuConfig layout = config.menu;
        Placeholders title = HomeTexts.of(found, homes.limit(player));
        show(player, title.apply(layout.title), found.all(), layout.lore, Placeholders.NONE, page,
                next -> openOwn(player, next),
                (click, home) -> {
                    if (click.isRight()) {
                        settings.open(player, home.name(), () -> openOwn(player, page));
                        return;
                    }
                    player.closeContainer();
                    feedback.trip(player, homes.travel(player, home), Optional.empty());
                });
    }

    private void showOthers(ServerPlayer viewer, OwnerHomes owner, int page) {
        HomeListMenuConfig layout = config.menu;
        Placeholders ownerValues = Placeholders.NONE.with("owner", owner.name());
        show(viewer, ownerValues.apply(layout.othersTitle), owner.homes(), layout.othersLore, ownerValues, page,
                next -> openOf(viewer, owner.name(), next),
                (click, home) -> {
                    viewer.closeContainer();
                    feedback.trip(viewer, visits.visit(viewer, owner.owner(), home), Optional.of(owner.name()));
                });
    }

    private void show(ServerPlayer player, String title, List<Home> list, List<String> lore, Placeholders extra,
                      int requestedPage, IntConsumer reopen, BiConsumer<MenuClick<ServerPlayer>, Home> onClick) {
        if (player.hasDisconnected()) {
            return;
        }
        HomeListMenuConfig layout = config.menu;
        int size = layout.rows * 9;
        List<Integer> slots = layout.homeSlots.stream().filter(slot -> slot >= 0 && slot < size).toList();
        Pages pages = new Pages(list.size(), slots.size(), requestedPage);

        Menu<ItemStack, ServerPlayer> menu = menus.chest(layout.rows).title(title);
        MenuItems.place(menu, layout.items, size, Placeholders.NONE, player, pages::shows,
                action -> act(player, pages, action, reopen));
        for (int i = 0; i < pages.itemsOnPage(); i++) {
            Home home = list.get(pages.first() + i);
            ItemStack icon = MenuItems.dress(icon(home, config), layout.homeName, lore,
                    HomeTexts.of(home).mergedOver(extra), player);
            menu.item(slots.get(i), icon, CLICKS, click -> onClick.accept(click, home));
        }
        menu.locked().open(player);
    }

    private static void act(ServerPlayer player, Pages pages, MenuAction action, IntConsumer reopen) {
        switch (action) {
            case CLOSE, BACK -> player.closeContainer();
            case PREVIOUS_PAGE -> reopen.accept(pages.current() - 1);
            case NEXT_PAGE -> reopen.accept(pages.current() + 1);
            default -> {
            }
        }
    }

    /** O ícone escolhido para a home, ou o padrão da config. */
    static ItemStack icon(Home home, HomesConfig config) {
        return MenuItems.stack(home.icon().orElse(config.defaultIcon));
    }
}
