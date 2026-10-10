<script setup>
import { ref, onMounted } from 'vue'
import HeroScroll from '../components/HeroScroll.vue'
import SobreNosotros from '../components/SobreNosotros.vue'
import MenuPizzas from '../components/MenuPizzas.vue'

// Esta es la misma lógica de carga que antes vivía en App.vue: se movió aquí porque
// ahora App.vue solo arma el layout general (NavBar + la vista de turno) y esta
// pantalla es, específicamente, la que necesita el menú de pizzas.
const pizzas = ref([])
const cargando = ref(true)
const error = ref('')

async function cargarPizzas() {
  try {
    const respuesta = await fetch('/api/pizzas')
    if (!respuesta.ok) {
      throw new Error('El servidor respondió con error ' + respuesta.status)
    }
    pizzas.value = await respuesta.json()
  } catch (e) {
    console.error(e)
    error.value = 'No se pudo cargar el menú. Intenta de nuevo más tarde.'
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargarPizzas()
})
</script>

<template>
  <div>
    <HeroScroll />
    <SobreNosotros />

    <!-- El estado de carga/error se maneja aquí, antes de llegar a MenuPizzas, porque
         MenuPizzas no sabe (ni necesita saber) nada de fetch: solo recibe un arreglo. -->
    <section v-if="cargando" class="menu-estado">Cargando pizzas...</section>
    <section v-else-if="error" class="menu-estado">{{ error }}</section>
    <MenuPizzas v-else :pizzas="pizzas" />
  </div>
</template>

<style scoped>
.menu-estado {
  padding: 3rem 1.5rem;
  text-align: center;
  color: #2a170f;
}
</style>
