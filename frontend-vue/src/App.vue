<script setup>
import { ref } from "vue"

const nombre = "Juan"

const arrayPizzas = ref([
  {
    name: "Pepperoni",
    price: "$1200",
    description: "Mozzarella, pepperoni y orégano",
  },
  {
    name: "Hawaiana",
    price: "$1100",
    description: "Mozzarella, piña y jamón",
  },
])

// Methods
const agregarPizza = () => {
  const nuevaPizza = {
    name: "Margarita",
    price: "$1000",
    description: "Mozzarella, tomate y albahaca",
  }
  arrayPizzas.value.push(nuevaPizza)
  console.log(arrayPizzas.value)
}
</script>

<template>
  <h1>Hola {{ nombre }}</h1>
  <ul>
    <li v-for="pizza in arrayPizzas" :key="pizza.name">
      <h2>{{ pizza.name }}</h2>
      <p>{{ pizza.price }}</p>
      <p>{{ pizza.description }}</p>
    </li>
  </ul>
  <button @click="agregarPizza">Agregar Pizza</button>
</template>

