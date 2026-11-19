package net.loyalnetwork.ravenessentials.command.item;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.loyalnetwork.ravenessentials.util.RegistryIds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.Collection;

@Command("potion")
public class Potion {

    private static final int MAX_LEVEL = 255;
    private static final int MAX_SECONDS = 1_000_000;

    // /potion add <effect> [level] [seconds]   seconds = -1 deixa o efeito infinito.
    @Command(value = "add", permission = RavenPermissions.POTION)
    void add(
            ServerPlayer player,
            @Arg("effect") @Suggest(provider = "effects") String effectId,
            @Arg("level") @Default("1") int level,
            @Arg("seconds") @Default("180") int seconds
    ) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new CommandException("O nível deve estar entre 1 e " + MAX_LEVEL + ".");
        }
        if (seconds != -1 && (seconds < 1 || seconds > MAX_SECONDS)) {
            throw new CommandException("A duração deve ser entre 1 e " + MAX_SECONDS + " segundos, ou -1 para infinita.");
        }

        ItemStack stack = heldPotion(player);
        var effect = RegistryIds.find(player.registryAccess(), Registries.MOB_EFFECT, effectId, "efeito");

        int duration = seconds == -1 ? MobEffectInstance.INFINITE_DURATION : seconds * 20;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        stack.set(DataComponents.POTION_CONTENTS, contents.withEffectAdded(new MobEffectInstance(effect, duration, level - 1)));

        Messages.send(player, "Efeito adicionado: " + effectId + " " + level);
    }

    // /potion clear   tira todos os efeitos da poção.
    @Command(value = "clear", permission = RavenPermissions.POTION)
    void clear(ServerPlayer player) {
        heldPotion(player).set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Messages.send(player, "Efeitos da poção removidos.");
    }

    private static ItemStack heldPotion(ServerPlayer player) {
        ItemStack stack = Players.requireHeldItem(player);
        // Poções normais, splash, lingering e flechas com efeito.
        if (!stack.has(DataComponents.POTION_CONTENTS)) {
            throw new CommandException("Segure uma poção (ou flecha com efeito) na mão.");
        }
        return stack;
    }

    Collection<String> effects() {
        return RegistryIds.vanillaPaths(Registries.MOB_EFFECT);
    }
}
