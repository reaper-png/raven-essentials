package net.loyalnetwork.ravenessentials.kit.menu.admin;

import net.loyalnetwork.coffeelib.api.menu.Menu;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.kit.application.KitAdminService;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.menu.ChatPrompts;
import net.loyalnetwork.ravenessentials.menu.ConfirmMenu;
import net.loyalnetwork.ravenessentials.menu.Pages;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** {@code /kit admin}: todos os kits, para editar, e o botão de criar. */
public final class KitAdminMenu {

    private static final int ROWS = 6;
    private static final int KITS_PER_PAGE = 45;
    private static final int PREVIOUS = 45;
    private static final int CREATE = 49;
    private static final int CLOSE = 50;
    private static final int NEXT = 53;

    private final KitAdminService admin;
    private final NeoForgeMenus menus;
    private final ChatPrompts prompts;
    private final KitSettingsMenu settings;

    public KitAdminMenu(KitAdminService admin, NeoForgeMenus menus, ChatPrompts prompts, Scheduler scheduler) {
        this.admin = admin;
        this.menus = menus;
        this.prompts = prompts;
        this.settings = new KitSettingsMenu(admin, menus, new KitItemsEditor(admin, scheduler), prompts,
                new ConfirmMenu(menus));
    }

    public void open(ServerPlayer player) {
        open(player, 0);
    }

    private void open(ServerPlayer player, int page) {
        List<Kit> kits = admin.all();
        Pages pages = new Pages(kits.size(), KITS_PER_PAGE, page);
        Menu<ItemStack, ServerPlayer> menu = menus.chest(ROWS).title("&8Kits (admin)");
        for (int i = 0; i < pages.itemsOnPage(); i++) {
            Kit kit = kits.get(pages.first() + i);
            menu.item(i, AdminIcons.kit(player, kit, "&eClique para editar"),
                    click -> settings.open(player, kit.id(), () -> open(player, pages.current())));
        }
        navigation(menu, player, pages);
        menu.item(CREATE, AdminIcons.button(player, Items.EMERALD, "&aCriar kit",
                        "&7Clique e digite o id no chat.", "&7Ex.: &finicial&7, &fvip&7, &fsemanal"),
                click -> askForNewKit(player, pages.current()));
        menu.item(CLOSE, AdminIcons.button(player, Items.BARRIER, "&cFechar"), click -> player.closeContainer());
        menu.locked().open(player);
    }

    private void navigation(Menu<ItemStack, ServerPlayer> menu, ServerPlayer player, Pages pages) {
        if (pages.hasPrevious()) {
            menu.item(PREVIOUS, AdminIcons.button(player, Items.ARROW, "&ePágina anterior"),
                    click -> open(player, pages.current() - 1));
        }
        if (pages.hasNext()) {
            menu.item(NEXT, AdminIcons.button(player, Items.ARROW, "&ePróxima página"),
                    click -> open(player, pages.current() + 1));
        }
    }

    private void askForNewKit(ServerPlayer player, int page) {
        prompts.ask(player, "Digite o id do novo kit (letras minúsculas, números, - ou _):",
                text -> create(player, text, page), () -> open(player, page));
    }

    private void create(ServerPlayer player, String text, int page) {
        try {
            Kit kit = admin.create(KitId.parse(text));
            player.sendSystemMessage(Messages.success("Kit " + kit.id() + " criado. Agora coloque os itens."));
            settings.open(player, kit.id(), () -> open(player, page));
        } catch (KitException e) {
            player.sendSystemMessage(Messages.error(e.getMessage()));
            open(player, page);
        }
    }
}
