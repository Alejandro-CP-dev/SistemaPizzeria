<script setup>
import { ref } from "vue"
import { onMounted } from "vue"
import PizzaCard from "./components/PizzaCard.vue"

// ref() convierte este arreglo en un dato reactivo: si en el futuro se agrega, quita o edita
// una pizza, Vue se da cuenta solo y vuelve a dibujar la pantalla sin que lo hagamos a mano.
const pizzas = ref([])
const cargando = ref(true)
const error = ref('')

// Carga las pizzas desde el backend (api/pizzas) usando fetch y async/await.
async function cargarPizzas() {
  try{
    const respuesta = await fetch('/api/pizzas') 

    if(!respuesta.ok){
      throw new Error('El servidor respondió con error' + respuesta.status)
    }

    pizzas.value = await respuesta.json()
  }catch(e){
    console.error(e)
    error.value = 'No se pudo cargar el menú. Intenta de nuevo más tarde.'
  }finally{
    cargando.value = false // aunque haya error, ya no estamos cargando
  }
}

onMounted(() => {
  cargarPizzas()
})
</script>

<template>
  <main class="menu">
    <h1 class="menu__titulo">Menú</h1>

    <p v-if="cargando">Cargando pizzas...</p>
    <p v-else-if="error">{{ error }}</p>
    <div v-else class="menu__grid">
      <!-- v-for recorre el arreglo "pizzas" y crea un <PizzaCard> por cada elemento.
           "pizza in pizzas" nombra "pizza" a cada elemento individual dentro de la vuelta del ciclo.

           :key="pizza.id" le da a Vue un identificador único por tarjeta: así, cuando el arreglo
           cambie (se agregue, borre o reordene una pizza), Vue sabe exactamente qué tarjeta crear,
           mover o eliminar en vez de tener que redibujar toda la cuadrícula de nuevo. Sin :key,
           Vue podría confundir una tarjeta con otra y mezclar datos en pantalla.

           :pizza="pizza" lleva los dos puntos porque es un binding (igual que :src en PizzaCard):
           le estamos pasando el OBJETO pizza completo (con id, imagen, nombre, etc.) como prop al
           componente hijo. Sin los dos puntos, Vue entendería que el texto literal "pizza" es la
           prop, no el objeto que viene del v-for. -->
      <PizzaCard
        v-for="pizza in pizzas"
        :key="pizza.id"
        :pizza="pizza"
      />
    </div>
  </main>
</template>

<style scoped>
.menu {
  background-color: #fff8f6; /* crema: fondo de toda la página del menú */
  min-height: 100vh;
  padding: 2rem 1rem;
}

.menu__titulo {
  color: #a62518; /* rojo tomate: mismo color de acento que el nombre de cada pizza */
  text-align: center;
  margin-bottom: 1.5rem;
}

.menu__grid {
  display: grid;
  /* auto-fill + minmax hace la cuadrícula responsive: cada tarjeta mide al menos 320px de ancho;
     si la pantalla es angosta y no caben dos columnas, pasa sola a una sola columna */
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.25rem;
  max-width: 1100px;
  margin: 0 auto;
}
</style>
