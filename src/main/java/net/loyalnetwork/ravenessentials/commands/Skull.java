package net.loyalnetwork.ravenessentials.commands;

import com.mojang.authlib.properties.PropertyMap;
import net.loyalnetwork.coffeelib.api.command.annotation.Arg;
import net.loyalnetwork.coffeelib.api.command.annotation.Command;
import net.loyalnetwork.coffeelib.api.command.annotation.Default;
import net.loyalnetwork.coffeelib.api.command.annotation.Suggest;
import net.loyalnetwork.ravenessentials.RavenPermissions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Collection;
import java.util.Optional;

public class Skull {

    // /skull [player]   sem nome = a própria cabeça. Funciona com jogadores offline pelo nome.
    @Command(value = "skull", permission = RavenPermissions.SKULL)
    void skull(ServerPlayer player, @Arg("player") @Default("") @Suggest(provider = "online") String name) {
        String owner = name.isEmpty() ? player.getGameProfile().getName() : name;
        ServerPlayer online = player.server.getPlayerList().getPlayerByName(owner);

        // Jogador online: perfil completo, com a skin. Offline: só o nome, o cliente busca a skin.
        ResolvableProfile profile = online != null
                ? new ResolvableProfile(online.getGameProfile())
                : new ResolvableProfile(Optional.of(owner), Optional.empty(), new PropertyMap());

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, profile);

        player.getInventory().placeItemBackInInventory(head);
        player.sendSystemMessage(Component.literal("Você recebeu a cabeça de " + owner + "."));
    }

    Collection<String> online() {
        return Targets.onlineNames();
    }
}
