package net.loyalnetwork.ravenessentials.commands;

import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;

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
        Holder<MobEffect> effect = findEffect(player, effectId);

        int duration = seconds == -1 ? MobEffectInstance.INFINITE_DURATION : seconds * 20;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        stack.set(DataComponents.POTION_CONTENTS, contents.withEffectAdded(new MobEffectInstance(effect, duration, level - 1)));

        player.sendSystemMessage(Component.literal("Efeito adicionado: " + effectId + " " + level));
    }

    // /potion clear   tira todos os efeitos da poção.
    @Command(value = "clear", permission = RavenPermissions.POTION)
    void clear(ServerPlayer player) {
        ItemStack stack = heldPotion(player);
        stack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        player.sendSystemMessage(Component.literal("Efeitos da poção removidos."));
    }

    private static ItemStack heldPotion(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        // Poções normais, splash, lingering e flechas com efeito.
        if (!stack.has(DataComponents.POTION_CONTENTS)) {
            throw new CommandException("Segure uma poção (ou flecha com efeito) na mão.");
        }
        return stack;
    }

    private static Holder<MobEffect> findEffect(ServerPlayer player, String effectId) {
        ResourceLocation id = ResourceLocation.tryParse(effectId);
        if (id == null) {
            throw new CommandException("ID de efeito inválido.");
        }

        return player.registryAccess()
                .lookupOrThrow(Registries.MOB_EFFECT)
                .get(ResourceKey.create(Registries.MOB_EFFECT, id))
                .orElseThrow(() -> new CommandException("Efeito não encontrado: " + effectId));
    }

    Collection<String> effects() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return List.of();

        // O argumento é uma palavra só (sem ':'), então sugere os efeitos vanilla pelo nome.
        return server.registryAccess()
                .registryOrThrow(Registries.MOB_EFFECT)
                .keySet()
                .stream()
                .filter(id -> id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                .map(ResourceLocation::getPath)
                .toList();
    }
}
