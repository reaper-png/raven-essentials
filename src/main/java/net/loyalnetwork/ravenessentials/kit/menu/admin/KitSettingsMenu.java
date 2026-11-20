package net.loyalnetwork.ravenessentials.kit.menu.admin;

import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.kit.application.KitAdminService;
import net.loyalnetwork.ravenessentials.kit.domain.Cooldown;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.menu.KitTexts;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/** As configurações de um kit: itens, ícone, nome, cooldown, acesso e exclusão. */
final class KitSettingsMenu {

    private static final int SUMMARY = 4;
    private static final int ITEMS = 10;
    private static final int ICON = 11;
    private static final int NAME = 12;
    private static final int COOLDOWN = 13;
    private static final int ACCESS = 14;
    private static final int DELETE = 16;
    private static final int BACK = 22;

    private final KitAdminService admin;
    private final NeoForgeMenus menus;
    private final KitItemsEditor editor;
    private final ChatPrompts prompts;
    private final ConfirmMenu confirm;

    KitSettingsMenu(KitAdminService admin, NeoForgeMenus menus, KitItemsEditor editor, ChatPrompts prompts,
                    ConfirmMenu confirm) {
        this.admin = admin;
        this.menus = menus;
        this.editor = editor;
        this.prompts = prompts;
        this.confirm = confirm;
    }

    /** {@code back}: a lista de kits do admin. */
    void open(ServerPlayer player, KitId id, Runnable back) {
        Optional<Kit> found = admin.find(id);
        if (found.isEmpty()) {
            player.sendSystemMessage(Messages.error("O kit " + id + " não existe mais."));
            back.run();
            return;
        }
        Kit kit = found.get();
        Runnable reopen = () -> open(player, id, back);
        String access = kit.rules().restricted() ? "&cSó com permissão" : "&aTodos";
        menus.chest(3)
                .title("&8Kit " + id)
                .item(SUMMARY, AdminIcons.kit(player, kit, "&7Configurações do kit"))
                .item(ITEMS, AdminIcons.button(player, Items.CHEST, "&eItens",
                        "&7Abre um baú com os itens do kit.", "&7Ao fechar, o que estiver dentro vira o kit."),
                        click -> editor.open(player, kit, reopen))
                .item(ICON, AdminIcons.button(player, Items.ITEM_FRAME, "&eÍcone",
                        "&7Usa o item da sua mão como ícone."),
                        click -> useHeldIcon(player, id, reopen))
                .item(NAME, AdminIcons.button(player, Items.NAME_TAG, "&eNome",
                        "&7Atual: &f" + kit.appearance().name(), "&7Clique e digite no chat (aceita &)."),
                        click -> ask(player, id, "Digite o novo nome do kit " + id + ":",
                                Kit::rename, reopen))
                .item(COOLDOWN, AdminIcons.button(player, Items.CLOCK, "&eCooldown",
                        "&7Atual: &f" + KitTexts.cooldown(kit.rules().cooldown()),
                        "&7Clique e digite no chat:", "&f0&7, &funa-vez&7, &f90&7 (segundos), &f1d 12h&7, &f30m"),
                        click -> ask(player, id, "Digite o cooldown do kit " + id + ":",
                                (current, text) -> current.withCooldown(Cooldown.parse(text)), reopen))
                .item(ACCESS, AdminIcons.button(player, Items.TRIPWIRE_HOOK, "&eAcesso: " + access,
                        "&7Restrito: só quem tem &f" + id.permission() + "&7.", "&7Clique para alternar."),
                        click -> change(player, id, Kit::toggleRestriction, reopen))
                .item(DELETE, AdminIcons.button(player, Items.LAVA_BUCKET, "&cExcluir",
                        "&7Apaga o kit e quem já pegou."),
                        click -> confirmDelete(player, id, back, reopen))
                .item(BACK, AdminIcons.button(player, Items.ARROW, "&eVoltar"), click -> back.run())
                .locked()
                .open(player);
    }

    private void useHeldIcon(ServerPlayer player, KitId id, Runnable reopen) {
        ItemStack held = player.getMainHandItem();
        change(player, id, kit -> kit.withIcon(held), reopen);
    }

    /** Pergunta pelo chat e aplica a mudança que a resposta monta. */
    private void ask(ServerPlayer player, KitId id, String question,
                     BiFunction<Kit, String, Kit> edit, Runnable reopen) {
        prompts.ask(player, question, text -> change(player, id, kit -> edit.apply(kit, text), reopen), reopen);
    }

    private void change(ServerPlayer player, KitId id, UnaryOperator<Kit> change, Runnable reopen) {
        try {
            admin.change(id, change);
            player.sendSystemMessage(Messages.success("Kit " + id + " atualizado."));
        } catch (KitException e) {
            player.sendSystemMessage(Messages.error(e.getMessage()));
        }
        reopen.run();
    }

    private void confirmDelete(ServerPlayer player, KitId id, Runnable back, Runnable reopen) {
        confirm.open(player, "&8Excluir o kit " + id + "?", "O kit e os registros de quem pegou somem.",
                () -> {
                    admin.delete(id);
                    player.sendSystemMessage(Messages.success("Kit " + id + " excluído."));
                    back.run();
                },
                reopen);
    }
}
