package net.loyalnetwork.ravenessentials.god;

import net.loyalnetwork.ravenessentials.core.RavenAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** Estado do modo god, salvo no jogador. As regras ficam em {@link GodEvents} e no mixin de vida. */
public final class God {

    private God() {
    }

    /** Se é um jogador em modo god. Aceita qualquer entidade (ou null) para os eventos e mixins. */
    public static boolean isGod(@Nullable Entity entity) {
        return entity instanceof ServerPlayer player && player.getData(RavenAttachments.GOD);
    }

    public static void setGod(ServerPlayer player, boolean enabled) {
        player.setData(RavenAttachments.GOD, enabled);
    }
}
