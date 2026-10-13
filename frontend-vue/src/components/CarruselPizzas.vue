<script setup>
import { computed } from 'vue'
import CircularCarousel from './CircularCarousel.vue'

// Recibe la misma lista de pizzas que ya carga InicioView para MenuPizzas: esta
// sección no hace su propio fetch, solo reutiliza los datos que ya llegaron.
const props = defineProps({
  pizzas: { type: Array, default: () => [] },
})

// El carrusel necesita { src, alt, title, subtitle }; las pizzas sin foto se dejan
// fuera (no tiene sentido mostrar una tarjeta vacía girando en el aro 3D).
const items = computed(() =>
  props.pizzas
    .filter((pizza) => pizza.imagen)
    .map((pizza) => ({
      src: `/images/pizzas/${pizza.imagen}`,
      alt: pizza.nombre,
      title: pizza.nombre,
      subtitle: pizza.descripcion,
    }))
)
</script>

<template>
  <!-- Si todavía no cargó el menú (o ninguna pizza tiene foto) no se muestra nada:
       evita un carrusel vacío girando sin contenido mientras el fetch de InicioView
       sigue en curso. -->
  <section v-if="items.length" class="vista-previa">
    <div class="vista-previa__encabezado">
      <p class="vista-previa__ojo">Antes de la carta</p>
      <h2 class="vista-previa__titulo">Un giro por nuestras pizzas</h2>
    </div>

    <div class="vista-previa__carrusel">
      <CircularCarousel :items="items" preset="cylinder" intro="rise" :card-width="220" :aspect-ratio="1" captions />
    </div>
  </section>
</template>

<style scoped>
/* Mismo fondo espresso que Nosotros (SobreNosotros.vue): esta sección es la
   continuación de esa narrativa de marca antes de entrar al menú claro de abajo. */
.vista-previa {
  background-color: #1b110a;
  padding: 5rem 1.5rem 6rem;
  text-align: center;
}

.vista-previa__encabezado {
  margin-bottom: 2.5rem;
}

.vista-previa__ojo {
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a62518;
  font-weight: 600;
  margin: 0 0 0.75rem;
}

.vista-previa__titulo {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 2.4rem;
  color: #fff8f6;
  margin: 0;
}

.vista-previa__carrusel {
  width: 100%;
  max-width: 1100px;
  height: 460px;
  margin: 0 auto;
  position: relative;
  /* El carrusel usa "color: inherit" en sus captions; esto es lo que las pinta
     claras sobre el fondo oscuro de esta sección. */
  color: #fff8f6;
}

@media (max-width: 560px) {
  .vista-previa__carrusel {
    height: 380px;
  }
}
</style>
