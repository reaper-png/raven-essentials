package net.loyalnetwork.ravenessentials.menu;

import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

/** Sim ou não, para ações que não têm volta. */
public final class ConfirmMenu {

    private static final int YES = 11;
    private static final int NO = 15;

    private final NeoForgeMenus menus;

    public ConfirmMenu(NeoForgeMenus menus) {
        this.menus = menus;
    }

    public void open(ServerPlayer player, String question, String consequence, Runnable yes, Runnable no) {
        menus.chest(3)
                .title(question)
                .item(YES, Buttons.button(player, Items.LIME_CONCRETE, "&aConfirmar", "&7" + consequence),
                        click -> yes.run())
                .item(NO, Buttons.button(player, Items.RED_CONCRETE, "&cCancelar"), click -> no.run())
                .locked()
                .open(player);
    }
}
