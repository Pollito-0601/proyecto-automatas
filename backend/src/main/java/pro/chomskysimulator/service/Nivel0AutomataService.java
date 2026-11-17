package pro.chomskysimulator.service;

import java.util.ArrayList;
import java.util.List;

import pro.chomskysimulator.dto.SimulationResult;
import pro.chomskysimulator.dto.SimulationStep;

import org.springframework.stereotype.Service;

@Service
public class Nivel0AutomataService {
    public static final String Q0 = "q0";
    public static final String Q1 = "q1";
    public static final String Q2 = "q2";
    public static final String Q3 = "q3";
    public static final String Q4 = "q4";
    public static final String Q5 = "q5";
    public static final String Q_ACCEPT = "q_accept";
    public static final String GC = "garbageCollector";

    private static final int MAX_PASOS = 5000;
    private static final char BLANCO = 'B';

    public SimulationResult simular(String input) {
        List<SimulationStep> pasos = new ArrayList<>();
        if (input == null) {
            input = "";
        }

        if (input.isEmpty()) {
            pasos.add(new SimulationStep(0, Q0, null, 0, GC, List.of("B"), "Inicio: Cadena vacía rechazada por la Máquina de Turing. Se requiere a^n b^m c^(n*m).", false));
            return new SimulationResult(input, false, GC, "La cadena está vacía, por lo tanto es rechazada: se requiere al menos a^1 b^1 c^1 (n, m >= 1).", pasos);
        }

        // Estructura de la cinta dinámica con margen de blancos
        List<Character> cinta = new ArrayList<>();
        cinta.add(BLANCO);
        for (char c : input.toCharArray()) {
            cinta.add(c);
        }
        for (int b = 0; b < 10; b++) {
            cinta.add(BLANCO);
        }

        int cabezal = 1; // Inicia en el primer carácter real de la entrada
        String estadoActual = Q0;
        int contadorPasos = 0;

        List<String> snapshotInicial = cinta.stream().map(String::valueOf).toList();
        pasos.add(new SimulationStep(contadorPasos++, estadoActual, cinta.get(cabezal), cabezal, estadoActual, new ArrayList<>(snapshotInicial), "Inicio: Máquina de Turing en q0 sobre la cinta extendible con blancos 'B'.", false));

        boolean terminado = false;
        while (!terminado && contadorPasos < MAX_PASOS) {
            // Expandir cinta a la derecha si el cabezal se aproxima al final
            while (cabezal >= cinta.size() - 2) {
                cinta.add(BLANCO);
            }
            // Expandir cinta a la izquierda si el cabezal llega a 0
            if (cabezal <= 0) {
                cinta.add(0, BLANCO);
                cabezal = 1;
            }

            char simboloLeido = cinta.get(cabezal);
            String estadoAnterior = estadoActual;
            String siguienteEstado = estadoActual;
            String descripcion;
            int direccion = 0; // -1: L, 0: S, 1: R

            switch (estadoActual) {
                case Q0 -> {
                    if (simboloLeido == 'a') {
                        cinta.set(cabezal, 'X');
                        siguienteEstado = Q1;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q0 lee 'a'. Marca con 'X' y avanza (R) a q1 para emparejar con las 'b'", contadorPasos);
                    } else if (simboloLeido == 'Y' || simboloLeido == 'b') {
                        siguienteEstado = Q5;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q0 lee '%c'. Todas las 'a' han sido multiplicadas, pasa (R) a q5 para verificar las 'c'", contadorPasos, simboloLeido);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q0 lee '%c'. Símbolo no esperado, pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q1 -> {
                    if (simboloLeido == 'a' || simboloLeido == 'Y') {
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q1 salta '%c' a la derecha (R) buscando 'b'", contadorPasos, simboloLeido);
                    } else if (simboloLeido == 'b') {
                        cinta.set(cabezal, 'Y');
                        siguienteEstado = Q2;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q1 lee 'b'. Marca con 'Y' y avanza (R) a q2 para marcar una 'c'", contadorPasos);
                    } else if (simboloLeido == 'Z' || simboloLeido == BLANCO) {
                        siguienteEstado = Q4;
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q1 se agotaron las 'b' para esta 'a'. Retrocede (L) a q4 para restaurar las 'b'", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q1 lee '%c' inesperado. Pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q2 -> {
                    if (simboloLeido == 'b' || simboloLeido == 'Z') {
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q2 salta '%c' a la derecha (R) buscando 'c'", contadorPasos, simboloLeido);
                    } else if (simboloLeido == 'c') {
                        cinta.set(cabezal, 'Z');
                        siguienteEstado = Q3;
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q2 lee 'c'. Marca con 'Z' producto exitoso y retrocede (L) a q3", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q2 no hay suficiente 'c' para el producto (n*m). Pasa a garbageCollector", contadorPasos);
                        terminado = true;
                    }
                }
                case Q3 -> {
                    if (simboloLeido == 'Z' || simboloLeido == 'b') {
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q3 retrocede (L) sobre '%c' buscando la 'Y' marcada", contadorPasos, simboloLeido);
                    } else if (simboloLeido == 'Y') {
                        siguienteEstado = Q1;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q3 encuentra 'Y'. Avanza (R) a q1 para buscar la siguiente 'b'", contadorPasos);
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q3 lee '%c' inesperado. Pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                case Q4 -> {
                    if (simboloLeido == 'Z') {
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q4 retrocede (L) sobre 'Z'", contadorPasos);
                    } else if (simboloLeido == 'Y') {
                        cinta.set(cabezal, 'b');
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q4 restaura 'Y' a 'b' y sigue retrocediendo (L)", contadorPasos);
                    } else if (simboloLeido == 'X') {
                        siguienteEstado = Q0;
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q4 encuentra 'X'. Ciclo de 'a' completado, avanza (R) a q0 para siguiente 'a'", contadorPasos);
                    } else {
                        direccion = -1;
                        descripcion = String.format("Paso %d: En q4 retrocediendo sobre '%c'", contadorPasos, simboloLeido);
                    }
                }
                case Q5 -> {
                    if (simboloLeido == 'Y' || simboloLeido == 'b' || simboloLeido == 'Z') {
                        direccion = 1;
                        descripcion = String.format("Paso %d: En q5 verificando símbolos marcados '%c' hacia la derecha (R)", contadorPasos, simboloLeido);
                    } else if (simboloLeido == BLANCO) {
                        siguienteEstado = Q_ACCEPT;
                        direccion = 0;
                        descripcion = String.format("Paso %d: En q5 llega al blanco 'B'. La cantidad de 'c' coincide exactamente con n*m. Pasa a q_accept", contadorPasos);
                        terminado = true;
                    } else {
                        siguienteEstado = GC;
                        descripcion = String.format("Paso %d: En q5 se encontró '%c' no balanceado (sobran símbolos 'c'). Pasa a garbageCollector", contadorPasos, simboloLeido);
                        terminado = true;
                    }
                }
                default -> {
                    siguienteEstado = GC;
                    descripcion = String.format("Paso %d: La Máquina de Turing permanece en garbageCollector", contadorPasos);
                    terminado = true;
                }
            }

            cabezal += direccion;
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
            mensaje = String.format("La cadena \"%s\" es ACEPTADA por la Máquina de Turing. Cumple con el lenguaje L = { a^n b^m c^(n*m) | n, m >= 1 }.", input);
        } else {
            mensaje = String.format("La cadena \"%s\" es RECHAZADA por la Máquina de Turing. Finalizó en el estado %s sin satisfacer el producto n * m.", input, estadoActual);
        }

        return new SimulationResult(input, aceptado, estadoActual, mensaje, pasos);
    }
}
