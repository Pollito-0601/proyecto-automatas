package pro.chomskysimulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import pro.chomskysimulator.dto.SimulationResult;
import pro.chomskysimulator.dto.SimulationStep;

import org.springframework.stereotype.Service;

@Service
public class Nivel2AutomataService {
    public static final String Q0 = "q0";
    public static final String Q1 = "q1";
    public static final String Q2 = "q2";
    public static final String GC = "garbageCollector";

    public SimulationResult simular(String input) {
        List<SimulationStep> pasos = new ArrayList<>();
        if (input == null) {
            input = "";
        }

        String estadoActual = Q0;
        Stack<Character> pila = new Stack<>();
        int contadorPasos = 0;
        pasos.add(new SimulationStep(contadorPasos++, estadoActual, null, -1, estadoActual, new ArrayList<>(), "Inicio: El autómata está en estado inicial q0 con la pila vacía esperando al menos una 'a'", false));

        for (int i = 0; i < input.length(); i++) {
            char simbolo = input.charAt(i);
            String estadoAnterior = estadoActual;
            String siguienteEstado;
            String descripcion;

            switch (estadoActual) {
                case Q0 -> {
                    if (simbolo == 'a') {
                        siguienteEstado = Q1;
                        pila.push('A');
                        descripcion = String.format("Paso %d: ( %c | ε, ↓, A ) - Primera 'a' leída, apila 'A' y transiciona a q1", i + 1, simbolo);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: ( %c | ε, -, - ) - Como la cadena debe iniciar con 'a', pasa a garbageCollector", i + 1, simbolo);
                    }
                }
                case Q1 -> {
                    if (simbolo == 'a') {
                        siguienteEstado = Q1;
                        pila.push('A');
                        descripcion = String.format("Paso %d: ( %c | A, ↓, A ) - Lee 'a', apila 'A' en la cima y permanece en q1", i + 1, simbolo);
                    } else if (simbolo == 'b') {
                        if (!pila.isEmpty()) {
                            pila.pop();
                            siguienteEstado = Q2;
                            descripcion = String.format("Paso %d: ( %c | A, ↑, A ) - Primera 'b' leída, desapila 'A' y transiciona a q2", i + 1, simbolo);
                        } else {
                            siguienteEstado = GC;
                            descripcion = String.format("Paso %d: ( %c | ε, -, - ) - Error: Pila vacía sin 'A' para desapilar, pasa a garbageCollector", i + 1, simbolo);
                        }
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: ( %c | -, -, - ) - Símbolo inválido, pasa a garbageCollector", i + 1, simbolo);
                    }
                }
                case Q2 -> {
                    if (simbolo == 'b') {
                        if (!pila.isEmpty()) {
                            pila.pop();
                            siguienteEstado = Q2;
                            descripcion = String.format("Paso %d: ( %c | A, ↑, A ) - Lee 'b', desapila 'A' y permanece en q2", i + 1, simbolo);
                        } else {
                            siguienteEstado = GC;
                            descripcion = String.format("Paso %d: ( %c | ε, -, - ) - Recibe 'b' pero la pila ya está vacía (más 'b's que 'a's), pasa a garbageCollector", i + 1, simbolo);
                        }
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: ( %c | -, -, - ) - No se permiten símbolos tras las 'b', pasa a garbageCollector", i + 1, simbolo);
                    }
                }
                default -> {
                    siguienteEstado = GC;
                    descripcion = String.format("Paso %d: ( %c | -, -, - ) - El autómata permanece en garbageCollector", i + 1, simbolo);
                }
            }

            List<String> estadoPila = pila.stream().map(String::valueOf).toList();
            boolean esPasoFinalAceptado = (i == input.length() - 1) && Q2.equals(siguienteEstado) && pila.isEmpty();

            pasos.add(new SimulationStep(contadorPasos++, estadoAnterior, simbolo, i, siguienteEstado, estadoPila, descripcion, esPasoFinalAceptado));
            estadoActual = siguienteEstado;
        }

        boolean aceptado = Q2.equals(estadoActual) && pila.isEmpty() && !input.isEmpty();
        String mensaje;

        if (input.isEmpty()) {
            mensaje = "La cadena está vacía, por lo tanto es rechazada: se requiere al menos una 'a' y una 'b' con la misma cantidad (n >= 1).";
        } else if (aceptado) {
            mensaje = String.format("La cadena \"%s\" es ACEPTADA. Cumple con el formato a^n b^n (n >= 1) con pila balanceada y termina en q2.", input);
        } else if (Q2.equals(estadoActual) && !pila.isEmpty()) {
            mensaje = String.format("La cadena \"%s\" es RECHAZADA. Finalizó la lectura pero la pila contiene %d elemento(s) no balanceados (más 'a's que 'b's).", input, pila.size());
        } else {
            mensaje = String.format("La cadena \"%s\" es RECHAZADA. Finalizó en el estado %s (no es de aceptación o la pila no quedó vacía).", input, estadoActual);
        }

        return new SimulationResult(input, aceptado, estadoActual, mensaje, pasos);
    }
}
