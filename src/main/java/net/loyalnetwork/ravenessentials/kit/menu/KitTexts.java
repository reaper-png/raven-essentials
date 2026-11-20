package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.ravenessentials.kit.domain.Cooldown;
import net.loyalnetwork.ravenessentials.kit.domain.Kit;
import net.loyalnetwork.ravenessentials.kit.domain.KitStatus;

import java.time.Duration;

/** Kits, cooldowns e tempos como texto para o jogador. */
public final class KitTexts {

    private KitTexts() {
    }

    /** {kit}, {items} e {cooldown} de um kit. */
    public static Placeholders of(Kit kit) {
        return Placeholders.NONE
                .with("kit", kit.appearance().name())
                .with("id", kit.id().value())
                .with("items", String.valueOf(kit.items().count()))
                .with("cooldown", cooldown(kit.rules().cooldown()));
    }

    /** {@link #of(Kit)} e o {remaining} do status. */
    public static Placeholders of(Kit kit, KitStatus status) {
        return of(kit).with("remaining", remaining(status));
    }

    public static String cooldown(Cooldown cooldown) {
        if (cooldown.isNone()) {
            return "nenhum";
        }
        if (cooldown.isOnce()) {
            return "uma vez só";
        }
        return time(cooldown.duration());
    }

    private static String remaining(KitStatus status) {
        return status instanceof KitStatus.OnCooldown cooldown ? time(cooldown.remaining()) : "";
    }

    /** {@code 1d 2h 3m 4s}, sem as partes zeradas; arredonda os segundos para cima. */
    public static String time(Duration duration) {
        long seconds = Math.max(1, (duration.toMillis() + 999) / 1000);
        StringBuilder text = new StringBuilder();
        append(text, seconds / 86_400, "d");
        append(text, seconds / 3_600 % 24, "h");
        append(text, seconds / 60 % 60, "m");
        append(text, seconds % 60, "s");
        return text.toString().trim();
    }

    private static void append(StringBuilder text, long amount, String unit) {
        if (amount > 0) {
            text.append(amount).append(unit).append(' ');
        }
    }
}
