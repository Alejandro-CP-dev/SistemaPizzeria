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
    <div class="pizza-card__foto">
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
      <div class="pizza-card__fila">
        <h3 class="pizza-card__nombre">{{ pizza.nombre }}</h3>
        <!-- Igual que con la foto: una pizza recién creada en PizzaForm empieza con
             precio = null (todavía no se ha escrito nada), y llamar a formatearPrecio(null)
             rompería (null no tiene .toLocaleString). Por eso también aquí hay un v-if/v-else,
             en vez de confiar en que "precio" siempre va a venir con un número. -->
        <span v-if="pizza.precio" class="pizza-card__precio">{{ formatearPrecio(pizza.precio) }}</span>
        <span v-else class="pizza-card__precio pizza-card__precio--vacio">Sin precio</span>
      </div>
      <p class="pizza-card__descripcion">{{ pizza.descripcion }}</p>
    </div>
  </div>
</template>

<style scoped>
.pizza-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background-color: var(--surface-container);
  border-radius: var(--radius-xl);
  box-shadow: 0 1px 3px rgba(44, 24, 16, 0.08);
  transition: box-shadow 0.2s;
}

.pizza-card:hover {
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.12);
}

@media (min-width: 640px) {
  .pizza-card {
    flex-direction: row;
    align-items: flex-start;
  }

  /* row-reverse intercambia el orden visual de los dos hijos (la foto y el bloque de
     texto) sin tocar el HTML: es la misma estructura, solo se dibuja al revés. En celular
     (ver regla de arriba) no se invierte nada: la foto siempre queda arriba. */
  .pizza-card--invertido {
    flex-direction: row-reverse;
  }
}

.pizza-card__foto {
  flex-shrink: 0;
  width: 100%;
  max-width: 220px;
  height: 160px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(44, 24, 16, 0.18);
}

@media (min-width: 640px) {
  .pizza-card__foto {
    width: 128px;
    height: 128px;
  }
}

.pizza-card__imagen,
.pizza-card__sin-foto {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pizza-card__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--inverse-surface);
  color: var(--inverse-on-surface);
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  text-align: center;
}

.pizza-card__info {
  flex: 1 1 auto;
  min-width: 0;
  width: 100%;
}

.pizza-card__fila {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-sm);
}

.pizza-card__nombre {
  margin: 0;
  font: var(--fw-headline-sm) var(--fs-headline-sm)/var(--lh-headline-sm) var(--font-headline);
  color: var(--on-surface);
  letter-spacing: -0.01em;
}

.pizza-card__precio {
  flex-shrink: 0;
  white-space: nowrap;
  font: var(--fw-metric-num) var(--fs-metric-num)/var(--lh-metric-num) var(--font-body);
  color: var(--primary);
}

.pizza-card__precio--vacio {
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-card__descripcion {
  margin: 0.25rem 0 0;
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: var(--on-surface-variant);
}
</style>
