<script setup>
defineProps({
  pizzas: Array,
})

// Emite el evento con la pizza completa (no solo el id): así AdminView y PizzaForm
// reciben de una vez todos los datos que necesitan para mostrar el formulario de edición
// o el mensaje de confirmación, sin tener que ir a buscarla de nuevo en la lista.
const emit = defineEmits(['editar', 'eliminar', 'agregar'])

// Misma función que PizzaCard.vue: se repite aquí (en vez de importarla de algún lado)
// porque son solo dos líneas y así cada componente se explica solo, sin que haya que
// saltar a otro archivo para entender de dónde sale el "$32.000".
function formatearPrecio(precio) {
  return '$' + precio.toLocaleString('es-CO')
}
</script>

<template>
  <div class="pizza-tabla">
    <div v-if="pizzas.length === 0" class="pizza-tabla__vacio">
      <p>Todavía no hay pizzas en el menú.</p>
      <button type="button" @click="emit('agregar')">Agregar pizza</button>
    </div>

    <!-- overflow-x: auto en el contenedor (ver <style>) deja que la TABLA se desplace
         de lado en pantallas angostas, en vez de desbordar y romper el diseño de toda
         la página. -->
    <div v-else class="pizza-tabla__scroll">
      <table class="pizza-tabla__tabla">
        <thead>
          <tr>
            <th>Foto</th>
            <th>Nombre</th>
            <th>Descripción</th>
            <th>Precio</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pizza in pizzas" :key="pizza.id">
            <td>
              <div v-if="!pizza.imagen" class="pizza-tabla__sin-foto">Sin foto</div>
              <img
                v-else
                :src="`/images/pizzas/${pizza.imagen}`"
                :alt="pizza.nombre"
                class="pizza-tabla__foto"
              />
            </td>
            <td>{{ pizza.nombre }}</td>
            <!-- class="pizza-tabla__descripcion" recorta el texto a una sola línea con "..."
                 (ver <style>): una descripción larga no debería estirar la fila completa. -->
            <td class="pizza-tabla__descripcion">{{ pizza.descripcion }}</td>
            <td>{{ formatearPrecio(pizza.precio) }}</td>
            <td class="pizza-tabla__acciones">
              <button type="button" @click="emit('editar', pizza)">Editar</button>
              <button type="button" @click="emit('eliminar', pizza)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.pizza-tabla__scroll {
  overflow-x: auto;
}

.pizza-tabla__vacio {
  text-align: center;
  padding: 3rem 1rem;
  color: #2a170f;
}

.pizza-tabla__vacio button {
  margin-top: 0.75rem;
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 6px;
  background-color: #2a6a48;
  color: #fff8f6;
  cursor: pointer;
}

.pizza-tabla__tabla {
  width: 100%;
  min-width: 560px; /* por debajo de este ancho, mejor que se desplace que se aplaste */
  border-collapse: collapse;
}

.pizza-tabla__tabla th,
.pizza-tabla__tabla td {
  padding: 0.6rem;
  text-align: left;
  border-bottom: 1px solid #fff8f6;
  color: #2a170f;
}

.pizza-tabla__foto,
.pizza-tabla__sin-foto {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
}

.pizza-tabla__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2a170f;
  color: #fff8f6;
  font-size: 0.6rem;
  text-align: center;
}

.pizza-tabla__descripcion {
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* corta el texto y pone "..." si no cabe en una sola línea */
}

.pizza-tabla__acciones {
  display: flex;
  gap: 0.5rem;
}

.pizza-tabla__acciones button {
  padding: 0.35rem 0.75rem;
  border: 1px solid #2a170f;
  border-radius: 6px;
  background-color: transparent;
  cursor: pointer;
}
</style>
