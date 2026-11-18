# Proyecto automatas - Tercer corte

## Objetivo

Desarrollar un programa tipo simulador interactivo que demuestre como funcionan los autómatas relacionados en la jerarquia de Chomsky; el simulador debe tener una interfaz grafica, y permitir visualizar, simular o validar el comportamiento de los modelos computacionales representativos a cada uno de los niveles de la jerarquia de Chomsky.

## Progreso

### Fecha de entrega esperada

20 de noviembre de 2026

---

### Estados

Implementación y pruebas completadas

## Lenguajes utilizados

- Java 25
- HTML
- CSS
- JavaScript

### Frameworks

- Spring Boot (Para el backend)
- Astro (Para el frontend)

## Pasos de ejecución del programa

### Requisitos previos

- Java 25 (JDK)
- Maven
- Node.js 18.17 o superior (se recomienda 20+) con npm

Clonar el repositorio:

```powershell
git clone https://github.com/Pollito-0601/proyecto-automatas.git
```

### 1. Backend (Spring Boot)

Para ejecutar el servidor backend, en una terminal dentro de la carpeta [backend/](backend/):

```powershell
mvn spring-boot:run
```

El backend se iniciará en `http://localhost:8080`. Debe estar en ejecución antes de abrir la interfaz.

Para ejecutar las pruebas unitarias automatizadas:

```powershell
mvn test
```

### 2. Frontend (Astro)

Para ejecutar la interfaz gráfica, en una segunda terminal dentro de la carpeta [frontend/](frontend/):

```powershell
npm install
npm run dev
```

La aplicación se iniciará en `http://localhost:4321`.

## Uso de la interfaz

1. En el panel principal se elige el nivel (módulo) que se quiere probar; también se puede cambiar de nivel desde el menú lateral.
2. Se escribe una cadena en el campo *Input* o se pulsa uno de los botones de ejemplo, y se presiona *Iniciar*.
3. La simulación avanza paso a paso mostrando el estado actual del autómata, la traza de cada transición y, según el nivel, la pila o la cinta con el cabezal.
4. Con *Pausar*, *Anterior* y *Siguiente* se controla la animación, y los botones *x0.5*, *x1* y *x2* cambian la velocidad.
5. Al terminar se indica si la cadena fue aceptada o rechazada.

El alfabeto de entrada es `{a, b}` en los niveles 3 y 2, y `{a, b, c}` en los niveles 1 y 0. La cadena vacía se rechaza en todos los niveles.

## Ejemplos de cadenas de prueba utilizados para cada nivel

Cada nivel incluye en la interfaz botones con cadenas de ejemplo para probar la funcionalidad del proyecto. Estos son los ejemplos utilizados:

### Nivel 3

$$L = \{a^n b^m \mid m, n \ge 1 \}$$

Es decir, el lenguaje recibe cualquier cadena que contenga al menos una 'a' seguidas de al menos una 'b'.

- Cadenas válidas: `ab`, `aaabbb`, `abbbb`
- Cadenas inválidas: `ba`, `aba`

---

### Nivel 2

$$L = \{a^n b^n \mid n \ge 1 \}$$

Lo que significa que, el lenguaje aceptara cualquier cadena que contenga una cantidad n de 'a', donde n es al menos 1, seguidas de la misma cantidad de 'b'.

- Cadenas válidas: `ab`, `aabb`, `aaabbb`
- Cadenas inválidas: `aab`, `abb`, `ba`

---

### Nivel 1

$$L = \{a^n b^n c^n \mid n \ge 1 \}$$

Es el mismo lenguaje que en el nivel 2, con un agregado, luego de las 'b' deben haber la misma cantidad que 'a' y 'b' que de 'c'.

- Cadenas válidas: `abc`, `aabbcc`, `aaabbbccc`
- Cadenas inválidas: `aabbc`, `aabbccc`, `abcba`

---

### Nivel 0

$$L = \{a^n b^m c^{n \times m} \mid n, m \ge 1 \}$$

Lo que significa que el lenguaje aceptará cualquier cadena que comience con una cantidad n de 'a', seguida de una cantidad m de 'b' (donde ambos valores son al menos 1), y que finalice con una cantidad de 'c' exactamente igual al producto (multiplicación) de la cantidad de 'a' por la cantidad de 'b'.

- Cadenas válidas: `abc` (1x1=1), `abbcc` (1x2=2), `aabcc` (2x1=2), `aabbcccc` (2x2=4)
- Cadenas inválidas: `ab`, `abcc`

## Documentación

La explicación de la implementación y de los conceptos aplicados está en [docs/IMPLEMENTACION.md](docs/IMPLEMENTACION.md).

## Modelo computacional por nivel

| **Nivel** | **Gramática** | **Modelo** | **Lenguaje de ejemplo** |
| --- | --- | --- | --- |
| 3 | Regular | Autómata Finito | $a^n b^m$ |
| 2 | Libre de contexto | Autómata de pila | $a^n b^n$ |
| 1 | Sensible al contexto | Autómata linealmente acotado | $a^n b^n c^n$ |
| 0 | Recursivamente enumerable | Máquina de Turing | $a^n b^m c^{nxm}$ |
