# Proyecto automatas - Tercer corte

## Objetivo

Desarrollar un programa tipo simulador interactivo que demuestre como funcionan los autómatas relacionados en la jerarquia de Chomsky; el simulador debe tener una interfaz grafica, y permitir visualizar, simular o validar el comportamiento de los modelos computacionales representativos a cada uno de los niveles de la jerarquia de Chomsky.

## Progreso

### Fecha de entrega esperada

20 de noviembre de 2026

---

### Estados

Documentación inicial - actual

## Lenguajes en los que se desarrollará el proyecto

- Java 21
- HTML
- CSS
- JavaScript

### Frameworks

- Spring Boot (Para el backend)
- Astro (Para el frontend)

## Pasos de ejecución del programa

Por el momento no existe programa que ejecutar.

## Ejemplos de cadenas de prueba utilizados para cada nivel

Cada nivel dentro del programa debera incluir una cadena de ejemplo para probar la funcionalidad del proyecto, estos serán los ejemplos que se utilizarán:

### Nivel 3

$$L = \{a^n b^m \mid m, n \ge 1 \}$$

Es decir, el lenguaje recibe cualquier cadena que contenga al menos una 'a' seguidas de al menos una 'b'.

---

### Nivel 2

$$L = \{a^n b^n \mid n \ge 1 \}$$

Lo que significa que, el lenguaje aceptara cualquier cadena que contenga una cantidad n de 'a', donde n es al menos 1, seguidas de la misma cantidad de 'b'.

---

### Nivel 1

$$L = \{a^n b^n c^n \mid n \ge 1 \}$$

Es el mismo lenguaje que en el nivel 2, con un agregado, luego de las 'b' deben haber la misma cantidad que 'a' y 'b' que de 'c'.

---

### Nivel 0

$$L = \{a^n b^m c^{n \times m} \mid n, m \ge 1 \}$$

Lo que significa que el lenguaje aceptará cualquier cadena que comience con una cantidad n de 'a', seguida de una cantidad m de 'b' (donde ambos valores son al menos 1), y que finalice con una cantidad de 'c' exactamente igual al producto (multiplicación) de la cantidad de 'a' por la cantidad de 'b'.
