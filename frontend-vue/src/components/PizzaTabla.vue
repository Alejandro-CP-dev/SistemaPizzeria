<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  pizzas: Array,
  // El id de la pizza que se está editando en este momento en PizzaForm (o null si el
  // formulario está en modo "nueva pizza"). AdminView es quien lo sabe de verdad; esta
  // tabla solo lo recibe para poder resaltar la fila correspondiente.
  idEditando: { type: Number, default: null },
})

// Emite el evento con la pizza completa (no solo el id): así AdminView y PizzaForm
// reciben de una vez todos los datos que necesitan para mostrar el formulario de edición
// o el mensaje de confirmación, sin tener que ir a buscarla de nuevo en la lista.
const emit = defineEmits(['editar', 'eliminar', 'agregar'])

const busqueda = ref('')

/**
 * Función normal (no computed) a propósito: el buscador es un caso simple que no
 * necesita caché. Se llama directamente desde el v-for del <template>; como ahí lee
 * "busqueda.value" y "props.pizzas", Vue igual la vuelve a ejecutar cada vez que
 * cualquiera de los dos cambia, sin que haga falta "computed" ni nada más elaborado.
 */
function pizzasFiltradas() {
  const texto = busqueda.value.trim().toLowerCase()
  if (!texto) return props.pizzas

  return props.pizzas.filter((pizza) => {
    const nombre = pizza.nombre.toLowerCase()
    const descripcion = (pizza.descripcion || '').toLowerCase()
    return nombre.includes(texto) || descripcion.includes(texto)
  })
}

// Misma función que PizzaCard.vue: se repite aquí (en vez de importarla de algún lado)
// porque son solo dos líneas y así cada componente se explica solo, sin que haya que
// saltar a otro archivo para entender de dónde sale el "$32.000".
function formatearPrecio(precio) {
  return '$' + precio.toLocaleString('es-CO')
}

// Sí conviene "computed" aquí (a diferencia del buscador): es un número que se muestra
// en pantalla y SIEMPRE se calcula sobre el catálogo completo (no sobre el filtro de
// búsqueda), así que depende solo de "pizzas", no de nada que cambie en cada letra que
// alguien escriba en el buscador.
const precioPromedio = computed(() => {
  if (props.pizzas.length === 0) return 0
  const suma = props.pizzas.reduce((total, pizza) => total + pizza.precio, 0)
  return Math.round(suma / props.pizzas.length)
})
</script>

<template>
  <div class="pizza-tabla">
    <!-- Barra superior: título de la sección + buscador. Se muestra siempre (incluso con
         la lista vacía o el filtro sin resultados) para que se pueda borrar la búsqueda
         sin tener que recargar nada. -->
    <div class="pizza-tabla__barra">
      <div class="pizza-tabla__barra-titulo">
        <span class="pizza-tabla__barra-nombre">Catálogo de horno</span>
        <span class="pizza-tabla__barra-sub">• Recetas activas</span>
      </div>
      <div class="pizza-tabla__buscador">
        <span class="material-symbols-outlined pizza-tabla__buscador-icono">search</span>
        <input
          v-model="busqueda"
          type="text"
          placeholder="Filtrar por nombre o ingrediente..."
          class="pizza-tabla__buscador-input"
        />
      </div>
    </div>

    <!-- Mismo estado vacío tanto si no hay NINGUNA pizza como si la búsqueda no encontró
         nada: en los dos casos la tabla no tiene filas que mostrar, así que no hace falta
         un segundo diseño para un caso que, visualmente, es el mismo. -->
    <div v-if="pizzasFiltradas().length === 0" class="pizza-tabla__vacio">
      <div class="pizza-tabla__vacio-icono">
        <span class="material-symbols-outlined">bakery_dining</span>
        <span class="pizza-tabla__vacio-icono-insignia">!</span>
      </div>
      <h3>Aún no hay pizzas registradas</h3>
      <p>Comienza agregando las recetas de la casa para que tus clientes puedan ordenar.</p>
      <button type="button" class="pizza-tabla__vacio-boton" @click="emit('agregar')">
        <span class="material-symbols-outlined">add_circle</span>
        <span>Agregar pizza</span>
      </button>
    </div>

    <!-- overflow-x: auto en el contenedor (ver <style>) deja que la TABLA se desplace
         de lado en pantallas angostas, en vez de desbordar y romper el diseño de toda
         la página. -->
    <div v-else class="pizza-tabla__scroll">
      <table class="pizza-tabla__tabla">
        <thead>
          <tr>
            <th class="pizza-tabla__col-foto">Foto</th>
            <th class="pizza-tabla__col-nombre">Nombre</th>
            <th>Descripción</th>
            <th class="pizza-tabla__col-precio">Precio</th>
            <th class="pizza-tabla__col-acciones">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="pizza in pizzasFiltradas()"
            :key="pizza.id"
            class="pizza-tabla__fila"
            :class="{ 'pizza-tabla__fila--editando': pizza.id === idEditando }"
          >
            <td class="pizza-tabla__col-foto">
              <div class="pizza-tabla__foto-marco">
                <div v-if="!pizza.imagen" class="pizza-tabla__sin-foto">Sin foto</div>
                <img v-else :src="`/images/pizzas/${pizza.imagen}`" :alt="pizza.nombre" class="pizza-tabla__foto" />
              </div>
            </td>
            <td class="pizza-tabla__col-nombre">
              <div class="pizza-tabla__nombre-fila">
                <span class="pizza-tabla__nombre">{{ pizza.nombre }}</span>
                <span
                  v-if="pizza.id === idEditando"
                  class="pizza-tabla__punto"
                  title="En edición actual"
                ></span>
              </div>
              <span v-if="pizza.id === idEditando" class="pizza-tabla__etiqueta-editando">Editando</span>
            </td>
            <td class="pizza-tabla__descripcion-celda">
              <p class="pizza-tabla__descripcion" :title="pizza.descripcion">{{ pizza.descripcion }}</p>
            </td>
            <td class="pizza-tabla__col-precio pizza-tabla__precio">{{ formatearPrecio(pizza.precio) }}</td>
            <td class="pizza-tabla__col-acciones">
              <div class="pizza-tabla__acciones">
                <button
                  type="button"
                  class="pizza-tabla__editar"
                  :class="{ 'pizza-tabla__editar--activo': pizza.id === idEditando }"
                  title="Editar receta"
                  @click="emit('editar', pizza)"
                >
                  <span class="material-symbols-outlined">edit</span>
                </button>
                <button
                  type="button"
                  class="pizza-tabla__eliminar"
                  title="Eliminar pizza"
                  @click="emit('eliminar', pizza)"
                >
                  <span class="material-symbols-outlined">delete</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pizza-tabla__pie">
        <span class="pizza-tabla__pie-promedio">
          Promedio menú: <strong>{{ formatearPrecio(precioPromedio) }}</strong>
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pizza-tabla {
  display: flex;
  flex-direction: column;
  background-color: var(--surface-container-lowest);
  border-radius: var(--radius-xl);
  box-shadow: 0 1px 3px rgba(44, 24, 16, 0.08);
  overflow: hidden;
  font-family: var(--font-body);
}

.pizza-tabla__barra {
  padding: var(--space-md);
  background-color: var(--surface-container-low);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: space-between;
  gap: var(--space-sm);
}

@media (min-width: 640px) {
  .pizza-tabla__barra {
    flex-direction: row;
    align-items: center;
  }
}

.pizza-tabla__barra-titulo {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex-wrap: wrap;
}

.pizza-tabla__barra-nombre {
  font: var(--fw-label-lg) var(--fs-label-lg)/var(--lh-label-lg) var(--font-body);
  color: var(--on-surface);
}

.pizza-tabla__barra-sub {
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-tabla__buscador {
  position: relative;
  width: 100%;
}

@media (min-width: 640px) {
  .pizza-tabla__buscador {
    width: 15rem;
  }
}

.pizza-tabla__buscador-icono {
  position: absolute;
  left: 0.6rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--on-surface-variant);
  font-size: 18px !important;
}

.pizza-tabla__buscador-input {
  width: 100%;
  padding: 0.4rem 0.75rem 0.4rem 2.25rem;
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--surface-container-lowest);
  color: var(--on-surface);
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
}

.pizza-tabla__buscador-input::placeholder {
  color: rgba(89, 65, 61, 0.5); /* --on-surface-variant al 50% */
}

.pizza-tabla__buscador-input:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(166, 37, 24, 0.2);
}

/* ---- Estado vacío (sin pizzas, o la búsqueda no encontró nada) ---- */
.pizza-tabla__vacio {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-sm);
  padding: var(--space-xl) var(--space-md);
}

.pizza-tabla__vacio-icono {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background-color: var(--surface-container-high);
  color: var(--tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(44, 24, 16, 0.08);
}

.pizza-tabla__vacio-icono .material-symbols-outlined {
  font-size: 36px;
}

.pizza-tabla__vacio-icono-insignia {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background-color: var(--primary-fixed);
  color: var(--on-primary-fixed);
  display: flex;
  align-items: center;
  justify-content: center;
  font: 700 12px/1 var(--font-body);
}

.pizza-tabla__vacio h3 {
  margin: var(--space-xs) 0 0;
  font: var(--fw-headline-sm) var(--fs-headline-sm)/var(--lh-headline-sm) var(--font-headline);
  color: var(--on-surface);
}

.pizza-tabla__vacio p {
  margin: 0;
  max-width: 320px;
  font: var(--fw-body-md) var(--fs-body-md)/var(--lh-body-md) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-tabla__vacio-boton {
  margin-top: var(--space-md);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  width: 100%;
  max-width: 260px;
  padding: 0.65rem;
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--primary);
  color: var(--on-primary);
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(166, 37, 24, 0.3);
  transition: background-color 0.15s, transform 0.1s;
}

.pizza-tabla__vacio-boton:active {
  transform: scale(0.95);
}

.pizza-tabla__vacio-boton:hover {
  background-color: var(--primary-container);
}

/* ---- Tabla ---- */
.pizza-tabla__scroll {
  overflow-x: auto;
}

.pizza-tabla__tabla {
  width: 100%;
  min-width: 620px; /* por debajo de este ancho, mejor que se desplace que se aplaste */
  border-collapse: collapse;
  text-align: left;
}

.pizza-tabla__tabla thead tr {
  background-color: rgba(255, 226, 216, 0.6); /* --surface-container-high al 60% */
}

.pizza-tabla__tabla thead th {
  padding: var(--space-sm) var(--space-md);
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--on-surface-variant);
}

.pizza-tabla__col-foto {
  width: 64px;
  text-align: center;
}

.pizza-tabla__col-nombre {
  width: 144px;
}

.pizza-tabla__col-precio {
  width: 112px;
  text-align: right;
}

.pizza-tabla__col-acciones {
  width: 112px;
  text-align: center;
}

.pizza-tabla__fila {
  border-bottom: 1px solid var(--surface-container);
  transition: background-color 0.15s;
}

.pizza-tabla__fila:hover {
  background-color: var(--surface-container-low);
}

/* bg-surface-container-highest/40 de la fila que se está editando: se marca distinto a
   un simple ":hover" para que se note incluso si el mouse no está encima. */
.pizza-tabla__fila--editando {
  background-color: rgba(255, 219, 206, 0.4);
}

.pizza-tabla__fila--editando:hover {
  background-color: rgba(255, 219, 206, 0.55);
}

.pizza-tabla__tabla tbody td {
  padding: var(--space-sm) var(--space-md);
  vertical-align: middle;
}

.pizza-tabla__foto-marco {
  width: 48px;
  height: 48px;
  margin: 0 auto;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(44, 24, 16, 0.1);
}

.pizza-tabla__foto,
.pizza-tabla__sin-foto {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.pizza-tabla__fila:hover .pizza-tabla__foto {
  transform: scale(1.05);
}

.pizza-tabla__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--inverse-surface);
  color: var(--inverse-on-surface);
  font-size: 9px;
  text-align: center;
}

.pizza-tabla__nombre-fila {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.pizza-tabla__nombre {
  font: var(--fw-label-lg) var(--fs-label-lg)/var(--lh-label-lg) var(--font-body);
  color: var(--on-surface);
  font-weight: 700;
}

.pizza-tabla__punto {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background-color: var(--primary);
  flex-shrink: 0;
}

.pizza-tabla__etiqueta-editando {
  display: block;
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  text-transform: uppercase;
  color: var(--primary);
}

.pizza-tabla__descripcion-celda {
  min-width: 0;
  max-width: 220px;
}

.pizza-tabla__descripcion {
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* corta el texto y pone "..." si no cabe en una sola línea */
  color: var(--on-surface-variant);
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
}

.pizza-tabla__precio {
  font: 700 15px/1.2 var(--font-body);
  color: var(--on-surface);
}

.pizza-tabla__acciones {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
}

.pizza-tabla__acciones button {
  padding: 0.35rem;
  border: none;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;
}

.pizza-tabla__acciones .material-symbols-outlined {
  font-size: 18px;
}

.pizza-tabla__editar {
  background-color: var(--surface-container);
  color: var(--on-surface-variant);
}

.pizza-tabla__editar:hover {
  background-color: var(--surface-container-high);
}

/* El botón de editar de la fila que ya está en edición se tiñe de rojo tomate, igual
   que en el diseño: refuerza cuál es la fila activa incluso mirando solo esa columna. */
.pizza-tabla__editar--activo {
  background-color: var(--primary-fixed);
  color: var(--on-primary-fixed);
}

.pizza-tabla__editar--activo:hover {
  background-color: var(--primary);
  color: var(--on-primary);
}

.pizza-tabla__eliminar {
  background-color: var(--surface-container);
  color: var(--error);
}

.pizza-tabla__eliminar:hover {
  background-color: rgba(186, 26, 26, 0.15); /* --error al 15% */
}

.pizza-tabla__pie {
  padding: var(--space-md);
  background-color: var(--surface-container-lowest);
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid var(--surface-container);
}

.pizza-tabla__pie-promedio {
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-surface);
}

.pizza-tabla__pie-promedio strong {
  color: var(--primary);
}
</style>
