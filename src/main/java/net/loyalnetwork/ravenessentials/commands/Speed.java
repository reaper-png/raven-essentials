package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Locale;

public class Speed {

    private static final float DEFAULT_WALK = 0.1F;
    private static final float DEFAULT_FLY = 0.05F;
    private static final float MAX = 10F;

    public enum Mode { AUTO, WALK, FLY }

    // /speed <0-10> [auto|walk|fly]   1 = velocidade normal. "auto" usa voo se estiver voando.
    @Command(value = "speed", permission = RavenPermissions.SPEED)
    void speed(ServerPlayer player, @Arg("speed") float speed, @Arg("mode") @Default("auto") Mode mode) {
        if (speed < 0 || speed > MAX) {
            throw new CommandException("A velocidade deve estar entre 0 e " + (int) MAX + ".");
        }

        var abilities = player.getAbilities();
        boolean fly = mode == Mode.FLY || (mode == Mode.AUTO && abilities.flying);

        if (fly) {
            abilities.setFlyingSpeed(DEFAULT_FLY * speed);
        } else {
            // walkingSpeed só mexe no FOV e no que é salvo; quem move de verdade é o atributo.
            // O vanilla só copia um para o outro ao carregar o jogador, então definimos os dois.
            abilities.setWalkingSpeed(DEFAULT_WALK * speed);
            var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
            movement.setBaseValue(DEFAULT_WALK * speed);

            // O FOV do cliente é atributo / walkingSpeed. Se as habilidades chegarem antes do
            // atributo, a câmera abre e fecha num piscar; mandando o atributo primeiro, nada pisca.
            player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), List.of(movement)));
        }
        player.onUpdateAbilities();

        player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
                "Velocidade de %s definida para %s.", fly ? "voo" : "caminhada", speed)));
    }
}
