package edu.chomsky.simulator.api;

import jakarta.validation.constraints.NotNull;

public record SimulationRequest(@NotNull String input) { }
