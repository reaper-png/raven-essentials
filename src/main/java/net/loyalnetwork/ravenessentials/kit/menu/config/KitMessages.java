package net.loyalnetwork.ravenessentials.kit.menu.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Comment;
import net.loyalnetwork.coffeelib.api.config.annotation.Key;

/** O que o jogador lê no chat. Placeholders: {kit}, {remaining}, {items}. */
public class KitMessages {

    public String claimed = "&aVocê pegou o kit {kit}&a.";

    @Comment("Quem recebe um kit pelo /kit give")
    public String received = "&aVocê recebeu o kit {kit}&a.";

    public String cooldown = "&cVocê poderá pegar o kit {kit} &cde novo em {remaining}.";

    @Key("already-claimed")
    public String alreadyClaimed = "&cVocê já pegou o kit {kit}&c.";

    public String locked = "&cVocê não tem acesso ao kit {kit}&c.";

    @Key("not-found")
    public String notFound = "&cNão existe um kit chamado {kit}.";

    @Key("inventory-full")
    public String inventoryFull = "&cAbra espaço no inventário: o kit {kit} &cprecisa de {items} espaços livres.";

    public String busy = "&cAguarde: seu último kit ainda está sendo entregue.";

    public String failed = "&cNão deu para entregar o kit agora. Tente de novo em instantes.";
}
