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
    <h2 class="menu__titulo">Menú</h2>
    <div class="menu__grid">
      <PizzaCard v-for="pizza in pizzas" :key="pizza.id" :pizza="pizza" />
    </div>
  </section>
</template>

<style scoped>
.menu {
  background-color: #fff8f6;
  padding: 3rem 1.5rem;
}

.menu__titulo {
  color: #a62518;
  text-align: center;
  margin-bottom: 1.5rem;
}

.menu__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.25rem;
  max-width: 1100px;
  margin: 0 auto;
}
</style>
