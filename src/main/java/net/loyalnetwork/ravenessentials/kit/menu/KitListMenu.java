package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.coffeelib.api.menu.Menu;
import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.kit.application.KitClaimService;
import net.loyalnetwork.ravenessentials.kit.application.KitOffer;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitListMenuConfig;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitLore;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitsConfig;
import net.loyalnetwork.ravenessentials.kit.menu.config.MenuAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** A lista de kits do jogador, com o layout de {@code kits.yml} e páginas quando não cabem. */
public final class KitListMenu {

    private final KitClaimService claims;
    private final KitsConfig config;
    private final NeoForgeMenus menus;
    private final KitPreviewMenu preview;
    private final ClaimFeedback feedback;

    public KitListMenu(KitClaimService claims, KitsConfig config, NeoForgeMenus menus,
                       KitPreviewMenu preview, ClaimFeedback feedback) {
        this.claims = claims;
        this.config = config;
        this.menus = menus;
        this.preview = preview;
        this.feedback = feedback;
    }

    public void open(ServerPlayer player, KitListMode mode) {
        open(player, mode, 0);
    }

    private void open(ServerPlayer player, KitListMode mode, int page) {
        claims.offersFor(player)
                .thenAccept(offers -> show(player, mode, visible(offers), page))
                .exceptionally(error -> feedback.failed(player, error));
    }

    private List<KitOffer> visible(List<KitOffer> offers) {
        if (!config.menu.hideLocked) {
            return offers;
        }
        return offers.stream().filter(offer -> !offer.isLocked()).toList();
    }

    private void show(ServerPlayer player, KitListMode mode, List<KitOffer> offers, int requestedPage) {
        if (player.hasDisconnected()) {
            return;
        }
        KitListMenuConfig layout = config.menu;
        int size = layout.rows * 9;
        List<Integer> slots = layout.kitSlots.stream().filter(slot -> slot >= 0 && slot < size).toList();
        Pages pages = new Pages(offers.size(), slots.size(), requestedPage);

        Menu<ItemStack, ServerPlayer> menu = menus.chest(layout.rows).title(layout.title);
        MenuItems.place(menu, layout.items, size, Placeholders.NONE, player, pages::shows,
                action -> act(player, mode, pages, action));
        for (int i = 0; i < pages.itemsOnPage(); i++) {
            KitOffer offer = offers.get(pages.first() + i);
            menu.item(slots.get(i), icon(offer, mode, player), click -> select(player, mode, pages.current(), offer));
        }
        menu.locked().open(player);
    }

    private ItemStack icon(KitOffer offer, KitListMode mode, ServerPlayer player) {
        KitListMenuConfig layout = config.menu;
        Placeholders values = KitTexts.of(offer.kit(), offer.status()).with("action", mode.hint(layout));
        return MenuItems.dress(offer.kit().appearance().icon(), layout.kitName,
                lore(layout.lore, offer.status()), values, player);
    }

    private static List<String> lore(KitLore lore, KitStatus status) {
        return switch (status) {
            case KitStatus.Available available -> lore.available;
            case KitStatus.OnCooldown cooldown -> lore.cooldown;
            case KitStatus.Locked locked -> lore.locked;
            case KitStatus.Claimed claimed -> lore.claimed;
        };
    }

    private void act(ServerPlayer player, KitListMode mode, Pages pages, MenuAction action) {
        switch (action) {
            case CLOSE, BACK -> player.closeContainer();
            case PREVIOUS_PAGE -> open(player, mode, pages.current() - 1);
            case NEXT_PAGE -> open(player, mode, pages.current() + 1);
            default -> {
            }
        }
    }

    private void select(ServerPlayer player, KitListMode mode, int page, KitOffer offer) {
        Runnable reopen = () -> open(player, mode, page);
        if (mode == KitListMode.VIEW) {
            preview.open(player, offer.kit(), reopen);
            return;
        }
        claims.claim(player, offer.kit().id())
                .thenAccept(result -> {
                    feedback.send(player, result);
                    reopen.run();
                })
                .exceptionally(error -> feedback.failed(player, error));
    }
}
