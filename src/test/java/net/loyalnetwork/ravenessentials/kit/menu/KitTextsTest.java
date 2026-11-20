package net.loyalnetwork.ravenessentials.kit.menu;

import net.loyalnetwork.ravenessentials.kit.domain.Cooldown;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KitTextsTest {

    @Test
    void tempoSemAsPartesZeradas() {
        assertEquals("1h 30m 10s", KitTexts.time(Duration.ofSeconds(5410)));
        assertEquals("2d", KitTexts.time(Duration.ofDays(2)));
        assertEquals("1d 1s", KitTexts.time(Duration.ofSeconds(86_401)));
    }

    // "Disponível em 0s" seria estranho: o que falta é arredondado para cima.
    @Test
    void arredondaParaCima() {
        assertEquals("1s", KitTexts.time(Duration.ofMillis(200)));
        assertEquals("2s", KitTexts.time(Duration.ofMillis(1001)));
        assertEquals("1s", KitTexts.time(Duration.ZERO));
    }

    @Test
    void cooldownComoTexto() {
        assertEquals("nenhum", KitTexts.cooldown(Cooldown.NONE));
        assertEquals("uma vez só", KitTexts.cooldown(Cooldown.ONCE));
        assertEquals("1d 12h", KitTexts.cooldown(Cooldown.parse("1d 12h")));
    }
}
