package edu.chomsky.simulator.api;

import java.util.List;

public record SimulationStep(
        int number,
        String state,
        String readSymbol,
        int headPosition,
        List<String> tape,
        List<String> stack,
        String action) { }
