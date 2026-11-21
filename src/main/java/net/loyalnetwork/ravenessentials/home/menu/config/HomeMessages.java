package net.loyalnetwork.ravenessentials.home.menu.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Key;

/**
 * O que o jogador lê no chat. Placeholders: {home}, {owner}, {count}, {limit}, {homes} (a
 * lista das suas), e os da home: {world}, {x}, {y}, {z}, {public}.
 */
public class HomeMessages {

    public String teleported = "&aTeleportado para a home {home}.";

    @Key("teleported-other")
    public String teleportedOther = "&aTeleportado para a home {home} de {owner}.";

    public String created = "&aHome {home} criada &7({count}/{limit})&a.";

    public String moved = "&aHome {home} movida para cá.";

    @Key("limit-reached")
    public String limitReached = "&cVocê já tem {limit} homes. Apague uma, ou use /sethome com o nome de uma que já existe.";

    @Key("not-found")
    public String notFound = "&cVocê não tem uma home chamada {home}. Suas homes: &f{homes}";

    @Key("no-homes")
    public String noHomes = "&cVocê ainda não tem homes. Crie uma com /sethome.";

    @Key("other-not-found")
    public String otherNotFound = "&c{owner} não tem uma home chamada {home}.";

    @Key("player-not-found")
    public String playerNotFound = "&cJogador não encontrado: {owner}.";

    @Key("private")
    public String privateHome = "&cA home {home} de {owner} é particular.";

    @Key("world-missing")
    public String worldMissing = "&cO mundo da home {home} não existe mais.";

    public String deleted = "&aHome {home} apagada.";

    public String failed = "&cNão deu para fazer isso agora. Tente de novo em instantes.";
}
