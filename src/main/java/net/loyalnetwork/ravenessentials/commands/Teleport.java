package net.loyalnetwork.ravenessentials.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Greedy;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.CommandNodes;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber(modid = RavenEssentials.MODID)
public class Teleport {

    // /tp <x> <y> <z> [dimensão]   ex.: /tp 0 100 0 minecraft:nether
    @Command(value = "tp", permission = RavenPermissions.TP)
    // Um argumento só, lido por nós: os argumentos de palavra do Brigadier não aceitam "~" nem ":".
    void tp(ServerPlayer player, @Arg("coords") @Greedy String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length < 3 || parts.length > 4) {
            throw new CommandException("Use: /tp <x> <y> <z> [dimensão]");
        }

        ServerLevel level = parts.length == 4 ? findLevel(player, parts[3]) : player.serverLevel();

        double targetX = coordinate(parts[0], player.getX(), true);
        double targetY = coordinate(parts[1], player.getY(), false);
        double targetZ = coordinate(parts[2], player.getZ(), true);

        Players.teleport(player, level, targetX, targetY, targetZ);

        Messages.send(player, String.format(Locale.ROOT,
                "Teleportado para %.1f %.1f %.1f em %s.",
                targetX, targetY, targetZ, level.dimension().location()));
    }

    private static ServerLevel findLevel(ServerPlayer player, String input) {
        ResourceLocation id = ResourceLocation.tryParse(alias(input.toLowerCase(Locale.ROOT)));
        if (id == null) {
            throw new CommandException("Dimensão inválida: " + input);
        }

        ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (level == null) {
            throw new CommandException("Dimensão não encontrada: " + input);
        }
        return level;
    }

    /** Atalhos: "nether", "minecraft:nether", "end" e "minecraft:end" viram os ids reais. */
    private static String alias(String input) {
        return switch (input) {
            case "nether", "minecraft:nether" -> Level.NETHER.location().toString();
            case "end", "minecraft:end" -> Level.END.location().toString();
            case "overworld" -> Level.OVERWORLD.location().toString();
            default -> input;
        };
    }

    /** Número absoluto, "~" (posição atual) ou "~5" (relativo). X e Z inteiros vão para o centro do bloco. */
    private static double coordinate(String input, double current, boolean center) {
        try {
            if (input.startsWith("~")) {
                String offset = input.substring(1);
                return current + (offset.isEmpty() ? 0 : Double.parseDouble(offset));
            }

            double value = Double.parseDouble(input);
            return center && !input.contains(".") ? value + 0.5 : value;
        } catch (NumberFormatException e) {
            throw new CommandException("Coordenada inválida: " + input);
        }
    }

    /** Tira o /tp vanilla antes da CoffeeLib registrar o nosso. O /teleport vanilla continua. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void removeVanillaTp(RegisterCommandsEvent event) {
        CommandNodes.remove(event.getDispatcher().getRoot(), "tp");
    }

    /**
     * Depois da CoffeeLib registrar o /tp, troca as sugestões do argumento "coords" por umas que
     * leem o que já foi digitado: as coordenadas atuais do jogador uma a uma, e as dimensões na quarta posição.
     * O executor e a permissão continuam os da CoffeeLib.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void attachSuggestions(RegisterCommandsEvent event) {
        CommandNode<CommandSourceStack> tp = event.getDispatcher().getRoot().getChild("tp");
        if (tp == null || !(tp.getChild("coords") instanceof ArgumentCommandNode<CommandSourceStack, ?> coords)) {
            return;
        }

        var replacement = RequiredArgumentBuilder.<CommandSourceStack, String>argument("coords", StringArgumentType.greedyString())
                .requires(coords.getRequirement())
                .executes(coords.getCommand())
                .suggests(Teleport::suggest)
                .build();

        CommandNodes.remove(tp, "coords");
        tp.addChild(replacement);
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String typed = builder.getRemaining();
        int tokenStart = typed.lastIndexOf(' ') + 1;
        int index = typed.isBlank() ? 0 : typed.substring(0, tokenStart).trim().split("\\s+").length;
        if (tokenStart == 0) index = 0;

        String current = typed.substring(tokenStart).toLowerCase(Locale.ROOT);
        SuggestionsBuilder token = builder.createOffset(builder.getStart() + tokenStart);
        ServerPlayer player = context.getSource().getPlayer();

        List<String> candidates = new ArrayList<>();
        if (index < 3) {
            candidates.add("~");
            if (player != null) {
                BlockPos pos = player.blockPosition();
                int[] coords = {pos.getX(), pos.getY(), pos.getZ()};
                candidates.add(String.valueOf(coords[index]));

                // No começo, oferece as três de uma vez também.
                if (index == 0) {
                    candidates.add(coords[0] + " " + coords[1] + " " + coords[2]);
                    candidates.add("~ ~ ~");
                }
            }
        } else if (index == 3) {
            candidates.addAll(List.of("nether", "end", "overworld"));
            for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
                candidates.add(level.dimension().location().toString());
            }
        }

        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(current)) {
                token.suggest(candidate);
            }
        }
        return token.buildFuture();
    }
}
