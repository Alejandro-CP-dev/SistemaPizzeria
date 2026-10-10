<script setup>
import { ref } from 'vue'
import PizzaCard from './PizzaCard.vue'

// Un solo objeto con la misma forma que las pizzas de la base de datos
const nuevaPizza = ref({
  nombre: '',        // 1. ¿Con qué valor empieza un campo de texto vacío?
  descripcion: '',
  precio: 0,        // 2. ¿0, null o ''? Piensa qué mostraría la vista previa en cada caso
  imagen: ''
})

// Las imágenes que ya existen en public/images/pizzas/
const imagenesDisponibles = ['carnes.jpg', 'costillas.jpg', 'pepperoni.jpg', 'hawaiana.jpg', 'mexicana.jpg', 'pollo-champinon.jpg']   // 3. Los 6 nombres de archivo, entre comillas
</script>

<template>
  <section class="pizza-form">
    <form>
      <label>
        Nombre
        <input v-model="nuevaPizza.nombre" maxlength="50" />          <!-- 4. el v-model hacia nuevaPizza.nombre -->
      </label>
      <small>{{ nuevaPizza.nombre.length }} / 50</small>            <!-- 5. ¿cuántos caracteres lleva escritos? -->

      <label>
        Descripción
        <textarea v-model="nuevaPizza.descripcion" maxlength="200"></textarea>
      </label>

      <label>
        Precio
        <input type="number" v-model.number="nuevaPizza.precio" />            <!-- 6. ¡con el modificador! -->
      </label>

      <label>
        Imagen
        <select v-model="nuevaPizza.imagen">
          <option value="">Selecciona una imagen</option>
          <option v-for="img in imagenesDisponibles" :key="img" :value="img">{{ img }}</option>
        </select>
      </label>
    </form>

    <!-- La misma tarjeta del menú, pero alimentada por el formulario -->
    <h3>Vista previa en la carta</h3>
    <PizzaCard :pizza="nuevaPizza" />                 <!-- 7. ¿Qué le pasas? -->
  </section>
</template>