<script setup>
// defineProps declara qué datos espera recibir este componente desde su "padre" (quien lo use:
// MenuPizzas en el menú público, o PizzaForm en la vista previa de edición). Es una función
// especial de Vue que el compilador de <script setup> reconoce automáticamente: no hay que
// importarla.
// Pedimos UNA sola prop llamada "pizza", de tipo Object, porque viaja toda la información de
// una pizza (id, imagen, nombre, descripcion, precio) junta dentro de un solo objeto, en vez
// de mandar cinco props sueltas una por una.
defineProps({
  pizza: Object,
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
  <!-- Una fila de "carta de restaurante": foto circular pequeña a la izquierda, nombre y
       precio en la misma línea, descripción en cursiva debajo. -->
  <div class="pizza-card">
    <!-- Si no hay nombre de imagen (pizza nueva sin foto, o el campo llegó vacío desde la
         BD), mostramos un recuadro con texto en vez de un <img> roto. -->
    <div v-if="!pizza.imagen" class="pizza-card__sin-foto">Sin foto</div>
    <img
      v-else
      :src="`/images/pizzas/${pizza.imagen}`"
      :alt="pizza.nombre"
      class="pizza-card__imagen"
    />

    <div class="pizza-card__info">
      <div class="pizza-card__fila">
        <h3 class="pizza-card__nombre">{{ pizza.nombre }}</h3>
        <!-- Llamamos formatearPrecio directamente aquí, pasándole pizza.precio; Vue ejecuta esta
             función cada vez que el componente se dibuja, no hace falta computed para algo tan simple -->
        <span class="pizza-card__precio">{{ formatearPrecio(pizza.precio) }}</span>
      </div>
      <p class="pizza-card__descripcion">{{ pizza.descripcion }}</p>
    </div>
  </div>
</template>

<style scoped>
/* "scoped" hace que estas reglas solo apliquen a este componente, para que el CSS de una
   tarjeta no choque por accidente con el CSS de otra parte de la página */

.pizza-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.35rem 0;
  /* Línea delgada entre pizzas, como los renglones de una carta impresa, en vez de cada
     pizza metida en su propia caja con sombra. */
  border-bottom: 1px solid rgba(42, 23, 15, 0.09);
}

.pizza-card__imagen,
.pizza-card__sin-foto {
  width: 72px;
  height: 72px;
  border-radius: 50%; /* foto circular: el detalle que le da el aire de "carta elegante" a la fila */
  object-fit: cover;
  flex-shrink: 0;
  /* Anillo delgado en rojo tomate al 30% de opacidad: marca la identidad de la marca sin
     ponerle un círculo de color grueso detrás, que se vería más "app" que "restaurante". */
  border: 1.5px solid rgba(166, 37, 24, 0.3);
}

.pizza-card__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2a170f; /* café */
  color: #fff8f6;
  font-size: 0.6rem;
  text-align: center;
}

.pizza-card__info {
  flex: 1;
  min-width: 0; /* permite que pizza-card__descripcion pueda encogerse en vez de desbordar */
}

.pizza-card__fila {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 1rem;
}

.pizza-card__nombre {
  margin: 0;
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 1.25rem;
  color: #2a170f; /* café: antes el nombre iba en rojo tomate, ahora ese rojo se reserva para
                      detalles pequeños (el anillo de la foto) y el nombre lee como texto fino */
}

.pizza-card__precio {
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-weight: 600;
  font-size: 1.15rem;
  color: #2a6a48; /* verde albahaca: distingue el precio del resto del texto */
  white-space: nowrap;
}

.pizza-card__descripcion {
  margin: 0.4rem 0 0;
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-style: italic;
  font-size: 0.95rem;
  line-height: 1.5;
  color: #7a5b4c; /* café suave: un tono más claro que el café de texto principal, para que la
                      descripción quede claramente por debajo del nombre en importancia */
}
</style>
