package net.loyalnetwork.ravenessentials.fly;

import net.loyalnetwork.ravenessentials.core.RavenAttachments;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.loyalnetwork.ravenessentials.util.Permissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Voo do RavenEssentials, separado de qualquer outro voo (outros mods, itens, habilidades).
 * <ul>
 *   <li><b>/fly</b>: salvo no jogador e só ativo com {@link RavenPermissions#FLY}. Dá o voo por um
 *       modificador {@code +1} no atributo {@code neoforge:creative_flight}.</li>
 *   <li><b>Outros voos</b>: liberados por {@link RavenPermissions#FLY_ALLOW}. Sem ela, um modificador
 *       {@code ×0} (o "desligar" padrão do NeoForge para atributos booleanos) zera o atributo, e o
 *       {@code abilities.mayfly} que outro mod ligar em sobrevivência é desfeito.</li>
 * </ul>
 * Criativo e espectador sempre voam: isso é do modo de jogo, não um "modo de voo".
 * Quando o voo some, o próprio vanilla derruba o jogador no tick seguinte.
 */
public final class Flight {

    private static final ResourceLocation GRANT = id("fly");
    private static final ResourceLocation BLOCK = id("fly_block");

    private Flight() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavenEssentials.MODID, path);
    }

    /** Se o /fly está ligado (salvo), independente da permissão. */
    public static boolean isEnabled(ServerPlayer player) {
        return player.getData(RavenAttachments.FLY);
    }

    /** Liga/desliga o /fly e já aplica. Retorna se ficou voando de fato (pode faltar permissão). */
    public static boolean setEnabled(ServerPlayer player, boolean enabled) {
        player.setData(RavenAttachments.FLY, enabled);
        sync(player);
        return isEnabled(player);
    }

    /**
     * Aplica o estado e as permissões atuais. Sem {@link RavenPermissions#FLY}, o /fly é desligado
     * de vez (não volta sozinho se a permissão for devolvida).
     */
    public static void sync(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight == null) return;

        boolean ours = isEnabled(player);
        if (ours && !Permissions.has(player, RavenPermissions.FLY)) {
            player.setData(RavenAttachments.FLY, false);
            player.sendSystemMessage(Messages.error("Seu fly foi desativado: você não tem mais permissão."));
            ours = false;
        }

        // Com o /fly ativo não há o que barrar: o bloqueio zeraria o nosso também.
        boolean block = !ours && !Permissions.has(player, RavenPermissions.FLY_ALLOW);

        setModifier(flight, GRANT, ours, 1, AttributeModifier.Operation.ADD_VALUE);
        setModifier(flight, BLOCK, block, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        if (block) {
            blockLegacyFlight(player);
        }
    }

    private static void setModifier(AttributeInstance attribute, ResourceLocation id, boolean present,
                                    double amount, AttributeModifier.Operation operation) {
        if (present == attribute.hasModifier(id)) return;

        if (present) {
            // Transitório: não vai para o save. O estado salvo é o attachment, reaplicado em cada sync.
            attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
        } else {
            attribute.removeModifier(id);
        }
    }

    /**
     * Mods que ainda ligam {@code abilities.mayfly} direto escapam do atributo. Em sobrevivência e
     * aventura esse campo só fica ligado por eles (o modo de jogo o desliga), então é desfeito.
     */
    @SuppressWarnings("deprecation")
    private static void blockLegacyFlight(ServerPlayer player) {
        var abilities = player.getAbilities();
        if (abilities.mayfly && player.gameMode.isSurvival()) {
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }
}
