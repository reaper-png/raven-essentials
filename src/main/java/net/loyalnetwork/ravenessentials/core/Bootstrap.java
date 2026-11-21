package net.loyalnetwork.ravenessentials.core;

import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.command.Commands;
import net.loyalnetwork.ravenessentials.database.RavenDatabase;
import net.loyalnetwork.ravenessentials.fly.FlightEvents;
import net.loyalnetwork.ravenessentials.home.HomeModule;
import net.loyalnetwork.ravenessentials.kit.KitModule;
import net.loyalnetwork.ravenessentials.menu.ChatPrompts;
import net.loyalnetwork.ravenessentials.teleport.TeleportModule;
import net.loyalnetwork.ravenessentials.util.Permissions;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Ponto de entrada dos iniciadores. Sistema novo que precisa ser iniciado: chame aqui,
 * sem mexer na classe {@code @Mod}. A ordem importa e está comentada em cada passo.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void start(IEventBus modEventBus, ModContainer container) {
        // 1. Permissões que nenhum comando declara (os nós de comando a CoffeeLib declara no registro).
        var permissions = CoffeeLib.permissions(container);
        permissions.declare(RavenPermissions.class);
        Permissions.init(permissions);

        // 2. Registros do NeoForge (precisam estar no barramento do mod antes do registro rodar).
        RavenAttachments.REGISTER.register(modEventBus);

        // 3. Banco e Hibernate.
        RavenDatabase.open(container);

        // 4. Serviços para os @Receive dos comandos: os da CoffeeLib (permissões, scheduler,
        //    banco, jogadores) já vêm; os do mod entram aqui, antes dos comandos.
        var services = CoffeeLib.services(container);
        services.register(new RavenServices());

        // 5. Sistemas que se ligam à CoffeeLib.
        Vanish.install();
        var scheduler = CoffeeLib.scheduler(container);
        FlightEvents.install(permissions, scheduler);

        // 6. Kits, homes e pedidos de teleporte (os dois primeiros precisam do banco aberto). Os módulos
        //    também são provedores dos @Receive dos comandos.
        // Perguntas pelo chat dos menus (nome de kit, de home...): uma só, para ninguém ter duas abertas.
        var prompts = new ChatPrompts(scheduler);
        NeoForge.EVENT_BUS.register(prompts);
        services.register(KitModule.install(container, permissions, scheduler, prompts));
        services.register(HomeModule.install(container, permissions, scheduler, prompts));
        services.register(TeleportModule.install(container, scheduler));

        // 7. Comandos (compilados para o Brigadier no RegisterCommandsEvent).
        Commands.registerAll(container);
    }
}
