package pro.chomskysimulator.dto;

import java.util.List;

public record SimulationResult (String inputString, boolean accepted, String finalState, String message, List<SimulationStep> steps) {
    
}
