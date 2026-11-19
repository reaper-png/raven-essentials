package net.loyalnetwork.ravenessentials.mixin.fly;

import net.loyalnetwork.ravenessentials.fly.Flight;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * As habilidades que o servidor manda para o cliente levam o "pode voar" do atributo de voo,
 * para clientes que não são NeoForge (que não recebem o atributo) também conseguirem voar com o /fly.
 * onUpdateAbilities cobre login, respawn e qualquer mudança; changeDimension manda as próprias.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Redirect(
            method = {"onUpdateAbilities", "changeDimension"},
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/entity/player/Abilities;)Lnet/minecraft/network/protocol/game/ClientboundPlayerAbilitiesPacket;"
            )
    )
    private ClientboundPlayerAbilitiesPacket ravenessentials$sendEffectiveMayFly(Abilities abilities) {
        return Flight.abilitiesPacket((ServerPlayer) (Object) this, abilities);
    }
}
