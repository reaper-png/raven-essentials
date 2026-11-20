package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.coffeelib.api.text.TextFormatter;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.kit.application.ClaimResult;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitMessages;
import net.loyalnetwork.ravenessentials.kit.menu.config.KitsConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Diz ao jogador, com as mensagens da config, como terminou o pedido de kit. */
public final class ClaimFeedback {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");
    private static final TextFormatter<MutableComponent> TEXT = CoffeeLib.text(RavenEssentials.MODID);

    private final KitsConfig config;

    public ClaimFeedback(KitsConfig config) {
        this.config = config;
    }

    public void send(ServerPlayer player, ClaimResult result) {
        KitMessages messages = config.messages;
        switch (result) {
            case ClaimResult.Claimed claimed -> tell(player, messages.claimed, KitTexts.of(claimed.kit()));
            case ClaimResult.Refused refused -> tell(player, refusal(messages, refused.status()),
                    KitTexts.of(refused.kit(), refused.status()));
            case ClaimResult.NotFound missing -> tell(player, messages.notFound,
                    Placeholders.NONE.with("kit", missing.id().value()));
            case ClaimResult.InventoryFull full -> tell(player, messages.inventoryFull, KitTexts.of(full.kit()));
            case ClaimResult.Busy busy -> tell(player, messages.busy, Placeholders.NONE);
        }
    }

    /** Para quem recebeu um kit pelo {@code /kit give}. */
    public void received(ServerPlayer player, Kit kit) {
        tell(player, config.messages.received, KitTexts.of(kit));
    }

    private static String refusal(KitMessages messages, KitStatus status) {
        return switch (status) {
            case KitStatus.OnCooldown cooldown -> messages.cooldown;
            case KitStatus.Claimed claimed -> messages.alreadyClaimed;
            case KitStatus.Locked locked -> messages.locked;
            case KitStatus.Available available -> messages.claimed;
        };
    }

    /** O pedido falhou (banco fora, servidor parando): avisa e registra. */
    public Void failed(ServerPlayer player, Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        if (cause instanceof CancellationException) {
            return null;
        }
        LOGGER.log(Level.SEVERE, "Falha ao entregar kit para " + player.getGameProfile().getName(), cause);
        if (!player.hasDisconnected()) {
            tell(player, config.messages.failed, Placeholders.NONE);
        }
        return null;
    }

    private static void tell(ServerPlayer player, String message, Placeholders values) {
        player.sendSystemMessage(TEXT.parse(values.apply(message), player, values.context()));
    }
}
