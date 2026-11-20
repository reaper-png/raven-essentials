package net.loyalnetwork.ravenessentials.kit.menu.config;

import net.loyalnetwork.coffeelib.api.config.annotation.Comment;

import java.util.ArrayList;
import java.util.List;

/** A descrição de um kit na lista, conforme o jogador pode ou não pegá-lo. */
public class KitLore {

    @Comment("Pode pegar agora")
    public List<String> available = new ArrayList<>(List.of(
            "&7Itens: &f{items}",
            "&7Cooldown: &f{cooldown}",
            "&7Usos: &f{uses}",
            "",
            "{action}"));

    @Comment("Ainda em cooldown. {remaining}: quanto falta")
    public List<String> cooldown = new ArrayList<>(List.of(
            "&7Itens: &f{items}",
            "&7Cooldown: &f{cooldown}",
            "&7Usos: &f{uses}",
            "",
            "&cDisponível em &f{remaining}"));

    @Comment("Kit restrito, sem a permissão")
    public List<String> locked = new ArrayList<>(List.of(
            "&7Itens: &f{items}",
            "",
            "&cVocê não tem acesso a este kit."));

    @Comment("Kit de uma vez só, já pego")
    public List<String> claimed = new ArrayList<>(List.of(
            "&7Itens: &f{items}",
            "",
            "&cVocê já pegou este kit."));
}
