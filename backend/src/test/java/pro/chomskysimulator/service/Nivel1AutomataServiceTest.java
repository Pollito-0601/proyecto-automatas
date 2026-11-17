package pro.chomskysimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import pro.chomskysimulator.dto.SimulationResult;

class Nivel1AutomataServiceTest {
    private Nivel1AutomataService service;

    @BeforeEach
    void setUp() {
        service = new Nivel1AutomataService();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "aabbcc", "aaabbbccc"})
    @DisplayName("Debe aceptar cadenas con formato a^n b^n c^n (n >= 1)")
    void debeAceptarCadenasValidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertTrue(resultado.accepted(), "La cadena '" + cadena + "' debería ser aceptada.");
        assertEquals("q_accept", resultado.finalState(), "El estado final debería ser q_accept.");
        assertFalse(resultado.steps().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "ab", "aabbc", "aabbccc", "abcba", "cba", "aabbbccc"})
    @DisplayName("Debe rechazar cadenas que no cumplan el formato a^n b^n c^n")
    void debeRechazarCadenasInvalidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertFalse(resultado.accepted(), "La cadena \"" + cadena + "\" debería ser rechazada.");
        assertNotEquals("q_accept", resultado.finalState());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Y", "YZ", "Y]", "abcX", "[abc]"})
    @DisplayName("Debe rechazar cadenas con símbolos fuera del alfabeto de entrada {a, b, c}")
    void debeRechazarSimbolosFueraDelAlfabeto(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertFalse(resultado.accepted(), "La cadena \"" + cadena + "\" debería ser rechazada.");
        assertEquals("garbageCollector", resultado.finalState());
    }

    @Test
    @DisplayName("Debe verificar que la cinta contenga los delimitadores de frontera [ y ]")
    void debeContenerDelimitadoresDeFrontera() {
        SimulationResult resultado = service.simular("abc");

        assertTrue(resultado.accepted());
        assertEquals("[", resultado.steps().get(0).memory().get(0));
        int tamanoCinta = resultado.steps().get(0).memory().size();
        assertEquals("]", resultado.steps().get(0).memory().get(tamanoCinta - 1));
    }
}