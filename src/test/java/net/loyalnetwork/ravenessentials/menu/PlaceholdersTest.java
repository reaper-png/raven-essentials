package net.loyalnetwork.ravenessentials.menu;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Sem assunto, o contexto é vazio: os placeholders no singular ficam como estão.
    @Test
    void semAssuntoOContextoEVazio() {
        assertTrue(Placeholders.NONE.context().isEmpty());
        assertTrue(Placeholders.NONE.with("kit", "VIP").context().isEmpty());
    }

    // O Kit precisa do Minecraft; qualquer objeto mostra o mesmo caminho até o TextContext.
    @Test
    void aboutLevaOAssuntoAteOContextoEWithMantem() {
        Placeholders values = Placeholders.NONE.about(42).with("kit", "VIP");
        assertEquals(Optional.of(42), values.context().get(Integer.class));
        assertEquals("VIP", values.apply("{kit}"));
    }

    @Test
    void aboutDeOutroDoMesmoTipoSubstitui() {
        Placeholders values = Placeholders.NONE.about(1).about(2);
        assertEquals(Optional.of(2), values.context().get(Integer.class));
    }

    @Test
    void withNaoMudaOOriginal() {
        Placeholders base = Placeholders.NONE.with("kit", "VIP");
        base.with("kit", "Inicial");
        assertEquals("VIP", base.apply("{kit}"));
    }
}
