package edu.chomsky.simulator.api;

import edu.chomsky.simulator.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulations")
@CrossOrigin(origins = {"http://localhost:4321", "http://127.0.0.1:4321"})
public class SimulationController {
    private final SimulationService simulations;

    public SimulationController(SimulationService simulations) {
        this.simulations = simulations;
    }

    @PostMapping("/{level}")
    public SimulationResponse simulate(@PathVariable int level, @Valid @RequestBody SimulationRequest request) {
        return simulations.simulate(level, request.input());
    }
}
