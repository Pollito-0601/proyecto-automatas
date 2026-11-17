package pro.chomskysimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import pro.chomskysimulator.dto.SimulationResult;

class Nivel0AutomataServiceTest {
    private Nivel0AutomataService service;

    @BeforeEach
    void setUp() {
        service = new Nivel0AutomataService();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "abbcc", "aabcc", "aabbcccc"})
    @DisplayName("Debe aceptar cadenas de la forma a^n b^m c^(n*m) (n, m >= 1)")
    void debeAceptarCadenasValidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertTrue(resultado.accepted(), "La cadena '" + cadena + "' debería ser aceptada.");
        assertEquals("q_accept", resultado.finalState(), "El estado final debería ser q_accept.");
        assertFalse(resultado.steps().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ab", "abcc", "aabbcc", "abcba", "abbccc", "a", "b", "bb", "abcb", "aabcbc"})
    @DisplayName("Debe rechazar cadenas que no cumplan el producto a^n b^m c^(n*m)")
    void debeRechazarCadenasInvalidas(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertFalse(resultado.accepted(), "La cadena \"" + cadena + "\" debería ser rechazada.");
        assertNotEquals("q_accept", resultado.finalState());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Y", "bZ", "aYY", "abcX", "abcB"})
    @DisplayName("Debe rechazar cadenas con símbolos fuera del alfabeto de entrada {a, b, c}")
    void debeRechazarSimbolosFueraDelAlfabeto(String cadena) {
        SimulationResult resultado = service.simular(cadena);

        assertFalse(resultado.accepted(), "La cadena \"" + cadena + "\" debería ser rechazada.");
        assertEquals("garbageCollector", resultado.finalState());
    }

    @Test
    @DisplayName("Debe verificar que la cinta de Turing contenga blancos B y cabezal funcional")
    void debeContenerBlancosEnCinta() {
        SimulationResult resultado = service.simular("abc");

        assertTrue(resultado.accepted());
        assertTrue(resultado.steps().get(0).memory().contains("B"));
    }
}