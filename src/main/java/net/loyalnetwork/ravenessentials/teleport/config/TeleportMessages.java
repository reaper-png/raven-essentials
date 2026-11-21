package net.loyalnetwork.ravenessentials.teleport.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Key;

/**
 * O que o jogador lê no chat. Placeholders: {player} (a outra pessoa), {count} (no /tpall).
 * Os botões do pedido aceitam {player} no texto e no hover.
 */
public class TeleportMessages {

    public String sent = "&aPedido de teleporte enviado para {player}.";

    public String received = "&e{player} quer se teleportar até você.";

    @Key("accept-button")
    public String acceptButton = "&a&l[ACEITAR]";

    @Key("deny-button")
    public String denyButton = "&c&l[NEGAR]";

    @Key("accept-hover")
    public String acceptHover = "&7Aceitar o pedido de {player}.";

    @Key("deny-hover")
    public String denyHover = "&7Negar o pedido de {player}.";

    public String accepted = "&aPedido de {player} aceito.";

    @Key("accepted-other")
    public String acceptedOther = "&a{player} aceitou seu pedido de teleporte.";

    public String denied = "&cPedido de {player} negado.";

    @Key("denied-other")
    public String deniedOther = "&c{player} negou seu pedido de teleporte.";

    public String cancelled = "&cPedido para {player} cancelado.";

    @Key("cancelled-other")
    public String cancelledOther = "&c{player} cancelou o pedido de teleporte.";

    @Key("toggle-on")
    public String toggleOn = "&aPedidos de teleporte ativados.";

    @Key("toggle-off")
    public String toggleOff = "&cPedidos de teleporte desativados.";

    public String blocked = "&c{player} está com os pedidos de teleporte desativados.";

    public String self = "&cVocê não pode fazer isso consigo mesmo.";

    @Key("already-sent")
    public String alreadySent = "&cVocê já tem um pedido pendente para {player}. &7Use /tpcancel para cancelar.";

    @Key("no-requests")
    public String noRequests = "&cVocê não tem pedidos de teleporte pendentes.";

    @Key("no-request-from")
    public String noRequestFrom = "&c{player} não tem um pedido pendente para você.";

    @Key("no-sent")
    public String noSent = "&cVocê não tem pedidos de teleporte enviados.";

    @Key("no-sent-to")
    public String noSentTo = "&cVocê não tem um pedido pendente para {player}.";

    public String expired = "&eO pedido de teleporte de {player} expirou.";

    @Key("expired-other")
    public String expiredOther = "&eSeu pedido de teleporte para {player} expirou.";

    public String here = "&a{player} foi teleportado até você.";

    @Key("here-other")
    public String hereOther = "&aVocê foi teleportado até {player}.";

    public String all = "&a{count} jogadores teleportados até você.";

    @Key("all-empty")
    public String allEmpty = "&cNão há jogadores para teleportar até você.";

    @Key("all-other")
    public String allOther = "&aVocê foi teleportado até {player}.";

    public String failed = "&cNão deu para teleportar agora. Tente de novo em instantes.";
}
