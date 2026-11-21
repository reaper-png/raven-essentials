package net.loyalnetwork.ravenessentials.teleport;

import net.loyalnetwork.coffeelib.api.di.annotation.Provide;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.core.RavenPaths;
import net.loyalnetwork.ravenessentials.teleport.application.TeleportRequests;
import net.loyalnetwork.ravenessentials.teleport.application.TeleportService;
import net.loyalnetwork.ravenessentials.teleport.config.TeleportConfig;
import net.loyalnetwork.ravenessentials.teleport.message.TeleportFeedback;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Monta o sistema de pedidos de teleporte: liga a config, os pedidos em memória e a
 * apresentação, e entrega aos comandos o que eles recebem por {@code @Receive}.
 */
public final class TeleportModule {

    private final TeleportService teleports;
    private final TeleportFeedback feedback;

    private TeleportModule(TeleportService teleports, TeleportFeedback feedback) {
        this.teleports = teleports;
        this.feedback = feedback;
    }

    public static TeleportModule install(ModContainer container, Scheduler scheduler) {
        TeleportConfig config = CoffeeLib.forMod(container, RavenPaths.ROOT).load(TeleportConfig.class);
        TeleportFeedback feedback = new TeleportFeedback(config);
        TeleportRequests requests = new TeleportRequests(scheduler, () -> config.requestSeconds, feedback::expired);
        TeleportService teleports = new TeleportService(requests);

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                requests.forget(event.getEntity().getUUID()));

        return new TeleportModule(teleports, feedback);
    }

    @Provide
    TeleportService teleports() {
        return teleports;
    }

    @Provide
    TeleportFeedback feedback() {
        return feedback;
    }
}
