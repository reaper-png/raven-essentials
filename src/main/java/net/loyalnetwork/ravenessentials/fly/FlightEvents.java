package net.loyalnetwork.ravenessentials.fly;

import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Mantém o {@link Flight} aplicado entre logins, mortes, dimensões e mudanças de permissão. */
@EventBusSubscriber(modid = RavenEssentials.MODID)
public final class FlightEvents {

    // Permissões mudam a qualquer hora (LuckPerms, deop) e outros mods podem ligar voo: confere a cada segundo.
    private static final int CHECK_INTERVAL = 20;

    private FlightEvents() {
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

    // Sair do criativo/espectador volta a valer a regra dos outros voos.
    @SubscribeEvent
    public static void onChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.server.execute(() -> Flight.sync(player)); // depois do modo de jogo ser aplicado
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % CHECK_INTERVAL == 0) {
            Flight.sync(player);
        }
    }
}
