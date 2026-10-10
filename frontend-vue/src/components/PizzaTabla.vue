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
            <td class="pizza-tabla__nombre">{{ pizza.nombre }}</td>
            <!-- class="pizza-tabla__descripcion" recorta el texto a una sola línea con "..."
                 (ver <style>): una descripción larga no debería estirar la fila completa. -->
            <td class="pizza-tabla__descripcion">{{ pizza.descripcion }}</td>
            <td class="pizza-tabla__precio">{{ formatearPrecio(pizza.precio) }}</td>
            <td class="pizza-tabla__acciones">
              <button type="button" class="pizza-tabla__editar" @click="emit('editar', pizza)">
                Editar
              </button>
              <button type="button" class="pizza-tabla__eliminar" @click="emit('eliminar', pizza)">
                Eliminar
              </button>
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
  color: #7a5b4c;
  font-family: 'Figtree', system-ui, sans-serif;
}

.pizza-tabla__vacio button {
  margin-top: 1rem;
  padding: 0.6rem 1.3rem;
  border: none;
  border-radius: 2px;
  background-color: #a62518;
  color: #fbf3e7;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
}

.pizza-tabla__tabla {
  width: 100%;
  min-width: 620px; /* por debajo de este ancho, mejor que se desplace que se aplaste */
  border-collapse: collapse;
  font-family: 'Figtree', system-ui, sans-serif;
}

.pizza-tabla__tabla thead th {
  padding: 0 0.6rem 0.9rem;
  text-align: left;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #a62518;
  border-bottom: 1.5px solid #e0cfc3;
}

.pizza-tabla__tabla tbody td {
  padding: 0.85rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid #f0e3d8;
  color: #2a170f;
}

.pizza-tabla__tabla tbody tr:hover {
  background-color: #fbf3e7;
}

.pizza-tabla__nombre {
  font-weight: 600;
}

.pizza-tabla__precio {
  color: #2a6a48;
  font-weight: 600;
}

.pizza-tabla__foto,
.pizza-tabla__sin-foto {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid rgba(166, 37, 24, 0.35);
}

.pizza-tabla__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2a170f;
  color: #fff8f6;
  font-size: 0.55rem;
  text-align: center;
}

.pizza-tabla__descripcion {
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* corta el texto y pone "..." si no cabe en una sola línea */
  color: #7a5b4c;
}

.pizza-tabla__acciones {
  display: flex;
  gap: 0.5rem;
}

.pizza-tabla__acciones button {
  padding: 0.4rem 0.85rem;
  border-radius: 2px;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;
}

.pizza-tabla__editar {
  border: 1px solid #2a170f;
  background-color: transparent;
  color: #2a170f;
}

.pizza-tabla__editar:hover {
  background-color: #2a170f;
  color: #fff8f6;
}

.pizza-tabla__eliminar {
  border: 1px solid #a62518;
  background-color: transparent;
  color: #a62518;
}

.pizza-tabla__eliminar:hover {
  background-color: #a62518;
  color: #fff8f6;
}
</style>
