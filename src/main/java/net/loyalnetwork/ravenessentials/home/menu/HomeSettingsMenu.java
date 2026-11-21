package net.loyalnetwork.ravenessentials.home.menu;

import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.home.application.HomeService;
import net.loyalnetwork.ravenessentials.home.domain.Home;
import net.loyalnetwork.ravenessentials.home.domain.HomeException;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.menu.config.HomesConfig;
import net.loyalnetwork.ravenessentials.menu.Buttons;
import net.loyalnetwork.ravenessentials.menu.ChatPrompts;
import net.loyalnetwork.ravenessentials.menu.ConfirmMenu;
import net.loyalnetwork.ravenessentials.menu.MenuItems;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/** As configurações de uma home: ir, mover, renomear, ícone, pública e apagar. */
public final class HomeSettingsMenu {

    private static final int SUMMARY = 4;
    private static final int GO = 10;
    private static final int MOVE = 11;
    private static final int RENAME = 12;
    private static final int ICON = 13;
    private static final int VISIBILITY = 14;
    private static final int DELETE = 16;
    private static final int BACK = 22;

    private final HomeService homes;
    private final HomesConfig config;
    private final NeoForgeMenus menus;
    private final ChatPrompts prompts;
    private final ConfirmMenu confirm;
    private final HomeFeedback feedback;

    public HomeSettingsMenu(HomeService homes, HomesConfig config, NeoForgeMenus menus, ChatPrompts prompts,
                            ConfirmMenu confirm, HomeFeedback feedback) {
        this.homes = homes;
        this.config = config;
        this.menus = menus;
        this.prompts = prompts;
        this.confirm = confirm;
        this.feedback = feedback;
    }

    /** {@code back}: a lista de homes. */
    public void open(ServerPlayer player, HomeName name, Runnable back) {
        // Pelo cache com volta ao banco: se a cópia expirou desde a lista, a home não "some".
        homes.homes(player)
                .thenAccept(found -> show(player, name, found.find(name), back))
                .exceptionally(error -> feedback.failed(player, error));
    }

    private void show(ServerPlayer player, HomeName name, Optional<Home> found, Runnable back) {
        if (player.hasDisconnected()) {
            return;
        }
        if (found.isEmpty()) {
            player.sendSystemMessage(Messages.error("A home " + name + " não existe mais."));
            back.run();
            return;
        }
        Home home = found.get();
        Runnable reopen = () -> open(player, name, back);
        menus.chest(3)
                .title("&8Home " + name)
                .context(HomeTexts.of(home).context())
                .item(SUMMARY, summary(player, home))
                .item(GO, Buttons.button(player, Items.ENDER_PEARL, "&aIr"), click -> go(player, home))
                .item(MOVE, Buttons.button(player, Items.COMPASS, "&eMover para cá",
                        "&7A home passa a ser onde você está agora."), click -> moveHere(player, name, reopen))
                .item(RENAME, Buttons.button(player, Items.NAME_TAG, "&eRenomear",
                        "&7Clique e digite o nome novo no chat."), click -> askForName(player, name, back, reopen))
                .item(ICON, Buttons.button(player, Items.ITEM_FRAME, "&eÍcone",
                        "&7Usa o item da sua mão como ícone."), click -> useHeldIcon(player, name, reopen))
                .item(VISIBILITY, Buttons.button(player, home.isPublic() ? Items.OAK_DOOR : Items.IRON_DOOR,
                        "&ePública: " + (home.isPublic() ? "&asim" : "&cnão"),
                        "&7Pública: qualquer um vai com &f/home " + player.getGameProfile().getName() + ":" + name + "&7.",
                        "&7Clique para alternar."), click -> change(player, name, Home::togglePublic, reopen))
                .item(DELETE, Buttons.button(player, Items.LAVA_BUCKET, "&cApagar"),
                        click -> confirmDelete(player, name, back, reopen))
                .item(BACK, Buttons.button(player, Items.ARROW, "&eVoltar"), click -> back.run())
                .locked()
                .open(player);
    }

    private ItemStack summary(ServerPlayer player, Home home) {
        return MenuItems.dress(HomeListMenu.icon(home, config), "&e{home}", List.of(
                "&7Mundo: &f{world}", "&7Posição: &f{x}, {y}, {z}", "&7Pública: &f{public}"),
                HomeTexts.of(home), player);
    }

    private void go(ServerPlayer player, Home home) {
        player.closeContainer();
        feedback.trip(player, homes.travel(player, home), Optional.empty());
    }

    private void moveHere(ServerPlayer player, HomeName name, Runnable reopen) {
        homes.set(player, name)
                .thenAccept(result -> {
                    feedback.set(player, result);
                    reopen.run();
                })
                .exceptionally(error -> feedback.failed(player, error));
    }

    // Com o nome novo, o menu reabre já nele; se não der, volta para a mesma home.
    private void askForName(ServerPlayer player, HomeName name, Runnable back, Runnable reopen) {
        prompts.ask(player, "Digite o novo nome da home " + name + ":", text -> {
            HomeName to;
            try {
                to = HomeName.parse(text);
            } catch (HomeException e) {
                player.sendSystemMessage(Messages.error(e.getMessage()));
                reopen.run();
                return;
            }
            homes.rename(player, name, to)
                    .thenAccept(renamed -> {
                        player.sendSystemMessage(Messages.success("Home " + name + " agora é " + renamed.name() + "."));
                        open(player, renamed.name(), back);
                    })
                    .exceptionally(error -> {
                        feedback.failed(player, error);
                        reopen.run();
                        return null;
                    });
        }, reopen);
    }

    private void useHeldIcon(ServerPlayer player, HomeName name, Runnable reopen) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(Messages.error("Segure um item para usar como ícone."));
            reopen.run();
            return;
        }
        String id = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        change(player, name, home -> home.withIcon(id), reopen);
    }

    private void change(ServerPlayer player, HomeName name, UnaryOperator<Home> change, Runnable reopen) {
        homes.change(player, name, change)
                .thenAccept(changed -> {
                    player.sendSystemMessage(Messages.success("Home " + name + " atualizada."));
                    reopen.run();
                })
                .exceptionally(error -> feedback.failed(player, error));
    }

    private void confirmDelete(ServerPlayer player, HomeName name, Runnable back, Runnable reopen) {
        confirm.open(player, "&8Apagar a home " + name + "?", "A home some de vez.",
                () -> homes.delete(player, name)
                        .thenAccept(found -> {
                            feedback.deleted(player, name, found);
                            back.run();
                        })
                        .exceptionally(error -> feedback.failed(player, error)),
                reopen);
    }
}
