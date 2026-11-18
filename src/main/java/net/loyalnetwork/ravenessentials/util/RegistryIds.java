package net.loyalnetwork.ravenessentials.util;

import net.loyalnetwork.coffeelib.api.command.exception.CommandException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;

/** Busca entradas de registro (encantamentos, efeitos...) a partir do id digitado num comando. */
public final class RegistryIds {

    private RegistryIds() {
    }

    /** A entrada do id digitado, ou erro de comando citando {@code label} ("encantamento", "efeito"...). */
    public static <T> Holder<T> find(RegistryAccess access, ResourceKey<? extends Registry<T>> registry,
                                     String input, String label) {
        ResourceLocation id = ResourceLocation.tryParse(input);
        if (id == null) {
            throw new CommandException("ID de " + label + " inválido: " + input);
        }
        return access.lookupOrThrow(registry)
                .get(ResourceKey.create(registry, id))
                .orElseThrow(() -> new CommandException("Não encontrado (" + label + "): " + input));
    }

    /**
     * Nomes vanilla do registro, sem o "minecraft:". Argumentos de palavra não aceitam ':',
     * então é isso que dá para sugerir.
     */
    public static Collection<String> vanillaPaths(ResourceKey<? extends Registry<?>> registry) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return List.of();

        return server.registryAccess().registryOrThrow(registry).keySet().stream()
                .filter(id -> id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                .map(ResourceLocation::getPath)
                .toList();
    }
}
