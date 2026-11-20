package net.loyalnetwork.ravenessentials.kit.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ViewRequestTest {

    private static final String NONE = ViewRequest.NONE;

    @Test
    void semArgumentosELista() {
        assertInstanceOf(ViewRequest.List.class, ViewRequest.of(NONE, NONE));
    }

    @Test
    void umArgumentoEOKitDeQuemDigitou() {
        assertEquals(new ViewRequest.Own("vip"), ViewRequest.of("vip", NONE));
    }

    @Test
    void doisArgumentosSaoJogadorEKit() {
        assertEquals(new ViewRequest.ForPlayer("Renan", "vip"), ViewRequest.of("Renan", "vip"));
    }

    // Sem o primeiro, o segundo não tem como ter sido digitado: o Brigadier lê em ordem.
    @Test
    void segundoSemPrimeiroAindaELista() {
        assertInstanceOf(ViewRequest.List.class, ViewRequest.of(NONE, "vip"));
    }
}
