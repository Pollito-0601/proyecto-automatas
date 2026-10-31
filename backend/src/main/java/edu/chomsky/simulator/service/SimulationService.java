package edu.chomsky.simulator.service;

import edu.chomsky.simulator.api.SimulationResponse;
import edu.chomsky.simulator.api.SimulationStep;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService {
    private static final String BLANK = "□";

    public SimulationResponse simulate(int level, String input) {
        if (input == null) throw new IllegalArgumentException("La entrada no puede ser nula.");
        return switch (level) {
            case 3 -> finiteAutomaton(input);
            case 2 -> pushdownAutomaton(input);
            case 1 -> boundedMachine(input);
            case 0 -> turingAddition(input);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El nivel debe ser 0, 1, 2 o 3.");
        };
    }

    private SimulationResponse finiteAutomaton(String input) {
        var steps = new ArrayList<SimulationStep>();
        String state = "q₀";
        steps.add(step(0, state, input.isEmpty() ? "ε" : String.valueOf(input.charAt(0)), 0, List.of(), List.of(), "Estado inicial; q₀ acepta prefijos de a*."));
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            state = switch (state) {
                case "q₀" -> symbol == 'a' ? "q₀" : symbol == 'b' ? "q₁" : "qᵈ";
                case "q₁" -> symbol == 'b' ? "q₁" : "qᵈ";
                default -> "qᵈ";
            };
            steps.add(step(steps.size(), state, String.valueOf(symbol), i + 1, List.of(), List.of(), "Lee «" + symbol + "» y avanza a " + state + "."));
        }
        boolean accepted = state.equals("q₀") || state.equals("q₁");
        steps.add(step(steps.size(), state, "ε", input.length(), List.of(), List.of(), accepted ? "Fin de entrada en un estado de aceptación." : "Fin de entrada en el estado trampa."));
        return response(3, "Autómata finito determinista", input, accepted, accepted ? "Cadena aceptada" : "Cadena rechazada", "Lenguaje: a*b*. El estado trampa qᵈ conserva el rechazo.", steps);
    }

    private SimulationResponse pushdownAutomaton(String input) {
        var steps = new ArrayList<SimulationStep>();
        var stack = new ArrayList<String>();
        stack.add("Z₀");
        String state = "qₐ";
        steps.add(step(0, state, input.isEmpty() ? "ε" : String.valueOf(input.charAt(0)), 0, List.of(), stack, "Pila inicial con marcador Z₀."));
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            if (state.equals("qₐ") && symbol == 'a') {
                stack.add("A");
            } else if ((state.equals("qₐ") || state.equals("qᵦ")) && symbol == 'b' && stack.size() > 1) {
                stack.remove(stack.size() - 1);
                state = "qᵦ";
            } else {
                state = "qᵣ";
            }
            steps.add(step(steps.size(), state, String.valueOf(symbol), i + 1, List.of(), stack, "Lee «" + symbol + "»; " + (state.equals("qᵣ") ? "no hay transición válida." : "actualiza la pila.")));
        }
        boolean accepted = !state.equals("qᵣ") && stack.size() == 1 && (input.isEmpty() || state.equals("qᵦ"));
        String finalState = accepted ? "qf" : "qᵣ";
        steps.add(step(steps.size(), finalState, "ε", input.length(), List.of(), stack, accepted ? "La entrada terminó y solo queda el marcador Z₀." : "La pila o el orden de símbolos no cumple aⁿbⁿ."));
        return response(2, "Autómata de pila", input, accepted, accepted ? "Cadena aceptada" : "Cadena rechazada", "Lenguaje: aⁿbⁿ con n ≥ 0. Cada a apila A; cada b desapila una A.", steps);
    }

    private SimulationResponse boundedMachine(String input) {
        if (input.length() > 24) throw new IllegalArgumentException("Para conservar una animación legible, el Nivel 1 admite hasta 24 símbolos.");
        var tape = new ArrayList<String>();
        tape.add("⊢");
        input.chars().forEach(c -> tape.add(String.valueOf((char) c)));
        tape.add("⊣");
        var steps = new ArrayList<SimulationStep>();
        int head = 1;
        String state = "q₀";
        steps.add(step(0, state, tape.get(head), head, tape, List.of(), "Cinta acotada por ⊢ y ⊣."));
        boolean validAlphabet = input.chars().allMatch(c -> c == 'a' || c == 'b' || c == 'c');
        if (!validAlphabet) return rejectBounded(input, steps, tape, head, "Símbolo fuera del alfabeto {a,b,c}.");

        int aCount = 0;
        for (int i = 1; i < tape.size() - 1 && tape.get(i).equals("a"); i++) aCount++;
        int bCount = 0;
        int cursor = 1 + aCount;
        while (cursor < tape.size() - 1 && tape.get(cursor).equals("b")) { bCount++; cursor++; }
        int cCount = 0;
        while (cursor < tape.size() - 1 && tape.get(cursor).equals("c")) { cCount++; cursor++; }
        if (aCount + bCount + cCount != input.length()) return rejectBounded(input, steps, tape, Math.min(cursor, tape.size() - 1), "La cadena no tiene el orden a*b*c*.");

        // Cada pasada marca un a, un b y un c; el cabezal solo recorre la cinta delimitada.
        for (int round = 0; round < aCount; round++) {
            head = seekForward(tape, "a", 1, steps);
            if (head < 0) break;
            state = "qₐ"; tape.set(head, "X"); record(steps, state, tape, head, "Marca un a como X.");
            head = seekForward(tape, "b", head + 1, steps);
            if (head < 0) return rejectBounded(input, steps, tape, tape.size() - 1, "Falta un b para emparejar el a marcado.");
            state = "qᵦ"; tape.set(head, "Y"); record(steps, state, tape, head, "Marca un b como Y.");
            head = seekForward(tape, "c", head + 1, steps);
            if (head < 0) return rejectBounded(input, steps, tape, tape.size() - 1, "Falta un c para emparejar el a y b marcados.");
            state = "q𝚌"; tape.set(head, "Z"); record(steps, state, tape, head, "Marca un c como Z.");
            while (head > 1) { head--; state = "q←"; record(steps, state, tape, head, "Regresa hacia el inicio de la cinta."); }
        }
        boolean noUnmarked = tape.stream().noneMatch(cell -> cell.equals("a") || cell.equals("b") || cell.equals("c"));
        boolean accepted = noUnmarked && aCount == bCount && bCount == cCount;
        steps.add(step(steps.size(), accepted ? "qf" : "qᵣ", "ε", head, tape, List.of(), accepted ? "No quedan símbolos sin marcar; acepta." : "Quedaron símbolos sin pareja; rechaza."));
        return response(1, "Máquina linealmente acotada", input, accepted, accepted ? "Cadena aceptada" : "Cadena rechazada", "Lenguaje: aⁿbⁿcⁿ con n ≥ 0. X, Y y Z marcan símbolos ya emparejados.", steps);
    }

    private SimulationResponse rejectBounded(String input, List<SimulationStep> steps, List<String> tape, int head, String reason) {
        steps.add(step(steps.size(), "qᵣ", head >= 0 && head < tape.size() ? tape.get(head) : "ε", head, tape, List.of(), reason));
        return response(1, "Máquina linealmente acotada", input, false, "Cadena rechazada", reason + " La cinta conserva sus delimitadores.", steps);
    }

    private int seekForward(List<String> tape, String symbol, int from, List<SimulationStep> steps) {
        for (int i = from; i < tape.size(); i++) {
            record(steps, "q→", tape, i, "Avanza por la cinta buscando «" + symbol + "».");
            if (i < tape.size() - 1 && tape.get(i).equals(symbol)) return i;
        }
        return -1;
    }

    private SimulationResponse turingAddition(String input) {
        if (input.length() > 42) throw new IllegalArgumentException("La suma unaria admite hasta 20 marcas por operando.");
        if (!input.matches("1+\\+1+")) throw new IllegalArgumentException("Usa dos operandos unarios no vacíos separados por +; ejemplo: 11+111.");
        var tape = new ArrayList<String>();
        tape.add(BLANK);
        input.chars().forEach(c -> tape.add(String.valueOf((char) c)));
        tape.add(BLANK);
        var steps = new ArrayList<SimulationStep>();
        int head = input.indexOf('+') + 1;
        String state = "q₀";
        record(steps, state, tape, head, "Configuración inicial; localiza el separador +.");
        tape.set(head, "1");
        state = "qᵣ";
        record(steps, state, tape, head, "Escribe 1 sobre + para unir los dos bloques.");
        head++;
        while (head < tape.size() - 1 && tape.get(head).equals("1")) {
            state = "qᵣ"; record(steps, state, tape, head, "Avanza hasta el extremo derecho del bloque."); head++;
        }
        head--;
        tape.set(head, BLANK);
        state = "qf";
        record(steps, state, tape, head, "Borra la última marca; la cinta contiene la suma.");
        String result = String.join("", tape).replace(BLANK, "");
        return response(0, "Máquina de Turing: suma unaria", input, true, result,
                "Resultado: " + result + " (" + result.length() + " unidades). Alfabeto de cinta: {1, +, □}.", steps);
    }

    private void record(List<SimulationStep> steps, String state, List<String> tape, int head, String action) {
        String read = head >= 0 && head < tape.size() ? tape.get(head) : BLANK;
        steps.add(step(steps.size(), state, read, head, tape, List.of(), action));
    }

    private void record(ArrayList<SimulationStep> steps, String state, List<String> tape, int head, String action) {
        record((List<SimulationStep>) steps, state, tape, head, action);
    }

    private SimulationStep step(int number, String state, String read, int head, List<String> tape, List<String> stack, String action) {
        return new SimulationStep(number, state, read, head, List.copyOf(tape), List.copyOf(stack), action);
    }

    private SimulationResponse response(int level, String title, String input, boolean accepted, String result, String message, List<SimulationStep> steps) {
        return new SimulationResponse(level, title, input, accepted, result, message, List.copyOf(steps));
    }
}
