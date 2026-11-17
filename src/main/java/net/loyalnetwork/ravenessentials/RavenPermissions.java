package net.loyalnetwork.ravenessentials;

import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;

/** Permission nodes for every RavenEssentials command, decided by LuckPerms when it is installed. */
public final class RavenPermissions {

    @Permission(description = "Use /hat")
    public static final String HAT = "ravenessentials.hat";

    @Permission(description = "Use /fly")
    public static final String FLY = "ravenessentials.fly";

    @Permission(description = "Use /clear")
    public static final String CLEAR = "ravenessentials.clear";

    @Permission(description = "Use /anvil")
    public static final String ANVIL = "ravenessentials.anvil";

    @Permission(description = "Use /feed")
    public static final String FEED = "ravenessentials.feed";

    @Permission(description = "Use /craft")
    public static final String CRAFT = "ravenessentials.craft";

    @Permission(description = "Use /ec")
    public static final String ENDERCHEST = "ravenessentials.enderchest";

    @Permission(description = "Use /compact")
    public static final String COMPACT = "ravenessentials.compact";

    @Permission(description = "Use /colors")
    public static final String COLORS = "ravenessentials.colors";

    @Permission(description = "Use /clearchat")
    public static final String CLEARCHAT = "ravenessentials.clearchat";

    @Permission(description = "Use /smelt")
    public static final String SMELT = "ravenessentials.smelt";

    @Permission(description = "Use /enchant")
    public static final String ENCHANT = "ravenessentials.enchant";

    @Permission(description = "Use /gm")
    public static final String GAMEMODE = "ravenessentials.gamemode";

    @Permission(description = "Use /title")
    public static final String TITLE = "ravenessentials.title";

    @Permission(description = "Use /vanish")
    public static final String VANISH = "ravenessentials.vanish";

    @Permission(description = "Toggle vanish for other players")
    public static final String VANISH_OTHERS = "ravenessentials.vanish.others";

    @Permission(description = "See players who are vanished")
    public static final String VANISH_SEE = "ravenessentials.vanish.see";

    // Ninguém por padrão: em vanish não se pega itens nem XP, a não ser que este nó seja dado.
    @Permission(def = PermissionDefault.NOBODY, description = "Pick up items and XP while vanished")
    public static final String VANISH_PICKUP = "ravenessentials.vanish.pickup";

    // Substitui o /list vanilla, que é de todos.
    @Permission(def = PermissionDefault.EVERYONE, description = "Use /list")
    public static final String LIST = "ravenessentials.list";

    private RavenPermissions() {
    }
}
