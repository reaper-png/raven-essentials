package net.loyalnetwork.ravenessentials.util;

import net.loyalnetwork.coffeelib.api.permission.PermissionManager;

/** Checagem de permissão em um lugar só. Aceita ServerPlayer ou CommandSourceStack. */
public final class Permissions {

    private static PermissionManager manager;

    private Permissions() {
    }

    /** Chamado uma vez pelo Bootstrap, com os nós já declarados. */
    public static void init(PermissionManager permissions) {
        manager = permissions;
    }

    public static boolean has(Object subject, String node) {
        return manager != null && manager.has(subject, node);
    }
}
