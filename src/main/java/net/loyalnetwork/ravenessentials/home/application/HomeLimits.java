package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.minecraft.server.level.ServerPlayer;

/** Porta: quantas homes um jogador pode ter (vem das permissões e da config). */
public interface HomeLimits {

    HomeLimit of(ServerPlayer player);
}
