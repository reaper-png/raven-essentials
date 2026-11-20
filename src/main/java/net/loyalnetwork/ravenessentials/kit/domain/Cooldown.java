package net.loyalnetwork.ravenessentials.kit.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quanto tempo depois de pegar um kit se pode pegar de novo: nada, um tempo, ou nunca
 * (kit de uma vez só).
 */
public record Cooldown(long seconds) {

    private static final long ONCE_SECONDS = -1;

    public static final Cooldown NONE = new Cooldown(0);
    public static final Cooldown ONCE = new Cooldown(ONCE_SECONDS);

    private static final Pattern FULL = Pattern.compile("(\\d+\\s*[dhms]\\s*)+");
    private static final Pattern PART = Pattern.compile("(\\d+)\\s*([dhms])");

    public Cooldown {
        if (seconds < ONCE_SECONDS) {
            throw new KitException("Cooldown inválido: " + seconds);
        }
    }

    public static Cooldown of(Duration duration) {
        return new Cooldown(duration.toSeconds());
    }

    /**
     * O que um admin digita: {@code 0}/{@code nenhum}, {@code uma-vez}/{@code once},
     * um número de segundos, ou partes como {@code 1d 12h 30m 10s}.
     */
    public static Cooldown parse(String input) {
        String text = input.trim().toLowerCase(Locale.ROOT);
        if (text.equals("0") || text.equals("nenhum") || text.equals("none")) {
            return NONE;
        }
        if (text.equals("uma-vez") || text.equals("uma vez") || text.equals("once")) {
            return ONCE;
        }
        if (text.matches("\\d{1,12}")) {
            return new Cooldown(Long.parseLong(text));
        }
        if (!FULL.matcher(text).matches()) {
            throw new KitException("Cooldown inválido: \"" + input + "\". Exemplos: 0, uma-vez, 90, 1d 12h, 30m.");
        }
        return of(sum(PART.matcher(text)));
    }

    private static Duration sum(Matcher parts) {
        Duration total = Duration.ZERO;
        while (parts.find()) {
            total = total.plus(unit(parts.group(2)).multipliedBy(Long.parseLong(parts.group(1))));
        }
        return total;
    }

    private static Duration unit(String letter) {
        return switch (letter) {
            case "d" -> Duration.ofDays(1);
            case "h" -> Duration.ofHours(1);
            case "m" -> Duration.ofMinutes(1);
            default -> Duration.ofSeconds(1);
        };
    }

    public boolean isOnce() {
        return seconds == ONCE_SECONDS;
    }

    public boolean isNone() {
        return seconds == 0;
    }

    /** O tempo de espera; zero para {@link #NONE} e {@link #ONCE}. */
    public Duration duration() {
        return isOnce() ? Duration.ZERO : Duration.ofSeconds(seconds);
    }

    /** Quando se pode pegar de novo depois de {@code lastClaim}; vazio quando nunca. */
    Optional<Instant> availableAfter(Instant lastClaim) {
        if (isOnce()) {
            return Optional.empty();
        }
        return Optional.of(lastClaim.plusSeconds(seconds));
    }
}
