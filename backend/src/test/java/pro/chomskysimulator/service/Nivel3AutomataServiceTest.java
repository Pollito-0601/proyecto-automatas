package pro.chomskysimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import pro.chomskysimulator.dto.SimulationResult;

class Nivel3AutomataServiceTest {
    private Nivel3AutomataService service;

    @BeforeEach
    void setUp() {
        service = new Nivel3AutomataService();
    }

    @ParameterizedTest 
    @ValueSource (strings = {"ab", "aab", "abb", "aaabbb", "aaaaabbbbb"})
    @DisplayName ("Debe aceptar cadenas con formato a^n b^m (n, m >= 1)")
    void debeAceptarCadenasValidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertTrue(resultado.accepted(), "La cadena '" + cadena + "' debería ser aceptada.");
        assertEquals("q2", resultado.finalState(), "El estado final debería ser q2.");
        assertEquals(cadena.length() + 1, resultado.steps().size(), "El número de pasos debería coincidir (paso inicial + 1 por símbolo).");
    }

    @ParameterizedTest 
    @ValueSource (strings = {"", "a", "b", "ba", "aba", "aabba", "abc", "bba"})
    @DisplayName ("Debe rechazar cadenas que no cumplen con el formato a^n b^n (n, m >= 1)")
    void debeRechazarCadenasInvalidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertFalse(resultado.accepted(), "La cadena \"" + cadena + "\" debería ser rechazada.");
        assertNotEquals("q2", resultado.finalState(), "El estado final NO debería ser q2.");
    }

    @Test
    @DisplayName ("Debe manejar de forma segura entradas nulas")
    void debeManejarEntradaNula() {
        SimulationResult resultado = service.simular(null);

        assertNotNull(resultado);

        assertFalse(resultado.accepted());
        assertEquals("q0", resultado.finalState());
        assertEquals(1, resultado.steps().size());
    }

    @Test
    @DisplayName ("La traza de pasos debe documentar correctamente la transición de estados")
    void debeRegistrarTransicionesCorrectamente() {
        SimulationResult resultado = service.simular("ab");

        assertEquals(3, resultado.steps().size());
        assertEquals("q0", resultado.steps().get(0).currentState());
        assertNull(resultado.steps().get(0).symbolReaded());
        assertEquals("q0", resultado.steps().get(1).currentState());
        assertEquals('a', resultado.steps().get(1).symbolReaded());
        assertEquals("q1", resultado.steps().get(1).nextState());

        assertEquals("q1", resultado.steps().get(2).currentState());
        assertEquals('b', resultado.steps().get(2).symbolReaded());
        assertEquals("q2", resultado.steps().get(2).nextState());
        assertTrue(resultado.steps().get(2).isFinalStep());
    }
}
