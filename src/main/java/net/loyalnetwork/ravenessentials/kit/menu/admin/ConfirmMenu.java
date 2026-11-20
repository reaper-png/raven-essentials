package net.loyalnetwork.ravenessentials.kit.menu.admin;

import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

/** Sim ou não, para ações que não têm volta. */
final class ConfirmMenu {

    private static final int YES = 11;
    private static final int NO = 15;

    private final NeoForgeMenus menus;

    ConfirmMenu(NeoForgeMenus menus) {
        this.menus = menus;
    }

    void open(ServerPlayer player, String question, String consequence, Runnable yes, Runnable no) {
        menus.chest(3)
                .title(question)
                .item(YES, AdminIcons.button(player, Items.LIME_CONCRETE, "&aConfirmar", "&7" + consequence),
                        click -> yes.run())
                .item(NO, AdminIcons.button(player, Items.RED_CONCRETE, "&cCancelar"), click -> no.run())
                .locked()
                .open(player);
    }
}
