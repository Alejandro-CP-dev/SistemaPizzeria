<script setup>
// defineProps declara qué datos espera recibir este componente desde su "padre" (quien lo use,
// en este caso App.vue). Es una función especial de Vue que el compilador de <script setup>
// reconoce automáticamente: no hay que importarla.
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
  <!-- Tarjeta completa de una pizza: foto a la izquierda, texto a la derecha (lo arma el CSS de abajo) -->
  <div class="pizza-card">
    <!-- :src lleva los dos puntos porque es un "binding" de Vue: así le decimos que el valor de
         src NO es el texto literal "pizza.imagen", sino el resultado de evaluar esa expresión de
         JavaScript (el valor real guardado en pizza.imagen). Sin los dos puntos, src sería
         siempre el texto fijo "pizza.imagen" y el navegador buscaría una imagen con ese nombre
         literal, que no existe.
         La ruta final se arma pegando la carpeta fija "/images/pizzas/" (donde Vite publica todo
         lo que está en public/images/pizzas/) con el nombre de archivo que llega en pizza.imagen
         (ej: "hawaiana.jpg"), porque en la base de datos solo se va a guardar ese nombre corto,
         no la ruta completa. -->
    <!-- Si no hay nombre de imagen (pizza nueva sin foto, o el campo llegó vacío desde la
         BD), mostramos un recuadro con texto en vez de un <img> roto: un <img> con src
         vacío o inválido se ve como un ícono de "imagen no encontrada", que no se puede
         explicar bien en una exposición. -->
    <div v-if="!pizza.imagen" class="pizza-card__sin-foto">Sin foto</div>
    <img
      v-else
      :src="`/images/pizzas/${pizza.imagen}`"
      :alt="pizza.nombre"
      class="pizza-card__imagen"
    />

    <!-- Columna de texto con el nombre, la descripción y el precio -->
    <div class="pizza-card__info">
      <h3 class="pizza-card__nombre">{{ pizza.nombre }}</h3>
      <p class="pizza-card__descripcion">{{ pizza.descripcion }}</p>
      <!-- Llamamos formatearPrecio directamente aquí, pasándole pizza.precio; Vue ejecuta esta
           función cada vez que el componente se dibuja, no hace falta computed para algo tan simple -->
      <p class="pizza-card__precio">{{ formatearPrecio(pizza.precio) }}</p>
    </div>
  </div>
</template>

<style scoped>
/* "scoped" hace que estas reglas solo apliquen a este componente, para que el CSS de una
   tarjeta no choque por accidente con el CSS de otra parte de la página */

.pizza-card {
  display: flex; /* fila: la imagen queda a la izquierda y la info a la derecha */
  gap: 1rem;
  background-color: #fff8f6; /* crema: fondo de la tarjeta */
  border-radius: 12px;
  overflow: hidden; /* recorta la imagen para que no se salga de las esquinas redondeadas */
  box-shadow: 0 2px 8px rgba(42, 23, 15, 0.15); /* sombra suave usando el café con transparencia */
}

.pizza-card__imagen {
  width: 140px;
  height: 140px;
  object-fit: cover; /* llena el cuadro de 140x140 recortando lo que sobre, sin deformar la foto */
  flex-shrink: 0; /* evita que la imagen se achique si la descripción es larga */
}

.pizza-card__sin-foto {
  width: 140px;
  height: 140px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2a170f; /* café: mismo recuadro de 140x140 que ocuparía la foto */
  color: #fff8f6;
  font-size: 0.85rem;
  text-align: center;
}

.pizza-card__info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0.75rem 1rem 0.75rem 0;
  gap: 0.35rem;
}

.pizza-card__nombre {
  margin: 0;
  color: #a62518; /* rojo tomate: resalta el nombre de la pizza */
  font-size: 1.1rem;
}

.pizza-card__descripcion {
  margin: 0;
  color: #2a170f; /* café: texto normal de la descripción */
  font-size: 0.9rem;
  line-height: 1.3;
}

.pizza-card__precio {
  margin: 0;
  color: #2a6a48; /* verde albahaca: distingue el precio del resto del texto */
  font-weight: bold;
}
</style>
