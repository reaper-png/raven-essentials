package net.loyalnetwork.ravenessentials.fly;

import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Mantém o {@link Flight} aplicado entre logins, mortes, dimensões e mudanças de permissão. */
@EventBusSubscriber(modid = RavenEssentials.MODID)
public final class FlightEvents {

    // Outros mods ligam voo sem avisar ninguém: conferido a cada segundo, para o ravenessentials.fly.allow valer.
    private static final int OTHER_FLIGHT_CHECK = 20;

    private static Scheduler scheduler;

    private FlightEvents() {
    }

    /**
     * Liga o fly à CoffeeLib: uma permissão perdida (LuckPerms, deop) desliga o /fly na hora,
     * e a conferência periódica pega o voo que outros mods ligam.
     */
    public static void install(PermissionManager permissions, Scheduler serverScheduler) {
        scheduler = serverScheduler;
        permissions.onChange(uuid -> {
            ServerPlayer player = online(uuid);
            if (player != null) {
                Flight.sync(player);
            }
        });
    }

    // A conferência periódica é agendada a cada servidor: o Scheduler cancela as tarefas quando
    // ele para, e num singleplayer o mesmo jogo abre vários mundos.
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        scheduler.every(OTHER_FLIGHT_CHECK, OTHER_FLIGHT_CHECK,
                () -> server.getPlayerList().getPlayers().forEach(Flight::sync));
    }

    private static ServerPlayer online(java.util.UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(uuid);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) Flight.sync(player);
    }

    // Morrer cria um jogador novo, sem os modificadores: aplica de novo.
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) Flight.sync(player);
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) Flight.sync(player);
    }

    // Sair do criativo/espectador volta a valer a regra dos outros voos. O evento vem antes do modo
    // mudar, então confere no próximo tick.
    @SubscribeEvent
    public static void onChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && scheduler != null) {
            scheduler.later(0, () -> {
                if (!player.hasDisconnected()) {
                    Flight.sync(player);
                }
            });
        }
    }
}
