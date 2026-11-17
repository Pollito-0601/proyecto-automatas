package pro.chomskysimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import pro.chomskysimulator.dto.SimulationResult;

class Nivel2AutomataServiceTest {
    private Nivel2AutomataService service;

    @BeforeEach
    void setUp() {
        service = new Nivel2AutomataService();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "aabb", "aaabbb", "aaaabbbb"})
    @DisplayName("Debe aceptar cadenas balanceadas con formato a^n b^n (n >= 1)")
    void debeAceptarCadenasValidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertTrue(resultado.accepted(), "La cadena '" + cadena + "' debería ser aceptada.");
        assertEquals("q2", resultado.finalState(), "El estado final debería ser q2.");
        assertEquals(cadena.length() + 1, resultado.steps().size(), "El número de pasos debería ser cadena.length() + 1.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "b", "aab", "abb", "ba", "abab", "aabbb", "aaabb"})
    @DisplayName("Debe rechazar cadenas que no cumplan el formato a^n b^n")
    void debeRechazarCadenasInvalidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertFalse(resultado.accepted(), "La cadena \"" + cadena + "\" debería ser rechazada.");
    }

    @Test
    @DisplayName("Debe manejar de forma segura entradas nulas")
    void debeManejarEntradaNula() {
        SimulationResult resultado = service.simular(null);

        assertNotNull(resultado);
        assertFalse(resultado.accepted());
        assertEquals("q0", resultado.finalState());
        assertEquals(1, resultado.steps().size());
    }

    @Test
    @DisplayName("La traza debe reflejar el apilado y desapilado correcto")
    void debeVerificarTrazaYPila() {
        SimulationResult resultado = service.simular("aabb");

        assertEquals(5, resultado.steps().size());
        // Paso 0: Pila vacía
        assertTrue(resultado.steps().get(0).memory().isEmpty());
        // Paso 1: 'a' -> 1 elemento en pila
        assertEquals(1, resultado.steps().get(1).memory().size());
        assertEquals("A", resultado.steps().get(1).memory().get(0));
        // Paso 2: 'a' -> 2 elementos en pila
        assertEquals(2, resultado.steps().get(2).memory().size());
        // Paso 3: 'b' -> 1 elemento en pila (desapiló uno)
        assertEquals(1, resultado.steps().get(3).memory().size());
        // Paso 4: 'b' -> 0 elementos en pila (desapiló el último y acepta)
        assertTrue(resultado.steps().get(4).memory().isEmpty());
        assertTrue(resultado.accepted());
    }
}
