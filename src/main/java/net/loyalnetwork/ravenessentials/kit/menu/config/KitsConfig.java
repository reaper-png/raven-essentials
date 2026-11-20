package net.loyalnetwork.ravenessentials.kit.menu.config;

import net.loyalnetwork.coffeelib.api.config.ConfigFormat;
import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.coffeelib.api.config.annotation.ConfigFile;
import net.loyalnetwork.coffeelib.api.config.annotation.Key;
import net.loyalnetwork.ravenessentials.kit.infrastructure.FullInventory;

/**
 * {@code raven/essentials/kits.yml}: como os kits aparecem para os jogadores. Os kits
 * em si (itens, cooldown, permissão) são montados no {@code /kit admin} e ficam no banco.
 * Recarregar: {@code /coffeelib config reload ravenessentials}.
 */
@ConfigFile(value = "kits", format = ConfigFormat.YAML)
public class KitsConfig {

    @Comment("Lista de kits (/kit view e /kit claim)")
    public KitListMenuConfig menu = new KitListMenuConfig();

    @Comment("Itens de um kit (ao clicar num kit no /kit view)")
    public KitPreviewMenuConfig preview = new KitPreviewMenuConfig();

    @Key("inventory-full")
    @Comment("Kit que não cabe no inventário. DROP: entrega e o resto cai no chão. DENY: não entrega")
    public FullInventory inventoryFull = FullInventory.DROP;

    public KitMessages messages = new KitMessages();

    @Comment("Placeholders da CoffeeLib, em qualquer texto (de qualquer mod que use a CoffeeLib): "
            + "%ravenessentials_kits_count%, e com o id do kit no fim: name, id, items, cooldown, uses, "
            + "e do jogador: status, remaining, times. Ex.: %ravenessentials_kits_remaining_vip%. "
            + "No singular, sobre o kit do texto (ícone, prévia, mensagens): %ravenessentials_kit_remaining%. "
            + "Aqui, o que o status mostra")
    public KitStatusTexts status = new KitStatusTexts();
}
