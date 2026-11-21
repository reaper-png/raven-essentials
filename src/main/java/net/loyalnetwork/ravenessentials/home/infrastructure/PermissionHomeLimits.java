package net.loyalnetwork.ravenessentials.home.infrastructure;

import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.ravenessentials.core.RavenPermissions;
import net.loyalnetwork.ravenessentials.home.application.HomeLimits;
import net.loyalnetwork.ravenessentials.home.domain.HomeLimit;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.IntSupplier;

/**
 * O limite de homes pelas permissões: {@code ravenessentials.homes.unlimited} não tem teto;
 * senão vale o maior {@code ravenessentials.homes.limit.<N>} que o jogador tiver (ou o meta
 * {@code ravenessentials.homes.limit} do LuckPerms); sem nenhum, o padrão da config.
 */
public final class PermissionHomeLimits implements HomeLimits {

    /** Até onde os nós {@code ravenessentials.homes.limit.<N>} são declarados. */
    public static final int HIGHEST = 100;

    private final PermissionManager permissions;
    private final IntSupplier defaultLimit;

    public PermissionHomeLimits(PermissionManager permissions, IntSupplier defaultLimit) {
        this.permissions = permissions;
        this.defaultLimit = defaultLimit;
    }

    public void declare() {
        permissions.declareRange(RavenPermissions.HOMES_LIMIT, 1, HIGHEST);
    }

    @Override
    public HomeLimit of(ServerPlayer player) {
        if (permissions.has(player, RavenPermissions.HOMES_UNLIMITED)) {
            return HomeLimit.UNLIMITED;
        }
        return new HomeLimit(permissions.number(player, RavenPermissions.HOMES_LIMIT, defaultLimit.getAsInt()));
    }
}
