package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.food.FoodConstants;

import java.util.Collection;

public class Heal {

    // /heal [player]   vida cheia, fome cheia, apaga o fogo e tira efeitos negativos.
    @Command(value = "heal", permission = RavenPermissions.HEAL)
    void heal(CommandSourceStack source, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        ServerPlayer target = Targets.resolve(source, name, RavenPermissions.HEAL_OTHERS);

        target.setHealth(target.getMaxHealth());
        target.getFoodData().setFoodLevel(FoodConstants.MAX_FOOD);
        target.getFoodData().setSaturation(FoodConstants.MAX_SATURATION);
        target.clearFire();
        target.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .map(effect -> effect.getEffect())
                .toList()
                .forEach(target::removeEffect);

        target.sendSystemMessage(Component.literal("Você foi curado."));
        if (target != source.getPlayer()) {
            source.sendSystemMessage(Component.literal(target.getGameProfile().getName() + " foi curado."));
        }
    }

    Collection<String> online() {
        return Targets.onlineNames();
    }
}
