# Documentación de implementación

## Arquitectura

El proyecto separa simulación y presentación:

- `backend/` es una API REST Java con Spring Boot. Sus simuladores calculan la secuencia de configuraciones determinista que representa una ejecución.
- `frontend/` es un sitio Astro. Presenta el resultado y permite recorrer la secuencia en ambos sentidos o reproducirla con temporizador.
- `docs/` contiene esta explicación técnica. Los ejemplos prácticos y comandos de instalación están en el README.

El servicio no guarda sesiones: cada solicitud es independiente. Los controles de reproducción son locales al navegador y usan la respuesta completa recibida de la API.

## Modelos implementados

### Nivel 3 — AFD para `a*b*`

El estado `q₀` es inicial y acepta cualquier cantidad de `a`. Al leer `b`, la máquina pasa a `q₁`, que acepta cualquier cantidad adicional de `b`. Leer `a` desde `q₁`, o leer un símbolo fuera de `{a,b}`, lleva al estado trampa `qᵈ`. La cadena vacía se acepta en `q₀`.

La vista dibuja los estados y resalta el estado activo. Cada paso conserva el símbolo leído y la posición siguiente del cabezal.

### Nivel 2 — Autómata de pila para `aⁿbⁿ`

La pila inicia con el marcador `Z₀`. Por cada `a` en el primer bloque se apila una `A`; por cada `b` en el segundo bloque se desapila una `A`. Una `a` después de comenzar los `b`, un símbolo inesperado o una pila que se vacíe antes de tiempo causa rechazo. Acepta cuando termina la entrada, se consumieron pares iguales y solo queda `Z₀`. También acepta ε.

La vista representa la cima de la pila y distingue los símbolos de entrada ya consumidos.

### Nivel 1 — Máquina linealmente acotada para `aⁿbⁿcⁿ`

La cinta contiene la entrada entre `⊢` y `⊣`; el cabezal no sale de esos límites. Antes de simular, se comprueba la forma `a*b*c*`. En cada pasada el cabezal marca el primer `a` disponible como `X`, el siguiente `b` como `Y` y el siguiente `c` como `Z`, y regresa al inicio. Acepta si todas las marcas se emparejaron; si falta un elemento de cualquier bloque, rechaza. ε se acepta.

La entrada se limita a 24 símbolos para que las pasadas y la animación sean manejables. El arreglo de cinta y sus dos delimitadores aparecen en cada configuración.

### Nivel 0 — Máquina de Turing para suma unaria

La entrada debe ser `1ⁿ+1ᵐ`, con ambos operandos no vacíos. La máquina ubica `+`, lo reemplaza con `1`, recorre el segundo bloque hasta el blanco `□`, retrocede una celda y borra la última marca. Así quedan `n + m` unos en la cinta; por ejemplo, `11+111` produce `11111`. Cada lectura, escritura y movimiento queda guardado como un paso. La cinta visualizada incluye blancos laterales para mostrar el espacio de trabajo.

Se limita cada operando a 20 marcas para mantener el recorrido breve. Una entrada con sintaxis inválida recibe un error HTTP 400 con una explicación.

## Contrato de la API

### Solicitud

```http
POST /api/simulations/3
Content-Type: application/json

{"input":"aaabbb"}
```

### Respuesta

```json
{
  "level": 3,
  "title": "Autómata finito determinista",
  "input": "aaabbb",
  "accepted": true,
  "result": "Cadena aceptada",
  "message": "Lenguaje: a*b*...",
  "steps": [
    {
      "number": 0,
      "state": "q₀",
      "readSymbol": "a",
      "headPosition": 0,
      "tape": [],
      "stack": [],
      "action": "Estado inicial..."
    }
  ]
}
```

Los campos `tape` y `stack` no usados por un modelo se devuelven como listas vacías. `headPosition` es el índice de la cinta o la posición lógica de lectura, según el modelo. Un nivel desconocido devuelve HTTP 404; errores de formato o límites devuelven HTTP 400. CORS permite el servidor Astro local en el puerto 4321.

## Código por responsabilidad

- `api/SimulationController.java` expone el endpoint por nivel.
- `api/SimulationRequest.java`, `SimulationResponse.java` y `SimulationStep.java` definen el contrato JSON.
- `service/SimulationService.java` contiene las cuatro implementaciones y produce las configuraciones.
- `frontend/src/scripts/simulator.ts` realiza la solicitud y actualiza visualización, controles y estado de reproducción.
- `frontend/src/pages/index.astro` declara la estructura de la página; `frontend/src/styles/global.css` define el diseño responsivo.

## Verificación

Desde `backend/`, ejecutar `mvn test` verifica aceptación y rechazo representativos, cadenas vacías, símbolos inválidos, sintaxis de suma y el resultado `11111`. Desde `frontend/`, `npm run build` confirma que la página Astro compila.
