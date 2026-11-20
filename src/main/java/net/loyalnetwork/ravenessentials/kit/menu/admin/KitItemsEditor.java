package net.loyalnetwork.ravenessentials.kit.menu.admin;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.kit.application.KitAdminService;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.domain.KitItems;
import net.loyalnetwork.ravenessentials.util.Menus;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Um baú de verdade com os itens do kit: o admin arruma como quiser e, ao fechar, o que
 * ficou dentro passa a ser o kit (na mesma posição). O que se põe aqui sai do inventário
 * do admin; o que se tira, sai do kit.
 */
final class KitItemsEditor {

    private static final int ROWS = 5;

    private final KitAdminService admin;
    private final Scheduler scheduler;

    KitItemsEditor(KitAdminService admin, Scheduler scheduler) {
        this.admin = admin;
        this.scheduler = scheduler;
    }

    /** {@code after} roda depois de salvar, se o admin continuar online e sem outro menu aberto. */
    void open(ServerPlayer player, Kit kit, Runnable after) {
        SimpleContainer contents = new SimpleContainer(KitItems.CAPACITY);
        for (int slot = 0; slot < KitItems.CAPACITY; slot++) {
            contents.setItem(slot, kit.items().at(slot));
        }
        KitId id = kit.id();
        Menus.open(player, "Itens do kit " + id + " (fechar salva)", (window, inventory, viewer) ->
                new EditorMenu(window, inventory, contents, () -> save(player, id, contents, after)));
    }

    private void save(ServerPlayer player, KitId id, SimpleContainer contents, Runnable after) {
        List<ItemStack> slots = new ArrayList<>(KitItems.CAPACITY);
        for (int slot = 0; slot < KitItems.CAPACITY; slot++) {
            slots.add(contents.getItem(slot));
        }
        try {
            Kit saved = admin.change(id, kit -> kit.withItems(KitItems.of(slots)));
            player.sendSystemMessage(Messages.success("Kit " + id + " salvo com " + saved.items().count() + " itens."));
        } catch (KitException e) {
            player.sendSystemMessage(Messages.error(e.getMessage()));
        }
        // Fechar o baú pode ter sido abrir outro menu: só volta se nada mais estiver aberto.
        scheduler.later(0, () -> {
            if (!player.hasDisconnected() && player.containerMenu == player.inventoryMenu) {
                after.run();
            }
        });
    }

    private static final class EditorMenu extends ChestMenu {

        private final Runnable onClose;
        private boolean closed;

        private EditorMenu(int window, Inventory inventory, SimpleContainer contents, Runnable onClose) {
            super(MenuType.GENERIC_9x5, window, inventory, contents, ROWS);
            this.onClose = onClose;
        }

        // Chamado ao fechar, ao abrir outro menu e ao sair do servidor: em todos, salva uma vez.
        @Override
        public void removed(Player player) {
            super.removed(player);
            if (!closed) {
                closed = true;
                onClose.run();
            }
        }
    }
}
