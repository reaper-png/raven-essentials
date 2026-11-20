package net.loyalnetwork.ravenessentials.kit.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholdersTest {

    @Test
    void trocaTodasAsOcorrencias() {
        Placeholders values = Placeholders.NONE.with("kit", "VIP").with("items", "3");
        assertEquals("&eVIP: 3 itens (VIP)", values.apply("&e{kit}: {items} itens ({kit})"));
    }

    @Test
    void desconhecidosFicamComoEstao() {
        assertEquals("{outro}", Placeholders.NONE.with("kit", "VIP").apply("{outro}"));
    }

    @Test
    void withNaoMudaOOriginal() {
        Placeholders base = Placeholders.NONE.with("kit", "VIP");
        base.with("kit", "Inicial");
        assertEquals("VIP", base.apply("{kit}"));
    }
}
