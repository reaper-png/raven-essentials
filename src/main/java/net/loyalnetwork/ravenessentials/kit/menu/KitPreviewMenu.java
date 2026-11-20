package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.coffeelib.api.menu.Menu;
import net.loyalnetwork.coffeelib.neoforge.menu.NeoForgeMenus;
import net.loyalnetwork.ravenessentials.kit.application.ClaimResult;
import net.loyalnetwork.ravenessentials.kit.application.KitClaimService;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitItems;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitPreviewMenuConfig;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitsConfig;
import net.loyalnetwork.ravenessentials.kit.menu.config.MenuAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Os itens de um kit, só para ver, com os botões da config (pegar, voltar...). */
public final class KitPreviewMenu {

    private static final int ROWS = 6;

    private final KitClaimService claims;
    private final KitsConfig config;
    private final NeoForgeMenus menus;
    private final ClaimFeedback feedback;

    public KitPreviewMenu(KitClaimService claims, KitsConfig config, NeoForgeMenus menus, ClaimFeedback feedback) {
        this.claims = claims;
        this.config = config;
        this.menus = menus;
        this.feedback = feedback;
    }

    /** {@code back} é para onde o botão de voltar leva (e para onde se vai depois de pegar). */
    public void open(ServerPlayer player, Kit kit, Runnable back) {
        KitPreviewMenuConfig layout = config.preview;
        Placeholders values = KitTexts.of(kit);
        Menu<ItemStack, ServerPlayer> menu = menus.chest(ROWS).title(values.apply(layout.title));
        for (int slot = 0; slot < KitItems.CAPACITY; slot++) {
            ItemStack stack = kit.items().at(slot);
            if (!stack.isEmpty()) {
                menu.item(slot, stack);
            }
        }
        MenuItems.place(menu, layout.items, ROWS * 9, values, player, action -> true,
                action -> act(player, kit, action, back));
        menu.locked().open(player);
    }

    private void act(ServerPlayer player, Kit kit, MenuAction action, Runnable back) {
        switch (action) {
            case CLOSE -> player.closeContainer();
            case BACK -> back.run();
            case CLAIM -> claim(player, kit, back);
            default -> {
            }
        }
    }

    private void claim(ServerPlayer player, Kit kit, Runnable back) {
        claims.claim(player, kit.id())
                .thenAccept(result -> {
                    feedback.send(player, result);
                    if (result instanceof ClaimResult.Claimed && !player.hasDisconnected()) {
                        back.run();
                    }
                })
                .exceptionally(error -> feedback.failed(player, error));
    }
}
