package net.loyalnetwork.ravenessentials.vanish;

import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    private static boolean blocksPickup(Object entity) {
        return entity instanceof ServerPlayer player
                && Vanish.isVanished(player)
                && !Vanish.has(player, RavenPermissions.VANISH_PICKUP);
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (blocksPickup(event.getPlayer())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (blocksPickup(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Sculk sensor, shrieker e warden não sentem vibrações de quem está em vanish.
    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof ServerPlayer player && Vanish.isVanished(player)) {
            event.setCanceled(true);
        }
    }

    // Não pisoteia terra arada.
    @SubscribeEvent
    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Vanish.isVanished(player)) {
            event.setCanceled(true);
        }
    }

    // Mobs não miram em quem está em vanish.
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && Vanish.isVanished(player)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Vanish.isVanished(player)) {
            player.sendSystemMessage(
                    Component.literal("Você entrou em vanish e está invisível para os outros jogadores.")
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }
}
