package net.loyalnetwork.ravenessentials.kit.command;

import net.loyalnetwork.coffeelib.api.command.SuggestContext;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.kit.application.KitClaimService;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.menu.ClaimFeedback;
import net.loyalnetwork.ravenessentials.kit.menu.KitListMenu;
import net.loyalnetwork.ravenessentials.kit.menu.KitListMode;
import net.loyalnetwork.ravenessentials.kit.menu.admin.KitAdminMenu;
import net.loyalnetwork.ravenessentials.util.Permissions;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/** {@code /kit}, {@code /kit view}, {@code /kit claim [kit]} e {@code /kit admin}. Só abre menus e repassa. */
public class KitCommand {

    // Sem argumento, o /kit claim abre o menu. Nenhum id de kit começa com "-".
    private static final String MENU = "-";

    @Receive
    KitClaimService claims;

    @Receive
    KitListMenu list;

    @Receive
    KitAdminMenu admin;

    @Receive
    ClaimFeedback feedback;

    @Command(value = "kit", aliases = "kits", permission = RavenPermissions.KIT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /kit")
    void kit(ServerPlayer player) {
        list.open(player, KitListMode.VIEW);
    }

    @Command(value = "kit view", permission = RavenPermissions.KIT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /kit")
    void view(ServerPlayer player) {
        list.open(player, KitListMode.VIEW);
    }

    @Command(value = "kit claim", permission = RavenPermissions.KIT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /kit")
    void claim(ServerPlayer player, @Arg("kit") @Default(MENU) @Suggest(provider = "kits") String kit) {
        if (kit.equals(MENU)) {
            list.open(player, KitListMode.CLAIM);
            return;
        }
        claims.claim(player, parse(kit))
                .thenAccept(result -> feedback.send(player, result))
                .exceptionally(error -> feedback.failed(player, error));
    }

    @Command(value = "kit admin", permission = RavenPermissions.KIT_ADMIN)
    void admin(ServerPlayer player) {
        admin.open(player);
    }

    private static KitId parse(String kit) {
        try {
            return KitId.parse(kit);
        } catch (KitException e) {
            throw new CommandException(e.getMessage());
        }
    }

    // Só os kits que quem digita pode usar.
    Collection<String> kits(SuggestContext context) {
        Object player = context.source().player();
        return claims.kits().stream()
                .filter(kit -> kit.allows(node -> Permissions.has(player, node)))
                .map(kit -> kit.id().value())
                .toList();
    }
}
