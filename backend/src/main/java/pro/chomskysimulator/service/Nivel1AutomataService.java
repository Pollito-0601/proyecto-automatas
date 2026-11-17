package pro.chomskysimulator.service;

import java.util.ArrayList;
import java.util.List;

import pro.chomskysimulator.dto.SimulationResult;
import pro.chomskysimulator.dto.SimulationStep;

import org.springframework.stereotype.Service;

@Service
public class Nivel1AutomataService {
    public static final String Q0 = "q0";
    public static final String Q1 = "q1";
    public static final String Q2 = "q2";
    public static final String Q3 = "q3";
    public static final String Q4 = "q4";
    public static final String Q_ACCEPT = "q_accept";
    public static final String GC = "garbageCollector";

    private static final int MAX_PASOS = 2000;

    public SimulationResult simular(String input) {
        List<SimulationStep> pasos = new ArrayList<>();
        if (input == null) {
            input = "";
        }

        // Validación inicial básica
        if (input.isEmpty()) {
            pasos.add(new SimulationStep(0, Q0, null, 0, GC, List.of("[", "]"), "Inicio: Cadena vacía rechazada. Se requiere a^n b^n c^n con n >= 1.", false));
            return new SimulationResult(input, false, GC, "La cadena está vacía, por lo tanto es rechazada: se requiere al menos a^1 b^1 c^1 (n >= 1).", pasos);
        }

        // El alfabeto de entrada es {a, b, c}; X, Y, Z y los delimitadores son solo de la cinta
        if (!input.matches("[abc]+")) {
            String mensajeAlfabeto = "La cadena contiene símbolos fuera del alfabeto de entrada {a, b, c}, por lo tanto es rechazada.";
            pasos.add(new SimulationStep(0, Q0, null, 0, GC, List.of("[", "]"), "Inicio: " + mensajeAlfabeto, false));
            return new SimulationResult(input, false, GC, mensajeAlfabeto, pasos);
        }

        // Construcción de la cinta acotada: [ + input + ]
        List<Character> cinta = new ArrayList<>();
        cinta.add('[');
        for (char c : input.toCharArray()) {
            cinta.add(c);
        }
        cinta.add(']');

        int cabezal = 1; // Comienza en el primer símbolo de la entrada
        String estadoActual = Q0;
        int contadorPasos = 0;

        List<String> snapshotCinta = cinta.stream().map(String::valueOf).toList();
        pasos.add(new SimulationStep(contadorPasos++, estadoActual, cinta.get(cabezal), cabezal, estadoActual, new ArrayList<>(snapshotCinta), "Inicio: Autómata linealmente acotado en q0 sobre la cinta acotada entre delimitadores '[' y ']'.", false));

        boolean terminado = false;
        while (!terminado && contadorPasos < MAX_PASOS) {
            char simboloLeido = cinta.get(cabezal);
            String estadoAnterior = estadoActual;
            String siguienteEstado = estadoActual;
            String descripcion;
            int direccion = 0; // -1: izquierda, 0: quieto, 1: derecha

            switch (estadoActual) {
                case Q0 -> {
                    if (simboloLeido == 'a') {
                        cinta.set(cabezal, 'X');
                        siguienteEstado = Q1;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q0 lee 'a'. Marca con 'X' y se desplaza a la DERECHA hacia q1", contadorPasos);
                    } else if (simboloLeido == 'Y') {
                        siguienteEstado = Q4;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q0 lee 'Y'. Todas las 'a' han sido marcadas, inicia fase de verificación en q4", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q0 lee '%c'. Símbolo inesperado, pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q1 -> {
                    if (simboloLeido == 'a' || simboloLeido == 'Y') {
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q1 lee '%c'. Salta a la DERECHA buscando 'b'", contadorPasos, simboloLeido);
                    } else if (simboloLeido == 'b') {
                        cinta.set(cabezal, 'Y');
                        siguienteEstado = Q2;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q1 lee 'b'. Marca con 'Y' y se desplaza a la DERECHA hacia q2", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q1 lee '%c'. No se encontró 'b' correspondiente, pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q2 -> {
                    if (simboloLeido == 'b' || simboloLeido == 'Z') {
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q2 lee '%c'. Salta a la DERECHA buscando 'c'", contadorPasos, simboloLeido);
                    } else if (simboloLeido == 'c') {
                        cinta.set(cabezal, 'Z');
                        siguienteEstado = Q3;
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q2 lee 'c'. Marca con 'Z' y retrocede a la IZQUIERDA hacia q3", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q2 lee '%c'. No se encontró 'c' correspondiente, pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q3 -> {
                    if (simboloLeido == 'a' || simboloLeido == 'b' || simboloLeido == 'Y' || simboloLeido == 'Z') {
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q3 retrocede a la IZQUIERDA sobre '%c' buscando la última 'X'", contadorPasos, simboloLeido);
                    } else if (simboloLeido == 'X') {
                        siguienteEstado = Q0;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q3 encuentra 'X'. Se mueve un paso a la DERECHA y vuelve a q0 para nuevo ciclo", contadorPasos);
                    } else if (simboloLeido == '[') {
                        siguienteEstado = Q0;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q3 llega al delimitador '['. Se mueve a la DERECHA y vuelve a q0", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q3 lee '%c' inesperado. Pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q4 -> {
                    if (simboloLeido == 'Y' || simboloLeido == 'Z') {
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q4 verificando símbolos marcados '%c'. Avanza a la DERECHA", contadorPasos, simboloLeido);
                    } else if (simboloLeido == ']') {
                        siguienteEstado = Q_ACCEPT;
                        direccion = 0;
                        descripcion = String.format("Paso %d: En q4 alcanza el delimitador final ']'. Todos los símbolos fueron balanceados, pasa a q_accept", contadorPasos);
                        terminado = true;
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q4 se encontró símbolo no marcado '%c'. La cadena está desbalanceada, pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                default -> {
                    siguienteEstado = GC;
                    descripcion = String.format("Paso %d: El autómata permanece en garbageCollector", contadorPasos);
                    terminado = true;
                }
            }

            cabezal += direccion;
            if (cabezal < 0 || cabezal >= cinta.size()) {
                siguienteEstado = GC;
                descripcion = String.format("Paso %d: Desborde del cabezal fuera de los límites de la cinta acotada. Pasa a garbageCollector", contadorPasos);
                terminado = true;
            }

            estadoActual = siguienteEstado;
            boolean esAceptado = Q_ACCEPT.equals(estadoActual);
            List<String> snapshot = cinta.stream().map(String::valueOf).toList();
            pasos.add(new SimulationStep(contadorPasos++, estadoAnterior, simboloLeido, cabezal, siguienteEstado, snapshot, descripcion, esAceptado));

            if (esAceptado) {
                terminado = true;
            }
        }

        boolean aceptado = Q_ACCEPT.equals(estadoActual);
        String mensaje;
        if (aceptado) {
            mensaje = String.format("La cadena \"%s\" es ACEPTADA por el LBA. Cumple con el lenguaje sensible al contexto a^n b^n c^n (n >= 1).", input);
        } else {
            mensaje = String.format("La cadena \"%s\" es RECHAZADA por el LBA. Finalizó en el estado %s sin balancear a^n b^n c^n dentro de los límites.", input, estadoActual);
        }

        return new SimulationResult(input, aceptado, estadoActual, mensaje, pasos);
    }
}