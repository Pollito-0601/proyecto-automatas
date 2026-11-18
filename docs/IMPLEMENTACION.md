# Documento de implementación y conceptos aplicados

Proyecto: Simulador interactivo de la jerarquía de Chomsky

Tecnologías: Java 25, Spring Boot, Astro (HTML, CSS, JavaScript)

## 1. Introducción

Este proyecto implementa un simulador gráfico que muestra, paso a paso, cómo reconoce cadenas el modelo computacional asociado a cada nivel de la jerarquía de Chomsky. El objetivo no es solo decir si una cadena pertenece o no a un lenguaje, sino hacer visible el mecanismo: qué estado ocupa el autómata, qué símbolo lee, qué hay en la memoria (pila o cinta) y por qué se acepta o se rechaza.

El documento está organizado así: primero los conceptos teóricos (sección 2), luego la arquitectura general (sección 3), el detalle de cada autómata (sección 4), cómo se construye la visualización (sección 5), las pruebas (sección 6) y las limitaciones conocidas (sección 7).

## 2. Conceptos aplicados

### 2.1 La jerarquía de Chomsky

Noam Chomsky clasificó las gramáticas formales en cuatro niveles según el tipo de reglas de producción que permiten. Cada nivel contiene al anterior (todo lenguaje regular es libre de contexto, y así sucesivamente), y a cada uno le corresponde un modelo de máquina cuya memoria define el poder de cómputo:

| Nivel | Gramática | Lenguajes | Modelo de máquina | Memoria |
| --- | --- | --- | --- | --- |
| 3 | Regular | Regulares | Autómata finito | Ninguna (solo el estado) |
| 2 | Libre de contexto | Libres de contexto | Autómata de pila | Pila (LIFO) |
| 1 | Sensible al contexto | Sensibles al contexto | Autómata linealmente acotado | Cinta de tamaño limitado a la entrada |
| 0 | Sin restricciones | Recursivamente enumerables | Máquina de Turing | Cinta infinita |

La idea que recorre todo el proyecto es esta: más memoria permite reconocer lenguajes más complejos.

### 2.2 Lenguaje elegido para cada nivel

Para cada nivel se eligió un lenguaje que es un ejemplo clásico de lo que ese modelo puede hacer y el anterior no:

| Nivel | Lenguaje | Por qué ilustra el nivel |
| --- | --- | --- |
| 3 | $a^n b^m$ con $n,m \ge 1$ | Solo exige un orden (primero `a`, luego `b`). Recordarlo cabe en un número finito de estados. |
| 2 | $a^n b^n$ con $n \ge 1$ | Exige contar: igualar `a` y `b`. Un autómata finito no puede, porque $n$ no tiene cota; la pila actúa como contador. |
| 1 | $a^n b^n c^n$ con $n \ge 1$ | Exige igualar tres cantidades. Una sola pila no alcanza (es el ejemplo clásico de lenguaje no libre de contexto); hace falta una memoria que se pueda recorrer en ambos sentidos. |
| 0 | $a^n b^m c^{n \cdot m}$ con $n,m \ge 1$ | Exige una operación aritmética (multiplicar) sobre la cinta, con marcas, retrocesos y restauración de símbolos. |

### 2.3 Determinismo y estado de rechazo

Los cuatro autómatas son deterministas: para cada combinación de estado y símbolo hay a lo sumo una transición. Para que el comportamiento sea completo, toda transición no definida lleva a un estado trampa llamado `garbageCollector`, del que no se sale. Así siempre hay una trayectoria clara que explicar al usuario cuando la cadena se rechaza.

### 2.4 Condición de aceptación

- AFD (nivel 3): se acepta si al terminar la cadena el estado es de aceptación (`q2`).
- Autómata de pila (nivel 2): se acepta por estado final y pila vacía: el estado es `q2` y todas las `a` apiladas fueron desapiladas.
- LBA y Máquina de Turing (niveles 1 y 0): se acepta al alcanzar el estado `q_accept`.

## 3. Arquitectura

El proyecto se divide en dos aplicaciones que se comunican por HTTP:

```text
┌─────────────────────┐   POST /api/v1/simulador/nivel-X   ┌──────────────────────────┐
│  Frontend (Astro)   │ ─────────────────────────────────▶ │   Backend (Spring Boot)  │
│  localhost:4321     │ ◀───────────────────────────────── │   localhost:8080         │
│  Muestra y anima    │      SimulationResult (JSON)       │   Ejecuta el autómata    │
└─────────────────────┘                                    └──────────────────────────┘
```

El backend calcula, el frontend reproduce. Cuando el usuario pulsa *Iniciar*, el frontend envía la cadena una sola vez. El backend ejecuta el autómata completo y devuelve la lista entera de pasos de esa ejecución. Después, el frontend solo recorre esa lista: avanzar, retroceder, pausar o cambiar la velocidad no requiere nuevas peticiones. Esto simplifica mucho los botones *Anterior* y *Siguiente*, porque retroceder es simplemente volver a un paso ya calculado.

### 3.1 Backend

Paquete `pro.chomskysimulator`:

- `controller/SimuladorController`: expone dos tipos de endpoints por nivel. Un `POST /api/v1/simulador/nivel-X` que recibe la cadena y devuelve la simulación, y un `GET /api/v1/simulador/nivel-X/info` con los datos del modelo (alfabeto, estados, estado inicial, estados de aceptación y ejemplos).
- `service/Nivel0..3AutomataService`: un servicio por nivel, con toda la lógica del autómata en el método `simular(String input)`.
- `dto/`: los tres registros (`record`) que viajan en la API.
- `config/WebConfig`: habilita CORS para que el frontend (en otro puerto) pueda llamar a la API.

El contrato de datos es común a los cuatro niveles:

```java
record SimulationRequest(String input)

record SimulationResult(
    String inputString,        // cadena original
    boolean accepted,          // veredicto
    String finalState,         // estado en el que terminó
    String message,            // explicación del resultado
    List<SimulationStep> steps // traza completa
)

record SimulationStep(
    int stepNumber,
    String currentState,       // estado antes de la transición
    Character symbolReaded,    // símbolo leído (null en el paso inicial)
    int stringIndex,           // posición en la cadena o del cabezal
    String nextState,          // estado después de la transición
    List<String> memory,       // contenido de la memoria (ver abajo)
    String description,        // texto explicativo del paso
    boolean isFinalStep
)
```

El campo `memory` es lo que hace flexible al contrato. Cada nivel lo usa según su tipo de memoria:

| Nivel | Contenido de `memory` |
| --- | --- |
| 3 | Lista vacía (el AFD no tiene memoria) |
| 2 | La pila, de fondo a cima, después de la transición |
| 1 | La cinta completa, incluidos `[` y `]` |
| 0 | La cinta completa, incluidos los blancos `B` |

Gracias a esto, el frontend de cada nivel recibe siempre la misma estructura y solo cambia cómo dibuja `memory`.

### 3.2 Frontend

Está hecho en Astro, con una página por nivel y un panel principal:

| Ruta | Archivo | Contenido |
| --- | --- | --- |
| `/` | `index.astro` | Panel principal con las cuatro tarjetas |
| `/tipo3-afd` | `tipo3-afd.astro` | Simulador del AFD |
| `/tipo2-pda` | `tipo2-pda.astro` | Simulador del autómata de pila |
| `/tipo1-lba` | `tipo1-lba.astro` | Simulador del LBA |
| `/tipo0-mt` | `tipo0-mt.astro` | Simulador de la Máquina de Turing |

`Layout.astro` aporta lo común: el menú lateral de navegación, las fuentes y los estilos base. Cada página contiene su HTML, su CSS y el JavaScript que controla la simulación.

## 4. Los autómatas

En todas las tablas, `R` significa mover el cabezal a la derecha y `L` a la izquierda. Cualquier combinación no listada lleva a `garbageCollector` (la única excepción es `q4` de la Máquina de Turing, que sigue retrocediendo).

### 4.1 Nivel 3: AFD para $a^n b^m$

Autómata $M = (Q, \Sigma, \delta, q_0, F)$ con $Q = \{q_0, q_1, q_2, \text{garbageCollector}\}$, $\Sigma = \{a, b\}$, $F = \{q_2\}$.

| Estado | lee `a` | lee `b` |
| --- | --- | --- |
| `q0` (inicial) | `q1` | trampa |
| `q1` | `q1` | `q2` |
| `q2` (aceptación) | trampa | `q2` |

`q0` espera la primera `a`; `q1` acumula `a`; `q2` acumula `b`. Una `a` después de una `b` es un error, y también lo es terminar la cadena en `q0` o `q1` (por ejemplo `a`, o la cadena vacía).

### 4.2 Nivel 2: autómata de pila para $a^n b^n$

Usa el alfabeto de pila $\{A\}$. Cada `a` apila una `A` y cada `b` desapila una; la cantidad coincide solo si la pila queda exactamente vacía.

| Estado | lee `a` | lee `b` |
| --- | --- | --- |
| `q0` (inicial) | apila `A` → `q1` | trampa |
| `q1` | apila `A` → `q1` | desapila `A` → `q2` |
| `q2` (aceptación) | trampa | desapila `A` → `q2` |

Si llega una `b` con la pila vacía (más `b` que `a`), se rechaza. Si la cadena termina y la pila aún tiene elementos (más `a` que `b`), también. Solo se acepta con el estado `q2` y la pila vacía.

Ejemplo con `aabb`:

| Paso | Lee | Estado | Acción | Pila (fondo → cima) |
| --- | --- | --- | --- | --- |
| 0 | (inicio) | `q0` | | vacía |
| 1 | `a` | `q0` → `q1` | apila | `A` |
| 2 | `a` | `q1` → `q1` | apila | `A A` |
| 3 | `b` | `q1` → `q2` | desapila | `A` |
| 4 | `b` | `q2` → `q2` | desapila | vacía |

Termina en `q2` con la pila vacía: aceptada. En la interfaz la pila se dibuja como un recipiente vertical donde los elementos aparecen y desaparecen en cada paso.

### 4.3 Nivel 1: LBA para $a^n b^n c^n$

El autómata trabaja sobre una cinta acotada que contiene exactamente la entrada rodeada por dos delimitadores de frontera: `[ a b c ]`. El cabezal nunca puede salir de esa zona; si lo intenta, se rechaza. El alfabeto de cinta es $\{a, b, c, X, Y, Z, [, ]\}$.

Estrategia. En cada ciclo se marca una `a` (como `X`), una `b` (como `Y`) y una `c` (como `Z`), regresando al inicio entre ciclos. Cuando ya no quedan `a` sin marcar, solo debe haber símbolos marcados hasta el delimitador final.

| Estado | Función | Transiciones |
| --- | --- | --- |
| `q0` | Busca la siguiente `a` | `a` → escribe `X`, R, `q1` · `Y` → R, `q4` |
| `q1` | Busca una `b` | `a`, `Y` → R · `b` → escribe `Y`, R, `q2` |
| `q2` | Busca una `c` | `b`, `Z` → R · `c` → escribe `Z`, L, `q3` |
| `q3` | Regresa a la última `X` | `a`, `b`, `Y`, `Z` → L · `X` → R, `q0` |
| `q4` | Verificación final | `Y`, `Z` → R · `]` → `q_accept` |

Si en `q1` o `q2` no se encuentra la `b` o la `c` que hacía falta, o en `q4` aparece un símbolo sin marcar, la cadena está desbalanceada y se rechaza.

La cinta cambia así para `abc`:

```text
[ a b c ]   inicio, cabezal en 'a'
[ X b c ]   q0 marca la 'a'
[ X Y c ]   q1 marca la 'b'
[ X Y Z ]   q2 marca la 'c' y retrocede
                q3 regresa hasta la 'X', q0 ve 'Y' y pasa a q4
                q4 recorre 'Y' y 'Z' hasta ']' → q_accept
```

En la interfaz se dibuja la cinta con `[` y `]` visibles y un puntero que se mueve en ambos sentidos.

### 4.4 Nivel 0: Máquina de Turing para $a^n b^m c^{n \cdot m}$

La cinta es extendible: el simulador agrega blancos `B` a izquierda y derecha cuando el cabezal se acerca a los bordes, de modo que nunca se queda sin espacio. El alfabeto de cinta es $\{a, b, c, X, Y, Z, B\}$.

Estrategia (multiplicación). Por cada `a` (marcada `X`) se recorren todas las `b`: por cada `b` (marcada `Y`) se marca una `c` (como `Z`). Al terminar con esa `a`, las `Y` se restauran a `b` para que la siguiente `a` pueda volver a recorrerlas. Al agotar las `a`, deben quedar solo `b` y `Z` (ninguna `c` sin marcar): si es así, la cantidad de `c` es exactamente $n \cdot m$.

| Estado | Función | Transiciones |
| --- | --- | --- |
| `q0` | Busca la siguiente `a` | `a` → `X`, R, `q1` · `b` (con una `X` a su izquierda) → R, `q5` |
| `q1` | Busca una `b` | `a`, `Y` → R · `b` → `Y`, R, `q2` · `Z` o `B` → L, `q4` |
| `q2` | Busca una `c` sin marcar | `b`, `Z` → R · `c` → `Z`, L, `q3` |
| `q3` | Regresa a la última `Y` | `b`, `Z` → L · `Y` → R, `q1` |
| `q4` | Restaura y vuelve a la `X` | `Z` → L · `Y` → escribe `b`, L · `X` → R, `q0` · otro símbolo → L |
| `q5` | Verificación final | `b`, `Y`, `Z` → R · `B` → `q_accept` |

En `q0`, una `b` solo inicia la verificación si ya se procesó al menos una `a`; así se rechazan cadenas sin `a` como `b` o `bb`. En `q5`, una `b` que aparezca después de una `Z` rechaza la cadena, lo que garantiza el orden $a^n b^m c^{n \cdot m}$ (cadenas como `abcb`).

Ejemplo con `aabcc` ($n=2$, $m=1$, $c=2$): la primera `a` marca una `b` y una `c`; se restaura la `b`; la segunda `a` marca de nuevo esa `b` y la segunda `c`; al agotar las `a` solo quedan `b` y `Z`, se llega al blanco y se acepta.

### 4.5 Protecciones comunes

- Validación del alfabeto. Los niveles 1 y 0 rechazan de entrada cualquier símbolo fuera de `{a, b, c}`. Esto es necesario porque `X`, `Y`, `Z`, `B`, `[` y `]` son símbolos internos de la cinta: si se pudieran escribir como entrada, ciertas cadenas se aceptarían por coincidencia con las marcas.
- Límite de pasos. Los simuladores de cinta tienen un tope de pasos (2000 en el LBA, 5000 en la Máquina de Turing) para que una simulación nunca quede en bucle infinito.
- Cadena vacía. Se rechaza en los cuatro niveles, ya que todos los lenguajes exigen $n, m \ge 1$.

## 5. La visualización

Cada página de nivel sigue el mismo diseño, para que el usuario se sienta en terreno conocido al cambiar de nivel:

1. Barra de entrada: campo de texto, botón *Iniciar* y botones de ejemplo (válidos e inválidos).
2. Traza y estado: la cadena dibujada símbolo por símbolo, con el símbolo actual resaltado, y una lista con la descripción textual de cada paso.
3. Diagrama del autómata: dibujado en SVG, con los estados como nodos y las transiciones como flechas. En cada paso se resalta el estado actual y la flecha usada.
4. Memoria: la pila (nivel 2) o la cinta con su cabezal (niveles 1 y 0).
5. Controles: *Anterior*, *Pausar/Reanudar*, *Siguiente* y velocidad (x0.5, x1, x2; base de 1400 ms por paso).
6. Ventana de rechazo: cuando la cadena se rechaza, explica el motivo y el estado final.

El JavaScript de cada página guarda la lista de pasos recibida del backend y un índice del paso actual. Un temporizador avanza el índice automáticamente; los botones *Anterior* y *Siguiente* lo mueven a mano. En cada cambio de índice, la página repinta el estado resaltado, la memoria y la traza a partir de los datos de ese paso.

## 6. Pruebas

Cada nivel tiene su clase de pruebas JUnit 5 (`Nivel0..3AutomataServiceTest`) con 68 pruebas en total. Prueban, con casos parametrizados:

- Cadenas válidas: se aceptan y terminan en el estado de aceptación.
- Cadenas inválidas: se rechazan (entre ellas casos límite como la cadena vacía, cantidades desbalanceadas y símbolos en orden incorrecto).
- Símbolos fuera del alfabeto de entrada en los niveles 1 y 0.
- Elementos propios de cada modelo: la pila, los delimitadores de frontera en el LBA y los blancos de la cinta en la Máquina de Turing.

Además de las pruebas escritas a mano, se comparó cada autómata contra una definición directa del lenguaje (una expresión regular y conteo de símbolos) probando todas las cadenas posibles hasta cierta longitud. Esa comparación encontró fallos en los niveles 1 y 0 que las pruebas manuales no cubrían (cadenas como `b` o `abcb` aceptadas por error en el nivel 0, y símbolos internos como `Y` aceptados como entrada), y se corrigieron (secciones 4.4 y 4.5).

## 7. Limitaciones conocidas

- El lenguaje del nivel 0 también es sensible al contexto. $a^n b^m c^{n \cdot m}$ puede decidirse con un autómata linealmente acotado, por lo que no *requiere* una Máquina de Turing. Se usa aquí porque ilustra bien el cómputo aritmético sobre una cinta extendible, que es lo que el nivel 0 pide mostrar. Un ejemplo de lenguaje que sí está en el nivel 0 y fuera del nivel 1 es el lenguaje del problema de la parada: una Máquina de Turing puede reconocerlo, pero ninguna máquina puede decidirlo.
- Lenguajes fijos. Cada nivel reconoce un solo lenguaje predeterminado; no se pueden definir autómatas propios desde la interfaz.
- Verificación de orden con la cinta. En el nivel 0 dos comprobaciones (la `X` a la izquierda de la primera `b` y la `Z` a la izquierda de una `b` en la verificación final) se resuelven mirando la celda vecina. En una Máquina de Turing formal se resolverían con estados adicionales; se optó por esta forma para mantener el diagrama de estados simple.
- Direcciones fijas. El frontend llama al backend en `http://localhost:8080`; para desplegarlo en otro lugar habría que cambiar esa dirección.

## 8. Referencias

- Hopcroft, J., Motwani, R. y Ullman, J. *Introducción a la teoría de autómatas, lenguajes y computación.*
- Sipser, M. *Introduction to the Theory of Computation.*
