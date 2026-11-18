package net.loyalnetwork.ravenessentials.util;

import net.loyalnetwork.coffeelib.api.permission.PermissionManager;
import net.loyalnetwork.ravenessentials.RavenEssentials;

/** Checagem de permissão em um lugar só. Aceita ServerPlayer ou CommandSourceStack. */
public final class Permissions {

    private Permissions() {
    }

    public static boolean has(Object subject, String node) {
        PermissionManager permissions = RavenEssentials.permissions();
        return permissions != null && permissions.has(subject, node);
    }
}
