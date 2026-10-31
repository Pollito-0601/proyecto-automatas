package edu.chomsky.simulator.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationServiceTest {
    private final SimulationService service = new SimulationService();

    @Test void finiteAutomatonAcceptsAndRejects() {
        assertTrue(service.simulate(3, "aaabbb").accepted());
        assertTrue(service.simulate(3, "").accepted());
        assertFalse(service.simulate(3, "abba").accepted());
    }

    @Test void pushdownAutomatonBalancesTheTwoBlocks() {
        assertTrue(service.simulate(2, "aabb").accepted());
        assertTrue(service.simulate(2, "").accepted());
        assertFalse(service.simulate(2, "aab").accepted());
        assertFalse(service.simulate(2, "abab").accepted());
    }

    @Test void boundedMachineMatchesThreeBlocks() {
        assertTrue(service.simulate(1, "aabbcc").accepted());
        assertTrue(service.simulate(1, "").accepted());
        assertFalse(service.simulate(1, "aabcc").accepted());
        assertFalse(service.simulate(1, "abcx").accepted());
    }

    @Test void turingMachineAddsUnaryNumbersAndRejectsMalformedInput() {
        var sum = service.simulate(0, "11+111");
        assertTrue(sum.accepted());
        assertEquals("11111", sum.result());
        assertTrue(sum.steps().size() > 1);
        assertThrows(IllegalArgumentException.class, () -> service.simulate(0, "1+"));
    }
}
