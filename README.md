# Proyecto automatas - Tercer corte

## Objetivo

Desarrollar un programa tipo simulador interactivo que demuestre como funcionan los autómatas relacionados en la jerarquia de Chomsky; el simulador debe tener una interfaz grafica, y permitir visualizar, simular o validar el comportamiento de los modelos computacionales representativos a cada uno de los niveles de la jerarquia de Chomsky.

## Progreso

### Fecha de entrega esperada

20 de noviembre de 2026

---

### Estados

Implementación y pruebas completadas

## Lenguajes en los que se desarrollará el proyecto

- Java 25
- HTML
- CSS
- JavaScript

### Frameworks

- Spring Boot (Para el backend)
- Astro (Para el frontend)

## Pasos de ejecución del programa

### 1. Backend (Spring Boot)

Para ejecutar el servidor backend, en una terminal dentro de la carpeta [backend/](backend/):

```powershell
mvn spring-boot:run
```

El backend se iniciará en `http://localhost:8080`.

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

## Ejemplos de cadenas de prueba utilizados para cada nivel

Cada nivel dentro del programa debera incluir una cadena de ejemplo para probar la funcionalidad del proyecto, estos serán los ejemplos que se utilizarán:

### Nivel 3

$$L = \{a^n b^m \mid m, n \ge 1 \}$$

Es decir, el lenguaje recibe cualquier cadena que contenga al menos una 'a' seguidas de al menos una 'b'.

- Cadenas válidas: `ab`, `aab`, `abbb`, `aaabbb`
- Cadenas inválidas: `a`, `b`, `ba`, `aba`, `aabba`

---

### Nivel 2

$$L = \{a^n b^n \mid n \ge 1 \}$$

Lo que significa que, el lenguaje aceptara cualquier cadena que contenga una cantidad n de 'a', donde n es al menos 1, seguidas de la misma cantidad de 'b'.

- Cadenas válidas: `ab`, `aabb`, `aaabbb`, `aaaabbbb`
- Cadenas inválidas: `a`, `b`, `aab`, `abb`, `ba`, `abab`

---

### Nivel 1

$$L = \{a^n b^n c^n \mid n \ge 1 \}$$

Es el mismo lenguaje que en el nivel 2, con un agregado, luego de las 'b' deben haber la misma cantidad que 'a' y 'b' que de 'c'.

- Cadenas válidas: `abc`, `aabbcc`, `aaabbbccc`
- Cadenas inválidas: `ab`, `aabbc`, `aabbccc`, `abcba`, `cba`

---

### Nivel 0

$$L = \{a^n b^m c^{n \times m} \mid n, m \ge 1 \}$$

Lo que significa que el lenguaje aceptará cualquier cadena que comience con una cantidad n de 'a', seguida de una cantidad m de 'b' (donde ambos valores son al menos 1), y que finalice con una cantidad de 'c' exactamente igual al producto (multiplicación) de la cantidad de 'a' por la cantidad de 'b'.

- Cadenas válidas: `abc` (1x1=1), `abbcc` (1x2=2), `aabcc` (2x1=2), `aabbcccc` (2x2=4)
- Cadenas inválidas: `ab`, `abcc`, `aabbcc`, `abcba`

## Modelo computacional por nivel

| **Nivel** | **Gramática** | **Modelo** | **Lenguaje de ejemplo** |
| --- | --- | --- | --- |
| 3 | Regular | Autómata Finito | $a^n b^m$ |
| 2 | Libre de contexto | Autómata de pila | $a^n b^n$ |
| 1 | Sensible al contexto | Autómata linealmente acotado | $a^n b^n c^n$ |
| 0 | Recursivamente enumerable | Máquina de Turing | $a^n b^m c^{nxm}$ |
