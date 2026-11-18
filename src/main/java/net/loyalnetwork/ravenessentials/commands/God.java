package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenAttachments;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.util.Targets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodConstants;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public class God {

    // /god [player]
    @Command(value = "god", permission = RavenPermissions.GOD)
    void god(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, RavenPermissions.GOD_OTHERS);

        boolean enabled = !isGod(target);
        target.setData(RavenAttachments.GOD, enabled);
        Messages.toggled(source, target, "Modo god", enabled);
    }

    Collection<String> online(CommandSource source) {
        return Targets.onlineNames(source);
    }

    /** Se é um jogador em modo god. Aceita qualquer entidade (ou null) para os eventos e mixins. */
    public static boolean isGod(@Nullable Entity entity) {
        return entity instanceof ServerPlayer player && player.getData(RavenAttachments.GOD);
    }

    // Bloqueia todo dano, inclusive o que ignora invulnerabilidade (void, /kill).
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (isGod(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Última garantia: se algo chegar a matar, a morte é cancelada e a vida volta cheia.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (isGod(event.getEntity()) && event.getEntity() instanceof ServerPlayer player) {
            event.setCanceled(true);
            player.setData(RavenAttachments.GOD, false); // libera o setHealth para preencher
            player.setHealth(player.getMaxHealth());
            player.setData(RavenAttachments.GOD, true);
        }
    }

    // Em god a fome não cai, e quem cai no void volta para o spawn em vez de cair para sempre.
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!isGod(event.getEntity()) || !(event.getEntity() instanceof ServerPlayer player)) return;

        var food = player.getFoodData();
        if (food.getFoodLevel() < FoodConstants.MAX_FOOD) food.setFoodLevel(FoodConstants.MAX_FOOD);

        if (player.getY() < player.level().getMinBuildHeight() - 64) {
            Players.teleportToSpawn(player);
            player.sendSystemMessage(Messages.info("Você caiu no void e voltou para o spawn."));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (isGod(event.getEntity())) {
            event.getEntity().sendSystemMessage(Messages.info("Você está em modo god."));
        }
    }
}
