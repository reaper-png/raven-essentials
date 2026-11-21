package net.loyalnetwork.ravenessentials.teleport.config;

import net.loyalnetwork.coffeelib.api.config.ConfigFormat;
import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.coffeelib.api.config.annotation.ConfigFile;
import net.loyalnetwork.coffeelib.api.config.annotation.Key;
import net.loyalnetwork.coffeelib.api.config.annotation.Range;

/**
 * {@code raven/essentials/tpa.yml}: quanto tempo um pedido fica pendente e o que o jogador lê.
 * Recarregar: {@code /coffeelib config reload ravenessentials}.
 */
@ConfigFile(value = "tpa", format = ConfigFormat.YAML)
public class TeleportConfig {

    @Key("request-seconds")
    @Range(min = 5, max = 600)
    @Comment("Quanto tempo um pedido de teleporte fica pendente antes de expirar")
    public int requestSeconds = 60;

    public TeleportMessages messages = new TeleportMessages();
}
