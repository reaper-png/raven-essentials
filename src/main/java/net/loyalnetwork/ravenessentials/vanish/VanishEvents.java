package net.loyalnetwork.ravenessentials.vanish;

import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public final class VanishEvents {

    private VanishEvents() {
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (Vanish.blocksPickup(event.getPlayer())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (Vanish.blocksPickup(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Sculk sensor, shrieker e warden não sentem vibrações de quem está em vanish.
    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        if (Vanish.isVanished(event.getCause())) {
            event.setCanceled(true);
        }
    }

    // Não pisoteia terra arada.
    @SubscribeEvent
    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (Vanish.isVanished(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Mobs não miram em quem está em vanish.
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (Vanish.isVanished(event.getNewAboutToBeSetTarget())) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (Vanish.isVanished(event.getEntity())) {
            event.getEntity().sendSystemMessage(
                    Messages.info("Você entrou em vanish e está invisível para os outros jogadores."));
        }
    }
}
