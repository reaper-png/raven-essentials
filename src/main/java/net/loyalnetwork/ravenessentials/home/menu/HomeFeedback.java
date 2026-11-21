package net.loyalnetwork.ravenessentials.home.menu;

import net.loyalnetwork.coffeelib.api.text.TextFormatter;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.home.application.HomeService;
import net.loyalnetwork.ravenessentials.home.application.HomeTrip;
import net.loyalnetwork.ravenessentials.home.domain.HomeException;
import net.loyalnetwork.ravenessentials.home.domain.HomeName;
import net.loyalnetwork.ravenessentials.home.domain.PlayerHomes;
import net.loyalnetwork.ravenessentials.home.domain.SetHome;
import net.loyalnetwork.ravenessentials.home.menu.config.HomeMessages;
import net.loyalnetwork.ravenessentials.home.menu.config.HomesConfig;
import net.loyalnetwork.ravenessentials.menu.Placeholders;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Diz ao jogador, com as mensagens da config, como terminou o que ele pediu. */
public final class HomeFeedback {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.homes");
    private static final TextFormatter<MutableComponent> TEXT = CoffeeLib.text(RavenEssentials.MODID);

    private final HomesConfig config;
    private final HomeService homes;

    public HomeFeedback(HomesConfig config, HomeService homes) {
        this.config = config;
        this.homes = homes;
    }

    /** {@code owner}: o dono, quando a home não é de quem pediu. */
    public void trip(ServerPlayer player, HomeTrip trip, Optional<String> owner) {
        HomeMessages messages = config.messages;
        Placeholders mine = mine(player).with("owner", owner.orElse(""));
        switch (trip) {
            case HomeTrip.Arrived arrived -> tell(player, owner.isPresent() ? messages.teleportedOther : messages.teleported,
                    merge(mine, HomeTexts.of(arrived.home())));
            case HomeTrip.NoSuchHome missing -> tell(player, notFound(messages, owner, homes.knownHomes(player)),
                    mine.with("home", missing.name().value()));
            case HomeTrip.NoSuchPlayer unknown -> tell(player, messages.playerNotFound, mine.with("owner", unknown.name()));
            case HomeTrip.Private hidden -> tell(player, messages.privateHome, merge(mine, HomeTexts.of(hidden.home())));
            case HomeTrip.WorldMissing gone -> tell(player, messages.worldMissing, merge(mine, HomeTexts.of(gone.home())));
        }
    }

    private static String notFound(HomeMessages messages, Optional<String> owner, PlayerHomes own) {
        if (owner.isPresent()) {
            return messages.otherNotFound;
        }
        return own.count() == 0 ? messages.noHomes : messages.notFound;
    }

    public void set(ServerPlayer player, SetHome result) {
        HomeMessages messages = config.messages;
        switch (result) {
            case SetHome.Created created -> tell(player, messages.created, merge(mine(player), HomeTexts.of(created.home())));
            case SetHome.Moved moved -> tell(player, messages.moved, merge(mine(player), HomeTexts.of(moved.home())));
            case SetHome.LimitReached reached -> tell(player, messages.limitReached, mine(player));
        }
    }

    public void deleted(ServerPlayer player, HomeName name, boolean found) {
        HomeMessages messages = config.messages;
        tell(player, found ? messages.deleted : messages.notFound, mine(player).with("home", name.value()));
    }

    /**
     * O pedido falhou: uma regra das homes (o nome já existe, a home sumiu) vira a mensagem
     * dela; o resto (banco fora) é registrado e vira a mensagem genérica.
     */
    public Void failed(ServerPlayer player, Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        if (cause instanceof CancellationException || player.hasDisconnected()) {
            return null;
        }
        if (cause instanceof HomeException rule) {
            player.sendSystemMessage(Messages.error(rule.getMessage()));
            return null;
        }
        LOGGER.log(Level.SEVERE, "Falha nas homes de " + player.getGameProfile().getName(), cause);
        tell(player, config.messages.failed, Placeholders.NONE);
        return null;
    }

    private Placeholders mine(ServerPlayer player) {
        PlayerHomes own = homes.knownHomes(player);
        return HomeTexts.of(own, homes.limit(player));
    }

    // Os valores da home por cima dos do jogador, com a home no contexto.
    private static Placeholders merge(Placeholders player, Placeholders home) {
        return home.mergedOver(player);
    }

    private static void tell(ServerPlayer player, String message, Placeholders values) {
        player.sendSystemMessage(TEXT.parse(values.apply(message), player, values.context()));
    }
}
