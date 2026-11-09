package pro.chomskysimulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import pro.chomskysimulator.dto.SimulationResult;
import pro.chomskysimulator.dto.SimulationStep;

import org.springframework.stereotype.Service;

@Service
public class Nivel3AutomataService {
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
        int contadorPasos = 0;
        pasos.add(new SimulationStep(contadorPasos++, estadoActual,
                null, 
                -1, 
                estadoActual, 
                Collections.emptyList(), 
                "Inicio: El autómata está en estado inicial q0 esperando al menos una 'a'", 
                false
        ));

        for (int i = 0; i < input.length(); i++) {
            char simbolo = input.charAt(i);
            String estadoAnterior = estadoActual;
            String siguienteEstado;
            String descripcion;

            switch(estadoActual) {
                case Q0 -> {
                    if (simbolo == 'a') {
                        siguienteEstado = Q1;
                        descripcion = String.format("Paso %d: Se lee '%c'. Primera 'a' encontrada, se pasa al estado q1", i + 1, simbolo);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: Se lee '%c'. Como la cadena debe iniciar 'a', se pasa al estado de garbageCollector", i + 1, simbolo);
                    }
                }
                case Q1 -> {
                    if (simbolo == 'a') {
                        siguienteEstado = Q1;
                        descripcion = String.format("Paso %d: Se lee '%c'. Se mantiene en el estado q1, esperando una 'b'", i + 1, simbolo);
                    } else if (simbolo == 'b') {
                        siguienteEstado = Q2;
                        descripcion = String.format("Paso %d: Se lee '%c'. Primera 'b' encontrada, se pasa al estado q2", i + 1, simbolo);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: Se lee '%c'. Como la cadena debe tener solo 'a' y 'b', se pasa al estado de garbageCollector", i + 1, simbolo);
                    }
                }
                case Q2 -> {
                    if (simbolo == 'b') {
                        siguienteEstado = Q2;
                        descripcion = String.format("Paso %d: Se lee '%c'. Se mantiene en el estado q2, esperando una 'c'", i + 1, simbolo);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: Se lee '%c'. Como la cadena debe terminar con solo 'b', se pasa al estado de garbageCollector", i + 1, simbolo);
                    }
                }
                default -> {
                    siguienteEstado = GC;
                    descripcion = String.format("Paso %d: Se lee '%c'. El autómata está en estado de garbageCollector, por lo que se mantiene en este estado", i + 1, simbolo);
                }
            }

            boolean esAceptado = Q2.equals(siguienteEstado);
            pasos.add(new SimulationStep(
                contadorPasos++, 
                estadoAnterior, 
                simbolo, 
                i, 
                siguienteEstado, 
                Collections.emptyList(), 
                descripcion, 
                esAceptado
            ));
            estadoActual = siguienteEstado;
        }

        boolean aceptado = Q2.equals(estadoActual);
        String mensaje;

        if (input.isEmpty()) {
            mensaje = "La cadena está vacía, por lo tanto es rechazada: se requiere al menos una 'a' y al menos una 'b' para ser aceptada.";
        } else if (aceptado) {
            mensaje = String.format("La cadena \"%s\" es ACEPTADA. Cumple con el formato a^n b^m (n, m >=1) y termina en q2", input);
        } else {
            mensaje = String.format("La cadena \"%s\" es RECHAZADA. Finalizó en el estado %s (no es de aceptación)", input, estadoActual);
        }

        return new SimulationResult(input, aceptado, estadoActual, mensaje, pasos);
    }
}
