package net.loyalnetwork.ravenessentials;

import com.mojang.logging.LogUtils;
import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.coffeelib.neoforge.CoffeeLib;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.loyalnetwork.ravenessentials.commands.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RavenEssentials.MODID)
public class RavenEssentials {
    public static final String MODID = "ravenessentials";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static PermissionManager permissions;

    public static PermissionManager permissions() {
        return permissions;
    }

    public RavenEssentials(IEventBus modEventBus, ModContainer modContainer) {
        // Declared before the commands so their nodes keep the defaults chosen here.
        permissions = CoffeeLib.permissions(modContainer);
        permissions.declare(RavenPermissions.class);

        Vanish.ATTACHMENTS.register(modEventBus);

        var commands = CoffeeLib.commands(modContainer);
        commands.register(new Hat());
        commands.register(new Fly());
        commands.register(new Clear());
        commands.register(new Anvil());
        commands.register(new Feed());
        commands.register(new Craft());
        commands.register(new EnderChest());
        commands.register(new Compact());
        commands.register(new Colors());
        commands.register(new ClearChat());
        commands.register(new Smelt());
        commands.register(new Enchant());
        commands.register(new GameModeCommand());
        commands.register(new VanishCommand());
        commands.register(new Title());
    }
}
