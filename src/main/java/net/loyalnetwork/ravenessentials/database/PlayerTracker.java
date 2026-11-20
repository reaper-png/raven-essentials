package net.loyalnetwork.ravenessentials.database;

import com.mojang.logging.LogUtils;
import net.loyalnetwork.coffeelib.neoforge.location.Locations;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Grava no banco o histórico de cada jogador: entradas, saídas, mortes e o ponto de antes de cada
 * teleporte. Lê tudo do jogador aqui, na thread do servidor, e só manda valores prontos para o banco.
 */
@EventBusSubscriber(modid = RavenEssentials.MODID)
public final class PlayerTracker {

    private static final Logger LOGGER = LogUtils.getLogger();

    private PlayerTracker() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String name = player.getGameProfile().getName();
        logFailure(repository().recordLogin(player.getStringUUID(), name, player.getIpAddress(), Instant.now()),
                "a entrada", name);
    }

    // Também roda quando o servidor fecha: o NeoForge desconecta cada jogador antes de parar.
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        logFailure(repository().recordLogout(player.getStringUUID(), Instant.now()),
                "a saída", player.getGameProfile().getName());
    }

    // Morte cancelada (god) não chega aqui: eventos cancelados não são entregues por padrão.
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        logFailure(repository().saveDeathLocation(player.getStringUUID(), Locations.of(player)),
                "o local da morte", player.getGameProfile().getName());
    }

    /** Chame antes de teleportar o jogador: guarda onde ele estava, para o /back. */
    public static void rememberLocation(ServerPlayer player) {
        logFailure(repository().saveLastLocation(player.getStringUUID(), Locations.of(player)),
                "o local antes do teleporte", player.getGameProfile().getName());
    }

    private static PlayerRepository repository() {
        return RavenDatabase.playerRepository();
    }

    // O erro de um CompletableFuture fica guardado nele; sem isto, uma falha no banco passa em silêncio.
    private static void logFailure(CompletableFuture<?> future, String what, String player) {
        future.exceptionally(error -> {
            LOGGER.error("Não foi possível salvar {} de {}", what, player, error);
            return null;
        });
    }
}
