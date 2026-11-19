package net.loyalnetwork.ravenessentials.core;

import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.command.Commands;
import net.loyalnetwork.ravenessentials.database.RavenDatabase;
import net.loyalnetwork.ravenessentials.util.Permissions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

/**
 * Ponto de entrada dos iniciadores. Sistema novo que precisa ser iniciado: chame aqui,
 * sem mexer na classe {@code @Mod}. A ordem importa e está comentada em cada passo.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void start(IEventBus modEventBus, ModContainer container) {
        // 1. Permissões antes dos comandos, para os nós manterem os padrões declarados aqui.
        var permissions = CoffeeLib.permissions(container);
        permissions.declare(RavenPermissions.class);
        Permissions.init(permissions);

        // 2. Registros do NeoForge (precisam estar no barramento do mod antes do registro rodar).
        RavenAttachments.REGISTER.register(modEventBus);

        // 3. Banco e Hibernate.
        RavenDatabase.open(container);

        // 4. Comandos (compilados para o Brigadier no RegisterCommandsEvent).
        Commands.registerAll(container);
    }
}
