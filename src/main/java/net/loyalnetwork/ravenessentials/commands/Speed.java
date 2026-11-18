package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public class Speed {

    private static final float MAX = 10F;

    public enum Mode { AUTO, WALK, FLY }

    // /speed <0-10> [auto|walk|fly]   1 = velocidade normal. "auto" usa voo se estiver voando.
    @Command(value = "speed", permission = RavenPermissions.SPEED)
    void speed(ServerPlayer player, @Arg("speed") float speed, @Arg("mode") @Default("auto") Mode mode) {
        if (speed < 0 || speed > MAX) {
            throw new CommandException("A velocidade deve estar entre 0 e " + (int) MAX + ".");
        }

        boolean fly = mode == Mode.FLY || (mode == Mode.AUTO && player.getAbilities().flying);
        if (fly) {
            Players.setFlySpeed(player, speed);
        } else {
            Players.setWalkSpeed(player, speed);
        }

        Messages.send(player, String.format(Locale.ROOT,
                "Velocidade de %s definida para %s.", fly ? "voo" : "caminhada", speed));
    }
}
