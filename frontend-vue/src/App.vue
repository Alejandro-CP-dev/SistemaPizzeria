<script setup>
import { ref } from "vue"
import PizzaCard from "./components/PizzaCard.vue"

// ref() convierte este arreglo en un dato reactivo: si en el futuro se agrega, quita o edita
// una pizza, Vue se da cuenta solo y vuelve a dibujar la pantalla sin que lo hagamos a mano.
// Por ahora son datos de prueba (mock) mientras no hay conexión a una base de datos real.
// El campo "imagen" guarda solo el nombre del archivo (así se va a guardar en la BD); la ruta
// completa (/images/pizzas/...) se arma dentro de PizzaCard.vue, no aquí.
const pizzas = ref([
  {
    id: 1,
    imagen: "hawaiana.jpg",
    nombre: "Hawaiana",
    descripcion: "Salsa de tomate, mozzarella, jamón y piña",
    precio: 28000,
  },
  {
    id: 2,
    imagen: "pepperoni.jpg",
    nombre: "Pepperoni",
    descripcion: "Salsa de tomate, mozzarella y pepperoni",
    precio: 30000,
  },
  {
    id: 3,
    imagen: "mexicana.jpg",
    nombre: "Mexicana",
    descripcion: "Salsa de tomate, mozzarella, carne molida, fríjol, jalapeño, maíz y cilantro",
    precio: 34000,
  },
  {
    id: 4,
    imagen: "carnes.jpg",
    nombre: "Carnes",
    descripcion: "Salsa de tomate, mozzarella, carne desmechada, chorizo y tocineta",
    precio: 36000,
  },
  {
    id: 5,
    imagen: "costillas.jpg",
    nombre: "Costillas BBQ",
    descripcion: "Salsa BBQ, mozzarella, costilla de cerdo desmechada y cebolla morada",
    precio: 38000,
  },
  {
    id: 6,
    imagen: "pollo-champinon.jpg",
    nombre: "Pollo y Champiñón",
    descripcion: "Salsa de tomate, mozzarella, pollo desmechado y champiñones",
    precio: 32000,
  },
])
</script>

<template>
  <main class="menu">
    <h1 class="menu__titulo">Menú</h1>

    <div class="menu__grid">
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
