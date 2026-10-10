# Práctica de 6 minutos: v-model en vivo

Ejercicio corto para que tus compañeros lo hagan en su propio computador durante la
exposición. No necesita el proyecto de la pizzería: es un archivo `.vue` nuevo y
separado, pensado para entender **una sola idea**: cuando un dato es reactivo (un `ref`),
cualquier parte de la pantalla que lo muestre se actualiza sola, sin que nadie tenga que
"refrescar" nada a mano.

Si tienen un proyecto Vue con Vite a la mano (por ejemplo, copiando `frontend-vue`), les
sirve crear un componente nuevo como `src/components/Practica.vue` y usarlo desde
`App.vue` por un momento. Si no, también funciona pegando el código final en
[play.vuejs.org](https://play.vuejs.org).

## Paso 1 — Un dato reactivo (1 minuto)

Crea el archivo y escribe solo esto:

```vue
<script setup>
import { ref } from 'vue'

const nombre = ref('')
</script>

<template>
  <p>Hola</p>
</template>
```

`ref('')` crea una caja reactiva que empieza vacía. Todavía nadie la usa ni la modifica:
solo existe.

## Paso 2 — Conectar un input con v-model (2 minutos)

Agrega un `<input>` conectado a `nombre` con `v-model`:

```vue
<template>
  <input v-model="nombre" placeholder="Escribe tu nombre" />
  <p>Hola</p>
</template>
```

`v-model="nombre"` hace dos cosas a la vez: si `nombre` cambiara desde el código, el
input se actualizaría solo; y, al revés (lo que vas a ver ahora), si alguien escribe en
el input, `nombre` cambia solo. Sin `v-model` tocarías `addEventListener('input', ...)` a
mano para lograr lo mismo.

## Paso 3 — Mostrar el dato en vivo (1 minuto)

Cambia el `<p>` fijo por uno que muestre el valor de `nombre`:

```vue
<template>
  <input v-model="nombre" placeholder="Escribe tu nombre" />
  <p>Hola, {{ nombre }}</p>
</template>
```

Prueba escribir en el input: el texto de abajo cambia letra por letra, sin que hayas
escrito ningún código para "actualizar la pantalla". Esa es la parte más importante del
ejercicio: nadie llamó a una función como `actualizarPantalla()`; Vue lo hace solo porque
`nombre` es un `ref`.

## Paso 4 — Una "tarjeta" que se actualiza en vivo (2 minutos)

Convierte ese `<p>` en algo con más forma de tarjeta, igual de reactivo:

```vue
<template>
  <input v-model="nombre" placeholder="Escribe tu nombre" />

  <div class="tarjeta">
    <h3>{{ nombre || 'Sin nombre todavía' }}</h3>
    <p>Esta tarjeta se actualiza mientras escribes arriba.</p>
  </div>
</template>

<style scoped>
.tarjeta {
  margin-top: 1rem;
  padding: 1rem;
  border: 1px solid #a62518;
  border-radius: 8px;
  max-width: 260px;
}
</style>
```

`{{ nombre || 'Sin nombre todavía' }}` es un truco corto: si `nombre` está vacío (texto
vacío cuenta como "falso" en JavaScript), muestra el texto de respaldo en su lugar.

## Para pensar (opcional, si queda tiempo)

Esto es exactamente lo mismo que pasa en `PizzaForm.vue` del proyecto de la pizzería:
el campo de nombre tiene `v-model="copia.nombre"`, y la vista previa (`<PizzaCard
:pizza="copia" />`) se actualiza en vivo por la misma razón que esta tarjeta — ningún
código extra, solo un dato reactivo que dos partes de la pantalla están mirando al mismo
tiempo.
