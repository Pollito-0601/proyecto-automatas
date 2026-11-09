package pro.chomskysimulator.dto;

import java.util.List;

public record SimulationStep (int stepNumber, String currentState, Character symbolReaded, int stringIndex, String nextState, List<String> memory, String description, boolean isFinalStep) {
    
}
