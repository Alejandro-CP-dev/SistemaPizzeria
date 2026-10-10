<script setup>
// defineProps declara qué datos espera recibir este componente desde su "padre" (quien lo use:
// MenuPizzas en el menú público, o PizzaForm en la vista previa de edición). Es una función
// especial de Vue que el compilador de <script setup> reconoce automáticamente: no hay que
// importarla.
defineProps({
  // Toda la información de una pizza (id, imagen, nombre, descripcion, precio) junta en un
  // solo objeto, en vez de mandar cinco props sueltas una por una.
  pizza: Object,
  // "invertido" decide de qué lado va la foto: MenuPizzas se lo pasa como true en las filas
  // pares (índice 1, 3, 5...) para que el menú quede en bandas alternadas (foto-texto,
  // texto-foto, foto-texto...) en vez de todas las filas iguales. Por defecto es false:
  // quien use PizzaCard sin pasar esta prop (como PizzaForm, que solo necesita una vista
  // previa normal) no tiene que preocuparse por ella.
  invertido: { type: Boolean, default: false },
})

// Función normal (no "computed") que recibe el precio, un número entero en pesos (ej: 32000),
// y devuelve un texto con formato colombiano (ej: "$32.000"). No hace falta "computed" aquí:
// las props YA son reactivas por sí solas (si pizza.precio cambia, Vue vuelve a dibujar este
// componente y esta función se vuelve a ejecutar con el valor nuevo, sin que nadie tenga que
// avisarle). "computed" solo suma valor cuando el cálculo es caro y conviene guardarlo en
// caché; aquí es una cuenta trivial, así que una función simple alcanza y es más fácil de
// explicar en una exposición: "esto es JavaScript normal, no magia de Vue".
function formatearPrecio(precio) {
  // toLocaleString("es-CO") le pide a JavaScript que escriba el número como se escribe en
  // Colombia: separando los miles con punto (32000 -> "32.000"). Luego pegamos el símbolo "$"
  // al inicio para formar el precio final.
  return "$" + precio.toLocaleString("es-CO")
}
</script>

<template>
  <!-- :class con un objeto {nombre: condición} agrega "pizza-card--invertido" solo cuando
       "invertido" es true; es la forma estándar de Vue para activar una clase de CSS según
       una condición, en vez de armar el texto de la clase a mano con un ternario. -->
  <div class="pizza-card" :class="{ 'pizza-card--invertido': invertido }">
    <div class="pizza-card__aro">
      <!-- Si no hay nombre de imagen (pizza nueva sin foto, o el campo llegó vacío desde la
           BD), mostramos un recuadro con texto en vez de un <img> roto. -->
      <div v-if="!pizza.imagen" class="pizza-card__sin-foto">Sin foto</div>
      <img
        v-else
        :src="`/images/pizzas/${pizza.imagen}`"
        :alt="pizza.nombre"
        class="pizza-card__imagen"
      />
    </div>

    <div class="pizza-card__info">
      <span class="pizza-card__nombre">{{ pizza.nombre }}</span>
      <p class="pizza-card__descripcion">{{ pizza.descripcion }}</p>
      <!-- Llamamos formatearPrecio directamente aquí, pasándole pizza.precio; Vue ejecuta esta
           función cada vez que el componente se dibuja, no hace falta computed para algo tan simple -->
      <span class="pizza-card__precio">{{ formatearPrecio(pizza.precio) }}</span>
    </div>
  </div>
</template>

<style scoped>
.pizza-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2rem;
}

/* row-reverse intercambia el orden visual de los dos hijos (el aro con la foto y el bloque
   de texto) sin tocar el HTML: es la misma estructura, solo se dibuja al revés. */
.pizza-card--invertido {
  flex-direction: row-reverse;
}

.pizza-card--invertido .pizza-card__info {
  text-align: right;
}

.pizza-card__aro {
  flex-shrink: 0;
  width: 170px;
  height: 170px;
  margin: 0 auto;
  border-radius: 50%;
  background-color: #a62518; /* rojo tomate: el "aro" grueso alrededor de la foto */
  padding: 8px; /* este padding es justamente lo que deja ver el aro rojo detrás de la foto */
}

.pizza-card__imagen,
.pizza-card__sin-foto {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.pizza-card__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2a170f; /* café */
  color: #fff8f6;
  font-size: 0.7rem;
  text-align: center;
}

.pizza-card__info {
  flex: 1 1 280px;
  min-width: 240px;
}

.pizza-card__nombre {
  display: inline-block;
  background-color: #a62518;
  color: #fbf3e7; /* papel: mismo tono cálido que el resto de la página */
  font-family: 'Baloo 2', sans-serif;
  font-weight: 700;
  font-size: 1.15rem;
  padding: 0.55rem 1.4rem;
  border-radius: 999px;
}

.pizza-card__descripcion {
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.95rem;
  line-height: 1.6;
  color: #7a5b4c; /* café suave: texto secundario, menos protagonista que el nombre */
  margin: 0.9rem 0 0;
}

.pizza-card__precio {
  display: inline-block;
  margin-top: 0.7rem;
  background-color: #e3eee7; /* verde albahaca muy tenue, de fondo */
  color: #2a6a48; /* verde albahaca: distingue el precio del resto del texto */
  font-family: 'Figtree', system-ui, sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  padding: 0.35rem 1rem;
  border-radius: 999px;
}

/* En pantallas angostas no alcanza el espacio para foto + texto lado a lado: todo pasa a una
   sola columna centrada, sin importar si la fila era "invertida" o no (invertir el orden ya
   no tiene sentido visual cuando todo está apilado). */
@media (max-width: 560px) {
  .pizza-card,
  .pizza-card--invertido {
    flex-direction: column;
    text-align: center;
  }

  .pizza-card--invertido .pizza-card__info {
    text-align: center;
  }
}
</style>
