# proyecto-automatas

## Chomsky Lab — Simulador interactivo

Aplicación educativa para explorar los cuatro niveles de la jerarquía de Chomsky. Una interfaz hecha con Astro consume una API Java con Spring Boot que genera el recorrido de cada modelo computacional.

## Requisitos

- Java 17 o superior.
- Maven 3.6.3 o superior.
- Node.js 22.23.4 o superior y npm.
- Un navegador moderno.

## Instalación y ejecución

Abre dos terminales desde la carpeta del proyecto.

**Terminal 1 — API Java**

```powershell
cd backend
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

**Terminal 2 — Interfaz Astro**

```powershell
cd frontend
npm install
npm run dev
```

Abre `http://localhost:4321`. En la barra lateral elige un nivel; selecciona una cadena de ejemplo o escribe otra y pulsa **Simular**. Usa los controles para avanzar, retroceder, reiniciar o reproducir automáticamente el recorrido. La velocidad se puede cambiar junto a los controles.

Para compilar la interfaz: `cd frontend; npm run build`. Para verificar el backend: `cd backend; mvn test`.

## Niveles y cadenas de prueba

| Nivel | Modelo y lenguaje | Ejemplos aceptados | Ejemplos rechazados |
| --- | --- | --- | --- |
| 3 | AFD, `a*b*` | `aaabbb`, `bbb`, cadena vacía (`ε`) | `abba` |
| 2 | Autómata de pila, `aⁿbⁿ`, `n ≥ 0` | `aabb`, `ab`, cadena vacía (`ε`) | `aab`, `abab` |
| 1 | Máquina linealmente acotada, `aⁿbⁿcⁿ`, `n ≥ 0` | `aabbcc`, `abc`, cadena vacía (`ε`) | `aabcc`, `abbc` |
| 0 | Máquina de Turing: suma unaria `1ⁿ+1ᵐ` | `11+111` → `11111`, `1+1` → `11` | `1+`, `11++1` |

El Nivel 1 limita la entrada a 24 símbolos para mantener legible la animación. El Nivel 0 admite hasta 20 marcas unarias por operando. Los niveles 3, 2 y 1 aceptan el lenguaje vacío cuando corresponde; para escribirlo en el campo de entrada, déjalo vacío. Los símbolos fuera del alfabeto causan rechazo.

## API

`POST /api/simulations/{level}` con un cuerpo JSON como `{"input":"aaabbb"}`. `level` puede ser `0`, `1`, `2` o `3`. La respuesta incluye el resultado, la explicación y una lista ordenada de configuraciones con estado, símbolo leído, posición del cabezal, cinta, pila y acción. La interfaz reproduce localmente esa lista, por lo que avanzar o pausar no vuelve a ejecutar la máquina.

## Documentación técnica

Consulta [docs/IMPLEMENTACION.md](docs/IMPLEMENTACION.md) para la explicación de los modelos, algoritmos, estructura del código y contrato de la API.

## Estructura

```text
backend/   API REST y simuladores Java
frontend/  Interfaz Astro, visualizaciones y controles
docs/      Documentación de implementación
```
