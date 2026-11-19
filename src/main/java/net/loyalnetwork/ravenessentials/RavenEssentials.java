package net.loyalnetwork.ravenessentials;

import net.loyalnetwork.ravenessentials.core.Bootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(RavenEssentials.MODID)
public class RavenEssentials {

    public static final String MODID = "ravenessentials";

    public RavenEssentials(IEventBus modEventBus, ModContainer modContainer) {
        Bootstrap.start(modEventBus, modContainer);
    }
}
