package net.loyalnetwork.ravenessentials.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagesTest {

    @Test
    void primeiraPaginaCheia() {
        Pages pages = new Pages(25, 21, 0);
        assertEquals(0, pages.first());
        assertEquals(21, pages.itemsOnPage());
        assertTrue(pages.hasNext());
        assertFalse(pages.hasPrevious());
    }

    @Test
    void ultimaPaginaComOResto() {
        Pages pages = new Pages(25, 21, 1);
        assertEquals(21, pages.first());
        assertEquals(4, pages.itemsOnPage());
        assertFalse(pages.hasNext());
        assertTrue(pages.hasPrevious());
    }

    @Test
    void paginaForaDoIntervaloVaiParaAPontaMaisPerto() {
        assertEquals(1, new Pages(25, 21, 7).current());
        assertEquals(0, new Pages(25, 21, -3).current());
    }

    @Test
    void semItensTemUmaPaginaVazia() {
        Pages pages = new Pages(0, 21, 0);
        assertEquals(0, pages.itemsOnPage());
        assertFalse(pages.hasNext());
    }

    // Uma config sem posições de kit não pode dividir por zero.
    @Test
    void semPosicoesNaoQuebra() {
        Pages pages = new Pages(3, 0, 0);
        assertEquals(1, pages.itemsOnPage());
        assertTrue(pages.hasNext());
    }

    @Test
    void botoesDePaginaSoQuandoHaParaOndeIr() {
        Pages only = new Pages(3, 21, 0);
        assertFalse(only.shows(MenuAction.NEXT_PAGE));
        assertFalse(only.shows(MenuAction.PREVIOUS_PAGE));
        assertTrue(only.shows(MenuAction.CLOSE));
    }
}
