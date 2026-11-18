package net.loyalnetwork.ravenessentials.util;

import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Operações básicas de jogador usadas pelos comandos e eventos. */
public final class Players {

    public static final float DEFAULT_WALK_SPEED = 0.1F;
    public static final float DEFAULT_FLY_SPEED = 0.05F;

    private Players() {
    }

    public static String name(ServerPlayer player) {
        return player.getGameProfile().getName();
    }

    // ---- itens ----

    /** Item da mão principal, ou erro de comando se a mão estiver vazia. */
    public static ItemStack requireHeldItem(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw new CommandException("Você precisa estar segurando um item.");
        }
        return stack;
    }

    /** Coloca no inventário; o que não couber cai no chão. */
    public static void give(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty()) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    // ---- vida e fome ----

    public static void feed(ServerPlayer player) {
        var food = player.getFoodData();
        food.setFoodLevel(FoodConstants.MAX_FOOD);
        food.setSaturation(FoodConstants.MAX_SATURATION);
    }

    /** Vida cheia, fome cheia, sem fogo e sem efeitos negativos. */
    public static void heal(ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        feed(player);
        player.clearFire();
        removeHarmfulEffects(player);
    }

    public static void removeHarmfulEffects(ServerPlayer player) {
        player.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList()
                .forEach(player::removeEffect);
    }

    // ---- movimento ----

    /** Teleporta mantendo para onde o jogador está olhando. */
    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
    }

    /** Spawn do mundo (overworld), o mesmo de /setworldspawn. */
    public static void teleportToSpawn(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        var pos = overworld.getSharedSpawnPos();
        player.fallDistance = 0;
        player.teleportTo(overworld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                overworld.getSharedSpawnAngle(), 0F);
    }

    /** {@code multiplier} 1 = velocidade normal de voo. */
    public static void setFlySpeed(ServerPlayer player, float multiplier) {
        player.getAbilities().setFlyingSpeed(DEFAULT_FLY_SPEED * multiplier);
        player.onUpdateAbilities();
    }

    /**
     * {@code multiplier} 1 = velocidade normal de caminhada.
     * <p>
     * walkingSpeed só mexe no FOV e no que é salvo; quem move de verdade é o atributo, e o vanilla
     * só copia um para o outro ao carregar o jogador, então os dois são definidos. O FOV do cliente
     * é atributo / walkingSpeed: mandando o atributo antes das habilidades, a câmera não pisca.
     */
    public static void setWalkSpeed(ServerPlayer player, float multiplier) {
        float speed = DEFAULT_WALK_SPEED * multiplier;
        player.getAbilities().setWalkingSpeed(speed);

        var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) {
            movement.setBaseValue(speed);
            player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), List.of(movement)));
        }
        player.onUpdateAbilities();
    }
}
