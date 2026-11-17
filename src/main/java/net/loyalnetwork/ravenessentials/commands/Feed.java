package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodConstants;

public class Feed {

    @Command(value = "feed", permission = RavenPermissions.FEED)
    void feed(ServerPlayer player) {
        var foodData = player.getFoodData();
        foodData.setFoodLevel(FoodConstants.MAX_FOOD);
        foodData.setSaturation(FoodConstants.MAX_SATURATION);
    }
}
