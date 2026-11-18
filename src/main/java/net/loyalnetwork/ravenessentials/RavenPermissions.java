package net.loyalnetwork.ravenessentials;

import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;

/** Permission nodes for every RavenEssentials command, decided by LuckPerms when it is installed. */
public final class RavenPermissions {

    @Permission(description = "Use /hat")
    public static final String HAT = "ravenessentials.hat";

    @Permission(description = "Use /fly")
    public static final String FLY = "ravenessentials.fly";

    @Permission(description = "Use /fly on other players")
    public static final String FLY_OTHERS = "ravenessentials.fly.others";

    // De todos por padrão, para não quebrar o voo de outros mods; negue para barrar.
    @Permission(def = PermissionDefault.EVERYONE, description = "Fly by means other than /fly (other mods, items)")
    public static final String FLY_ALLOW = "ravenessentials.fly.allow";

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

    @Permission(description = "Use /speed")
    public static final String SPEED = "ravenessentials.speed";

    @Permission(description = "Use /spawn")
    public static final String SPAWN = "ravenessentials.spawn";

    @Permission(description = "Use /slime")
    public static final String SLIME = "ravenessentials.slime";

    @Permission(description = "Use /skull")
    public static final String SKULL = "ravenessentials.skull";

    @Permission(description = "Use /repair")
    public static final String REPAIR = "ravenessentials.repair";

    @Permission(description = "Use /potion")
    public static final String POTION = "ravenessentials.potion";

    @Permission(description = "Use /ping")
    public static final String PING = "ravenessentials.ping";

    @Permission(description = "Use /online")
    public static final String ONLINE = "ravenessentials.online";

    @Permission(description = "Use /nightvision")
    public static final String NIGHTVISION = "ravenessentials.nightvision";

    @Permission(description = "Use /trash")
    public static final String TRASH = "ravenessentials.trash";

    @Permission(description = "Use /heal")
    public static final String HEAL = "ravenessentials.heal";

    @Permission(description = "Use /heal on other players")
    public static final String HEAL_OTHERS = "ravenessentials.heal.others";

    @Permission(description = "Use /god")
    public static final String GOD = "ravenessentials.god";

    @Permission(description = "Use /god on other players")
    public static final String GOD_OTHERS = "ravenessentials.god.others";

    @Permission(description = "Use /tp")
    public static final String TP = "ravenessentials.tp";

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
