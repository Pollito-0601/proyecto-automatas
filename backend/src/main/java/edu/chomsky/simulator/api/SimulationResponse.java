package edu.chomsky.simulator.api;

import java.util.List;

public record SimulationResponse(
        int level,
        String title,
        String input,
        boolean accepted,
        String result,
        String message,
        List<SimulationStep> steps) { }
