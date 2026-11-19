package net.loyalnetwork.ravenessentials.god;

import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodConstants;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public final class GodEvents {

    private GodEvents() {
    }

    // Bloqueia todo dano, inclusive o que ignora invulnerabilidade (void, /kill).
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (God.isGod(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Última garantia: se algo chegar a matar, a morte é cancelada e a vida volta cheia.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (God.isGod(event.getEntity()) && event.getEntity() instanceof ServerPlayer player) {
            event.setCanceled(true);
            God.setGod(player, false); // libera o setHealth para preencher
            player.setHealth(player.getMaxHealth());
            God.setGod(player, true);
        }
    }

    // Em god a fome não cai, e quem cai no void volta para o spawn em vez de cair para sempre.
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!God.isGod(event.getEntity()) || !(event.getEntity() instanceof ServerPlayer player)) return;

        var food = player.getFoodData();
        if (food.getFoodLevel() < FoodConstants.MAX_FOOD) food.setFoodLevel(FoodConstants.MAX_FOOD);

        if (player.getY() < player.level().getMinBuildHeight() - 64) {
            Players.teleportToSpawn(player);
            player.sendSystemMessage(Messages.info("Você caiu no void e voltou para o spawn."));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (God.isGod(event.getEntity())) {
            event.getEntity().sendSystemMessage(Messages.info("Você está em modo god."));
        }
    }
}
