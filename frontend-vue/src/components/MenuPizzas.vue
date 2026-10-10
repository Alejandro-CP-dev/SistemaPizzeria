<script setup>
import PizzaCard from './PizzaCard.vue'

// Recibe la lista ya cargada desde InicioView: este componente nunca hace fetch por su
// cuenta, solo se encarga de dibujar lo que le llega.
defineProps({
  pizzas: Array,
})
</script>

<template>
  <section id="menu" class="menu">
    <div class="menu__encabezado">
      <p class="menu__ojo">Nuestra carta</p>
      <h2 class="menu__titulo">Menú</h2>
      <div class="menu__linea"></div>
    </div>

    <!-- "menu__carta" es la tarjeta de papel donde viven las filas de PizzaCard, una debajo
         de otra: el efecto de carta física lo da este contenedor, no cada fila por separado. -->
    <div class="menu__carta">
      <PizzaCard v-for="pizza in pizzas" :key="pizza.id" :pizza="pizza" />
    </div>
  </section>
</template>

<style scoped>
.menu {
  /* Mismo tono de "crema profunda" que separa visualmente esta sección del Nosotros oscuro
     de arriba y de las demás secciones claras. */
  background-color: #f3e6dc;
  padding: 5rem 1.5rem 6rem;
}

.menu__encabezado {
  text-align: center;
  margin-bottom: 2.5rem;
}

.menu__ojo {
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a62518;
  font-weight: 600;
  margin: 0 0 0.75rem;
}

.menu__titulo {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 2.5rem;
  color: #2a170f;
  margin: 0;
}

.menu__linea {
  width: 64px;
  height: 1px;
  background-color: #a62518;
  margin: 1.5rem auto 0;
}

.menu__carta {
  max-width: 820px;
  margin: 0 auto;
  background-color: #fbf3e7; /* "papel": un tono de cartulina cálida, distinto de la crema
                                 normal, para que se sienta como una carta de verdad */
  border-radius: 6px;
  box-shadow: 0 20px 48px rgba(42, 23, 15, 0.16);
  padding: 0 2rem;
}

/* :deep() porque PizzaCard es un componente hijo: sin él, esta regla no llegaría a tocar
   su <div class="pizza-card">, ya que el "scoped" de este archivo solo marca los elementos
   que están escritos directamente en ESTE <template>. Le quita la línea divisoria a la
   última fila para que no quede flotando sola justo antes del borde redondeado de la carta. */
.menu__carta :deep(.pizza-card:last-child) {
  border-bottom: none;
}
</style>
