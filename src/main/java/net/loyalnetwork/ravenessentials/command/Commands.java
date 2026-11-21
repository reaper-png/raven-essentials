package net.loyalnetwork.ravenessentials.command;

import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.command.chat.ClearChat;
import net.loyalnetwork.ravenessentials.command.chat.Colors;
import net.loyalnetwork.ravenessentials.command.chat.Title;
import net.loyalnetwork.ravenessentials.command.info.Online;
import net.loyalnetwork.ravenessentials.command.info.Ping;
import net.loyalnetwork.ravenessentials.command.inventory.Anvil;
import net.loyalnetwork.ravenessentials.command.inventory.Clear;
import net.loyalnetwork.ravenessentials.command.inventory.Compact;
import net.loyalnetwork.ravenessentials.command.inventory.Craft;
import net.loyalnetwork.ravenessentials.command.inventory.EnderChest;
import net.loyalnetwork.ravenessentials.command.inventory.Smelt;
import net.loyalnetwork.ravenessentials.command.inventory.Trash;
import net.loyalnetwork.ravenessentials.command.item.Enchant;
import net.loyalnetwork.ravenessentials.command.item.Hat;
import net.loyalnetwork.ravenessentials.command.item.Potion;
import net.loyalnetwork.ravenessentials.command.item.Repair;
import net.loyalnetwork.ravenessentials.command.item.Skull;
import net.loyalnetwork.ravenessentials.command.player.Feed;
import net.loyalnetwork.ravenessentials.command.player.GameModeCommand;
import net.loyalnetwork.ravenessentials.command.player.Heal;
import net.loyalnetwork.ravenessentials.command.player.NightVision;
import net.loyalnetwork.ravenessentials.command.player.Speed;
import net.loyalnetwork.ravenessentials.command.world.Back;
import net.loyalnetwork.ravenessentials.command.world.Slime;
import net.loyalnetwork.ravenessentials.command.world.Spawn;
import net.loyalnetwork.ravenessentials.command.world.Teleport;
import net.loyalnetwork.ravenessentials.fly.FlyCommand;
import net.loyalnetwork.ravenessentials.god.GodCommand;
import net.loyalnetwork.ravenessentials.home.command.HomeCommand;
import net.loyalnetwork.ravenessentials.kit.command.KitCommand;
import net.loyalnetwork.ravenessentials.teleport.command.TeleportDirectCommand;
import net.loyalnetwork.ravenessentials.teleport.command.TeleportRequestCommand;
import net.loyalnetwork.ravenessentials.vanish.VanishCommand;
import net.loyalnetwork.ravenessentials.util.CurrentDimensionArgument;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModContainer;

/** Todos os comandos do mod. Comando novo: adicione aqui. */
public final class Commands {

    private Commands() {
    }

    public static void registerAll(ModContainer container) {
        var commands = CoffeeLib.commands(container);
        // Tipo de argumento do mod: dimensão com "~" para a atual (o /tp usa).
        commands.argument(ServerLevel.class, new CurrentDimensionArgument());

        // chat
        commands.register(new ClearChat());
        commands.register(new Colors());
        commands.register(new Title());

        // info
        commands.register(new Online());
        commands.register(new Ping());

        // inventory
        commands.register(new Anvil());
        commands.register(new Clear());
        commands.register(new Compact());
        commands.register(new Craft());
        commands.register(new EnderChest());
        commands.register(new Smelt());
        commands.register(new Trash());

        // item
        commands.register(new Enchant());
        commands.register(new Hat());
        commands.register(new Potion());
        commands.register(new Repair());
        commands.register(new Skull());

        // player
        commands.register(new Feed());
        commands.register(new GameModeCommand());
        commands.register(new Heal());
        commands.register(new NightVision());
        commands.register(new Speed());

        // world
        commands.register(new Slime());
        commands.register(new Spawn());
        commands.register(new Teleport());
        commands.register(new Back());

        // sistemas
        commands.register(new FlyCommand());
        commands.register(new GodCommand());
        commands.register(new KitCommand());
        commands.register(new HomeCommand());
        commands.register(new TeleportRequestCommand());
        commands.register(new TeleportDirectCommand());
        commands.register(new VanishCommand());
    }
}
