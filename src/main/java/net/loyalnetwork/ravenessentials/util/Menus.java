package net.loyalnetwork.ravenessentials.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuConstructor;

/** Abre menus virtuais (bigorna, crafting, lixeira...) sem bloco no mundo. */
public final class Menus {

    private Menus() {
    }

    public static void open(ServerPlayer player, Component title, MenuConstructor menu) {
        player.openMenu(new SimpleMenuProvider(menu, title));
    }

    public static void open(ServerPlayer player, String title, MenuConstructor menu) {
        open(player, Component.literal(title), menu);
    }
}
