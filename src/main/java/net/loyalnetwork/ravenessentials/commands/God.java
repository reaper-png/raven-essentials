package net.loyalnetwork.ravenessentials.commands;

import com.mojang.serialization.Codec;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collection;
import java.util.function.Supplier;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public class God {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RavenEssentials.MODID);

    // Salvo no jogador: continua ligado depois de relog e restart.
    public static final Supplier<AttachmentType<Boolean>> GOD = ATTACHMENTS.register(
            "god",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build()
    );

    // /god [player]
    @Command(value = "god", permission = RavenPermissions.GOD)
    void god(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, RavenPermissions.GOD_OTHERS);

        boolean enabled = !target.getData(GOD);
        target.setData(GOD, enabled);

        Component status = enabled
                ? Component.literal("Modo god ativado").withStyle(ChatFormatting.GREEN)
                : Component.literal("Modo god desativado").withStyle(ChatFormatting.RED);

        target.sendSystemMessage(status);
        if (target != source.getPlayer()) {
            source.sendSystemMessage(status.copy().append(Component.literal(" para " + target.getGameProfile().getName())));
        }
    }

    Collection<String> online() {
        return Targets.onlineNames();
    }

    public static boolean isGod(ServerPlayer player) {
        return player.getData(GOD);
    }

    // Bloqueia todo dano, inclusive o que ignora invulnerabilidade (void, /kill).
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isGod(player)) {
            event.setCanceled(true);
        }
    }

    // Última garantia: se algo chegar a matar, a morte é cancelada e a vida volta cheia.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isGod(player)) {
            event.setCanceled(true);
            player.setData(GOD, false); // libera o setHealth para preencher
            player.setHealth(player.getMaxHealth());
            player.setData(GOD, true);
        }
    }

    // Em god a fome não cai, e quem cai no void volta para o spawn em vez de cair para sempre.
    @SubscribeEvent
    public static void onTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isGod(player)) return;

        var food = player.getFoodData();
        if (food.getFoodLevel() < 20) food.setFoodLevel(20);

        if (player.getY() < player.level().getMinBuildHeight() - 64) {
            var overworld = player.server.overworld();
            var spawn = overworld.getSharedSpawnPos();
            player.fallDistance = 0;
            player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                    overworld.getSharedSpawnAngle(), 0F);
            player.sendSystemMessage(Component.literal("Você caiu no void e voltou para o spawn.").withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getData(GOD)) {
            player.sendSystemMessage(Component.literal("Você está em modo god.").withStyle(ChatFormatting.GRAY));
        }
    }
}
