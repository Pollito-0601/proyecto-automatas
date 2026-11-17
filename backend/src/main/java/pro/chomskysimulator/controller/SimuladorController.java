package pro.chomskysimulator.controller;

import java.util.Map;

import pro.chomskysimulator.service.Nivel3AutomataService;
import pro.chomskysimulator.service.Nivel2AutomataService;
import pro.chomskysimulator.service.Nivel1AutomataService;
import pro.chomskysimulator.service.Nivel0AutomataService;
import pro.chomskysimulator.dto.SimulationRequest;
import pro.chomskysimulator.dto.SimulationResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping ("/api/v1/simulador")
public class SimuladorController {
    private final Nivel3AutomataService nivel3Service;
    private final Nivel2AutomataService nivel2Service;
    private final Nivel1AutomataService nivel1Service;
    private final Nivel0AutomataService nivel0Service;

    public SimuladorController(Nivel3AutomataService nivel3Service, Nivel2AutomataService nivel2Service, Nivel1AutomataService nivel1Service, Nivel0AutomataService nivel0Service) {
        this.nivel3Service = nivel3Service;
        this.nivel2Service = nivel2Service;
        this.nivel1Service = nivel1Service;
        this.nivel0Service = nivel0Service;
    }

    @PostMapping ("/nivel-3")
    public ResponseEntity<SimulationResult> simulateNivel3(@RequestBody SimulationRequest request) {
        String input = (request != null) ? request.input() : "";
        SimulationResult result = nivel3Service.simular(input);
        return ResponseEntity.ok(result);
    }

    @GetMapping ("/nivel-3/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoNivel3() {
        return ResponseEntity.ok(Map.of(
            "nivel", 3, "nombre", "Gramáticas regulares", "modelo", "Autómata finito determinista (AFD)", "lenguaje", "L = { a^n b^m | n, m >= 1 }", "alfabeto", new String[]{"a", "b"}, "estados", new String[]{"q0", "q1", "q2", "garbageCollector"}, "estadoInicial", "q0", "estadosAceptacion", new String[]{"q2"}, "ejemplosValidos", new String[]{"ab", "aab", "abbb", "aaabbb"}, "ejemplosInvalidos", new String[]{"", "a", "b", "ba", "aabba"}
        ));
    }

    @PostMapping ("/nivel-2")
    public ResponseEntity<SimulationResult> simulateNivel2(@RequestBody SimulationRequest request) {
        String input = (request != null) ? request.input() : "";
        SimulationResult result = nivel2Service.simular(input);
        return ResponseEntity.ok(result);
    }

    @GetMapping ("/nivel-2/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoNivel2() {
        return ResponseEntity.ok(Map.of(
            "nivel", 2, "nombre", "Gramáticas libres de contexto", "modelo", "Autómata con pila (PDA)", "lenguaje", "L = { a^n b^n | n >= 1 }", "alfabeto", new String[]{"a", "b"}, "alfabetoPila", new String[]{"A"}, "estados", new String[]{"q0", "q1", "q2", "garbageCollector"}, "estadoInicial", "q0", "estadosAceptacion", new String[]{"q2"}, "ejemplosValidos", new String[]{"ab", "aabb", "aaabbb", "aaaabbbb"}, "ejemplosInvalidos", new String[]{"", "a", "b", "aab", "abb", "ba", "abab"}
        ));
    }

    @PostMapping ("/nivel-1")
    public ResponseEntity<SimulationResult> simulateNivel1(@RequestBody SimulationRequest request) {
        String input = (request != null) ? request.input() : "";
        SimulationResult result = nivel1Service.simular(input);
        return ResponseEntity.ok(result);
    }

    @GetMapping ("/nivel-1/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoNivel1() {
        return ResponseEntity.ok(Map.of(
            "nivel", 1, "nombre", "Gramáticas sensibles al contexto", "modelo", "Autómata linealmente acotado (LBA)", "lenguaje", "L = { a^n b^n c^n | n >= 1 }", "alfabeto", new String[]{"a", "b", "c"}, "alfabetoCinta", new String[]{"a", "b", "c", "X", "Y", "Z", "[", "]"}, "estados", new String[]{"q0", "q1", "q2", "q3", "q4", "q_accept", "garbageCollector"}, "estadoInicial", "q0", "estadosAceptacion", new String[]{"q_accept"}, "ejemplosValidos", new String[]{"abc", "aabbcc", "aaabbbccc"}, "ejemplosInvalidos", new String[]{"", "ab", "aabbc", "aabbccc", "abcba"}
        ));
    }

    @PostMapping ("/nivel-0")
    public ResponseEntity<SimulationResult> simulateNivel0(@RequestBody SimulationRequest request) {
        String input = (request != null) ? request.input() : "";
        SimulationResult result = nivel0Service.simular(input);
        return ResponseEntity.ok(result);
    }

    @GetMapping ("/nivel-0/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoNivel0() {
        return ResponseEntity.ok(Map.of(
            "nivel", 0, "nombre", "Gramáticas recursivamente enumerables", "modelo", "Máquina de Turing (MT)", "lenguaje", "L = { a^n b^m c^(n*m) | n, m >= 1 }", "alfabeto", new String[]{"a", "b", "c"}, "alfabetoCinta", new String[]{"a", "b", "c", "X", "Y", "Z", "B"}, "estados", new String[]{"q0", "q1", "q2", "q3", "q4", "q5", "q_accept", "garbageCollector"}, "estadoInicial", "q0", "estadosAceptacion", new String[]{"q_accept"}, "ejemplosValidos", new String[]{"abc", "abbcc", "aabcc", "aabbcccc"}, "ejemplosInvalidos", new String[]{"", "ab", "abcc", "aabbcc", "abcba"}
        ));
    }
}
