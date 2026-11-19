package net.loyalnetwork.ravenessentials.database;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@EventBusSubscriber
public class PlayerLoginListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
       if (!(event.getEntity() instanceof ServerPlayer player)) return;

       String ip = player.getIpAddress();
       String name = player.getName().getString();
       String uuid = player.getStringUUID();

       RavenDatabase.playerRepository().recordLogin(uuid, name, ip)
               .exceptionally(error -> {
                   LOGGER.error("Não foi possível salvar o jogador {} ({})", name, uuid, error);
                   return null;
               });
    }
}
