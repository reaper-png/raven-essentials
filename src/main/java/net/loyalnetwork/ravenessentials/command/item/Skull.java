package net.loyalnetwork.ravenessentials.command.item;

import com.mojang.authlib.properties.PropertyMap;
import net.loyalnetwork.coffeelib.api.command.SuggestContext;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.coffeelib.api.di.annotation.Receive;
import net.loyalnetwork.coffeelib.api.player.PlayerDirectory;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Players;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Skull {

    @Receive
    PlayerDirectory players;

    // /skull [player]   sem nome = a própria cabeça. Funciona com jogadores offline pelo nome.
    @Command(value = "skull", permission = RavenPermissions.SKULL)
    void skull(ServerPlayer player, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        String owner = name.isEmpty() ? Players.name(player) : name;
        ServerPlayer online = player.server.getPlayerList().getPlayerByName(owner);

        // Jogador online: perfil completo, com a skin. Offline: só o nome, o cliente busca a skin.
        ResolvableProfile profile = online != null
                ? new ResolvableProfile(online.getGameProfile())
                : new ResolvableProfile(Optional.of(owner), Optional.empty(), new PropertyMap());

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, profile);

        Players.give(player, head);
        Messages.send(player, "Você recebeu a cabeça de " + owner + ".");
    }

    Collection<String> online(SuggestContext context) {
        List<String> names = new ArrayList<>();
        for (Object online : players.visibleTo(context.source().player())) {
            names.add(Players.name((ServerPlayer) online));
        }
        return names;
    }
}
