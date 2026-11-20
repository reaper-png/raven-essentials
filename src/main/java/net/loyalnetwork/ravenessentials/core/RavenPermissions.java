package net.loyalnetwork.ravenessentials.core;

import net.loyalnetwork.coffeelib.api.permission.PermissionDefault;
import net.loyalnetwork.coffeelib.api.permission.annotation.Permission;

/**
 * Every RavenEssentials permission node, decided by LuckPerms when it is installed.
 * <p>
 * Command nodes are only constants: CoffeeLib declares each one when its command is
 * registered (operators by default, or the command's {@code @Permission}), and the
 * {@code .others} nodes come from {@code @OrSelf}. Only nodes no command declares
 * carry {@code @Permission} here.
 */
public final class RavenPermissions {

    // ---- commands ----

    public static final String ANVIL = "ravenessentials.anvil";
    public static final String BACK = "ravenessentials.back";
    public static final String CLEAR = "ravenessentials.clear";
    public static final String CLEARCHAT = "ravenessentials.clearchat";
    public static final String COLORS = "ravenessentials.colors";
    public static final String COMPACT = "ravenessentials.compact";
    public static final String CRAFT = "ravenessentials.craft";
    public static final String ENCHANT = "ravenessentials.enchant";
    public static final String ENDERCHEST = "ravenessentials.enderchest";
    public static final String FEED = "ravenessentials.feed";
    public static final String GAMEMODE = "ravenessentials.gamemode";
    public static final String HAT = "ravenessentials.hat";
    public static final String KIT = "ravenessentials.kit";
    public static final String KIT_ADMIN = "ravenessentials.kit.admin";
    public static final String KIT_GIVE = "ravenessentials.kit.give";
    public static final String KIT_RESET = "ravenessentials.kit.reset";
    public static final String LIST = "ravenessentials.list";
    public static final String NIGHTVISION = "ravenessentials.nightvision";
    public static final String ONLINE = "ravenessentials.online";
    public static final String PING = "ravenessentials.ping";
    public static final String POTION = "ravenessentials.potion";
    public static final String REPAIR = "ravenessentials.repair";
    public static final String SKULL = "ravenessentials.skull";
    public static final String SLIME = "ravenessentials.slime";
    public static final String SMELT = "ravenessentials.smelt";
    public static final String SPAWN = "ravenessentials.spawn";
    public static final String SPEED = "ravenessentials.speed";
    public static final String TITLE = "ravenessentials.title";
    public static final String TP = "ravenessentials.tp";
    public static final String TRASH = "ravenessentials.trash";

    public static final String FLY = "ravenessentials.fly";
    public static final String FLY_OTHERS = "ravenessentials.fly.others";
    public static final String GOD = "ravenessentials.god";
    public static final String GOD_OTHERS = "ravenessentials.god.others";
    public static final String HEAL = "ravenessentials.heal";
    public static final String HEAL_OTHERS = "ravenessentials.heal.others";
    public static final String VANISH = "ravenessentials.vanish";
    public static final String VANISH_OTHERS = "ravenessentials.vanish.others";

    // ---- not tied to a command: declared here ----

    // De todos por padrão, para não quebrar o voo de outros mods; negue para barrar.
    @Permission(def = PermissionDefault.EVERYONE, description = "Fly by means other than /fly (other mods, items)")
    public static final String FLY_ALLOW = "ravenessentials.fly.allow";

    @Permission(description = "Open a kit preview for another player (/kit view <player> <kit>)")
    public static final String KIT_VIEW_OTHERS = "ravenessentials.kit.view.others";

    @Permission(description = "See players who are vanished")
    public static final String VANISH_SEE = "ravenessentials.vanish.see";

    // Ninguém por padrão: em vanish não se pega itens nem XP, a não ser que este nó seja dado.
    @Permission(def = PermissionDefault.NOBODY, description = "Pick up items and XP while vanished")
    public static final String VANISH_PICKUP = "ravenessentials.vanish.pickup";

    private RavenPermissions() {
    }
}
