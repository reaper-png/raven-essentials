package net.loyalnetwork.ravenessentials.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.loyalnetwork.coffeelib.api.command.CommandSource;
import net.loyalnetwork.coffeelib.api.command.SuggestContext;
import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.loyalnetwork.coffeelib.neoforge.commands.BrigadierArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * O {@code ServerLevel} dos comandos do mod: o da CoffeeLib, mais {@code ~} para "a dimensão
 * de quem rodou", que é o que deixa {@code @Default("~") ServerLevel} escrever "opcional,
 * senão a atual". Aceita ids ({@code minecraft:the_nether}) e apelidos ({@code nether}).
 */
public final class CurrentDimensionArgument implements BrigadierArgument<ServerLevel> {

    /** O padrão que significa "a dimensão de quem rodou". */
    public static final String CURRENT = "~";

    private static final Map<String, ResourceKey<Level>> ALIASES = Map.of(
            "overworld", Level.OVERWORLD,
            "nether", Level.NETHER,
            "the_nether", Level.NETHER,
            "end", Level.END,
            "the_end", Level.END);

    @Override
    public ArgumentType<?> brigadier() {
        return ResourceLocationArgument.id();
    }

    @Override
    public ServerLevel read(CommandContext<CommandSourceStack> context, String name) {
        return find(context.getSource().getServer(), ResourceLocationArgument.getId(context, name).toString());
    }

    @Override
    public ServerLevel parse(CommandSource source, String input) {
        if (!(source.unwrap() instanceof CommandSourceStack stack)) {
            throw new CommandException("Informe a dimensão.");
        }
        return input.equals(CURRENT) ? stack.getLevel() : find(stack.getServer(), input);
    }

    private static ServerLevel find(MinecraftServer server, String input) {
        String typed = input.toLowerCase(Locale.ROOT);
        ResourceKey<Level> key = ALIASES.get(typed.startsWith("minecraft:") ? typed.substring(10) : typed);
        if (key == null) {
            ResourceLocation id = ResourceLocation.tryParse(typed);
            if (id == null) {
                throw new CommandException("Dimensão inválida: " + input);
            }
            key = ResourceKey.create(Registries.DIMENSION, id);
        }
        ServerLevel level = server.getLevel(key);
        if (level == null) {
            throw new CommandException("Dimensão não encontrada: " + input);
        }
        return level;
    }

    @Override
    public Collection<String> suggest(SuggestContext context) {
        List<String> names = new ArrayList<>(List.of("overworld", "nether", "end"));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
                names.add(level.dimension().location().toString());
            }
        }
        return names;
    }
}
