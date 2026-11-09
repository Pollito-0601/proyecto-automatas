package pro.chomskysimulator.controller;

import java.util.Map;

import pro.chomskysimulator.service.Nivel3AutomataService;
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

    public SimuladorController(Nivel3AutomataService nivel3Service) {
        this.nivel3Service = nivel3Service;
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
            "nivel", 3, "nombre", "Gramáticas regulares", "modelo", "Autómata finito determinista (AFD)", "lenguaje", "L = { a^n b^m |n, m >= 1 }", "alfabeto", new String[]{"a", "b"}, "estados", new String[]{"q0", "q1", "q2", "garbageCollector"}, "estadoInicial", "q0", "estadosAceptacion", new String[]{"q2"}, "ejemplosValidos", new String[]{"ab", "aab", "abbb", "aaabbb"}, "ejemplosInvalidos", new String[]{"", "a", "b", "ba", "aabba"}
        ));
    }
}
