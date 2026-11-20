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
import net.loyalnetwork.coffeelib.api.player.PlayerDirectory;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.database.PlayerRepository;
import net.loyalnetwork.ravenessentials.kit.application.KitAdminService;
import net.loyalnetwork.ravenessentials.kit.application.KitClaimService;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitException;
import net.loyalnetwork.ravenessentials.kit.domain.KitId;
import net.loyalnetwork.ravenessentials.kit.menu.ClaimFeedback;
import net.loyalnetwork.ravenessentials.kit.menu.KitListMenu;
import net.loyalnetwork.ravenessentials.kit.menu.KitListMode;
import net.loyalnetwork.ravenessentials.kit.menu.KitPreviewMenu;
import net.loyalnetwork.ravenessentials.kit.menu.admin.KitAdminMenu;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Permissions;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /kit}, {@code /kit view [jogador] [kit]}, {@code /kit claim [kit]},
 * {@code /kit give <jogador> <kit>}, {@code /kit reset <jogador> <kit>} e {@code /kit admin}.
 * Só lê os argumentos, abre menus e repassa para os serviços.
 */
public class KitCommand {

    // Argumento não informado. Nenhum id de kit começa com "-", nem nome de jogador.
    private static final String NONE = ViewRequest.NONE;

    @Receive
    KitClaimService claims;

    @Receive
    KitAdminService administration;

    @Receive
    KitListMenu list;

    @Receive
    KitPreviewMenu preview;

    @Receive
    KitAdminMenu admin;

    @Receive
    ClaimFeedback feedback;

    @Receive
    PlayerDirectory players;

    @Receive
    PlayerRepository playerRepository;

    @Receive
    Scheduler scheduler;

    @Command(value = "kit", aliases = "kits", permission = RavenPermissions.KIT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /kit")
    void kit(ServerPlayer player) {
        list.open(player, KitListMode.VIEW);
    }

    // /kit view: a lista. /kit view <kit>: a prévia, para quem digitou.
    // /kit view <jogador> <kit>: a prévia, aberta para o jogador (NPC, console, staff).
    // Opcional antes de obrigatório não existe no Brigadier: são dois opcionais, lidos pela quantidade.
    @Command(value = "kit view", permission = RavenPermissions.KIT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /kit")
    void view(CommandSourceStack source,
              @Arg("jogador-ou-kit") @Default(NONE) @Suggest(provider = "playersOrKits") String first,
              @Arg("kit") @Default(NONE) @Suggest(provider = "kits") String second) {
        switch (ViewRequest.of(first, second)) {
            case ViewRequest.List all -> list.open(self(source), KitListMode.VIEW);
            case ViewRequest.Own own -> showPreview(self(source), own.kit());
            case ViewRequest.ForPlayer forPlayer -> showPreviewFor(source, forPlayer);
        }
    }

    private void showPreviewFor(CommandSourceStack source, ViewRequest.ForPlayer request) {
        ServerPlayer target = visiblePlayer(source, request.player());
        boolean other = target != source.getPlayer();
        if (other && !Permissions.has(source, RavenPermissions.KIT_VIEW_OTHERS)) {
            throw new CommandException("Você não pode abrir kits para outros jogadores.");
        }
        showPreview(target, request.kit());
        if (other) {
            Messages.send(source, "Prévia do kit " + request.kit() + " aberta para " + Players.name(target) + ".");
        }
    }

    @Command(value = "kit claim", permission = RavenPermissions.KIT)
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /kit")
    void claim(ServerPlayer player, @Arg("kit") @Default(NONE) @Suggest(provider = "kits") String kit) {
        if (kit.equals(NONE)) {
            list.open(player, KitListMode.CLAIM);
            return;
        }
        claims.claim(player, parse(kit))
                .thenAccept(result -> feedback.send(player, result))
                .exceptionally(error -> feedback.failed(player, error));
    }

    // Por fora das regras: sem cooldown, sem permissão do kit e sem contar como pego.
    @Command(value = "kit give", permission = RavenPermissions.KIT_GIVE)
    void give(CommandSourceStack source, @Arg("player") ServerPlayer target,
              @Arg("kit") @Suggest(provider = "allKits") String kit) {
        Kit given = orCommandError(() -> administration.give(target, parse(kit)));
        feedback.received(target, given);
        if (target != source.getPlayer()) {
            Messages.send(source, "Kit " + given.id() + " entregue para " + Players.name(target) + ".");
        }
    }

    // Aceita quem está offline: procura pelo último nome usado no servidor.
    @Command(value = "kit reset", permission = RavenPermissions.KIT_RESET)
    CompletableFuture<Void> reset(CommandSourceStack source,
                                  @Arg("player") @Suggest(provider = "onlinePlayers") String name,
                                  @Arg("kit") @Suggest(provider = "allKits") String kit) {
        KitId id = parse(kit);
        orCommandError(() -> administration.find(id)
                .orElseThrow(() -> new KitException("Não existe um kit chamado " + id + ".")));
        return uuidOf(source, name)
                .thenCompose(uuid -> administration.reset(uuid, id))
                .thenAccept(found -> Messages.send(source, found
                        ? "Cooldown do kit " + id + " zerado para " + name + "."
                        : name + " ainda não tinha pegado o kit " + id + "."));
    }

    @Command(value = "kit admin", permission = RavenPermissions.KIT_ADMIN)
    void admin(ServerPlayer player) {
        admin.open(player);
    }

    // ---- argumentos ----

    private void showPreview(ServerPlayer target, String kit) {
        if (!preview.open(target, parse(kit), () -> list.open(target, KitListMode.VIEW))) {
            throw new CommandException("Não existe um kit chamado " + kit + ".");
        }
    }

    private static ServerPlayer self(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            throw new CommandException("Do console, use /kit view <jogador> <kit>.");
        }
        return player;
    }

    /** Online e visível para quem digitou: quem está em vanish responde igual a quem não existe. */
    private ServerPlayer visiblePlayer(CommandSourceStack source, String name) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null || !players.canSee(source.getPlayer(), player)) {
            throw new CommandException("Jogador não encontrado: " + name);
        }
        return player;
    }

    private CompletableFuture<UUID> uuidOf(CommandSourceStack source, String name) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getUUID());
        }
        return scheduler.onMain(playerRepository.findByName(name))
                .thenApply(found -> UUID.fromString(found.map(PlayerEntity::getId)
                        .orElseThrow(() -> new CommandException("Jogador não encontrado: " + name))));
    }

    private static KitId parse(String kit) {
        return orCommandError(() -> KitId.parse(kit));
    }

    /** As regras do domínio falam em {@link KitException}; o comando responde em {@link CommandException}. */
    private static <T> T orCommandError(java.util.function.Supplier<T> action) {
        try {
            return action.get();
        } catch (KitException e) {
            throw new CommandException(e.getMessage());
        }
    }

    // ---- sugestões ----

    // Os kits que quem digita pode usar (o console, todos).
    Collection<String> kits(SuggestContext context) {
        Object player = context.source().player();
        return claims.kits().stream()
                .filter(kit -> player == null || kit.allows(node -> Permissions.has(player, node)))
                .map(kit -> kit.id().value())
                .toList();
    }

    Collection<String> allKits(SuggestContext context) {
        return claims.kits().stream().map(kit -> kit.id().value()).toList();
    }

    // O primeiro do /kit view: jogadores para quem pode abrir para outros (o kit vem depois);
    // para quem não pode, a única forma é /kit view <kit>, então são os kits.
    Collection<String> playersOrKits(SuggestContext context) {
        if (Permissions.has(context.source(), RavenPermissions.KIT_VIEW_OTHERS)) {
            return onlinePlayers(context);
        }
        return kits(context);
    }

    Collection<String> onlinePlayers(SuggestContext context) {
        List<String> names = new ArrayList<>();
        for (Object player : players.visibleTo(context.source().player())) {
            names.add(((ServerPlayer) player).getGameProfile().getName());
        }
        return names;
    }
}
