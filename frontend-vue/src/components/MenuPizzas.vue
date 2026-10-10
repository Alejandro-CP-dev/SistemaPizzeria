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
    </div>

    <!-- Línea ondulada decorativa debajo del título: un <svg> con una curva que se repite
         ("M...Q...T...T..."), igual que la carta de referencia. Es solo decoración (por eso
         aria-hidden="true": un lector de pantalla no gana nada anunciando una curva), no
         necesita JavaScript ni ninguna librería. -->
    <svg viewBox="0 0 1000 24" preserveAspectRatio="none" class="menu__linea" aria-hidden="true">
      <path
        d="M0 12 Q 25 0 50 12 T 100 12 T 150 12 T 200 12 T 250 12 T 300 12 T 350 12 T 400 12 T 450 12 T 500 12 T 550 12 T 600 12 T 650 12 T 700 12 T 750 12 T 800 12 T 850 12 T 900 12 T 950 12 T 1000 12"
        stroke="#a62518"
        stroke-width="5"
        fill="none"
        stroke-linecap="round"
      />
    </svg>

    <!-- (pizza, indice) en vez de solo "pizza": v-for entrega la posición de cada elemento
         como segundo valor. Se usa solo para decidir el lado de la foto (índice par: foto a
         la izquierda; índice impar: a la derecha) — por eso no hace falta en :key, que sigue
         usando pizza.id como siempre. -->
    <div class="menu__lista">
      <PizzaCard
        v-for="(pizza, indice) in pizzas"
        :key="pizza.id"
        :pizza="pizza"
        :invertido="indice % 2 === 1"
      />
    </div>
  </section>
</template>

<style scoped>
.menu {
  background-color: #f3e6dc; /* crema profunda: separa esta sección del Nosotros oscuro de arriba */
  padding: 5rem 1.5rem 6rem;
  /* Compensa el navbar "sticky" (NavBar.vue) al saltar aquí desde el enlace "Menú". */
  scroll-margin-top: 80px;
}

.menu__encabezado {
  text-align: center;
  margin-bottom: 0.75rem;
}

.menu__ojo {
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a62518;
  font-weight: 600;
  margin: 0 0 0.6rem;
}

.menu__titulo {
  font-family: 'Baloo 2', sans-serif;
  font-weight: 800;
  font-size: 2.75rem;
  color: #a62518;
  margin: 0;
}

.menu__linea {
  display: block;
  width: 100%;
  max-width: 400px;
  height: 16px;
  margin: 0 auto 3.5rem;
}

.menu__lista {
  display: flex;
  flex-direction: column;
  gap: 3.5rem;
  max-width: 980px;
  margin: 0 auto;
}
</style>
