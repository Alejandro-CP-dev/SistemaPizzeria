<script setup>
import { ref } from 'vue'
import PizzaCard from './PizzaCard.vue'

// Las imágenes que ya existen en public/images/pizzas/ (ver backend-java/database/pizzeria.sql).
const imagenesDisponibles = [
  'hawaiana.jpg',
  'pepperoni.jpg',
  'mexicana.jpg',
  'carnes.jpg',
  'costillas.jpg',
  'pollo-champinon.jpg',
]

const props = defineProps({
  // null -> se está creando una pizza nueva. Un objeto -> se está editando esa pizza.
  pizza: { type: Object, default: null },
})

const emit = defineEmits(['guardar', 'cancelar'])

/** Arma el objeto inicial del formulario: vacío para crear, o una COPIA de "pizza" para editar. */
function crearCopia() {
  if (props.pizza) {
    // "..." (spread) copia cada campo a un objeto NUEVO. Si en cambio escribiéramos
    // "copia = props.pizza", "copia" sería la MISMA caja que la prop, y escribir en un
    // <input> con v-model="copia.nombre" estaría modificando la prop por dentro.
    return { ...props.pizza }
  }
  return { id: null, nombre: '', descripcion: '', precio: null, imagen: '' }
}

// En Vue, una prop le pertenece al componente PADRE: el hijo solo la "pide prestada"
// para leerla. Si PizzaForm modificara props.pizza directamente, el padre (AdminView)
// vería cambiar sus datos sin haber hecho nada él mismo, y Vue ni siquiera garantiza
// que ese cambio se refleje en pantalla de forma confiable. Por eso se trabaja siempre
// sobre "copia": un ref propio de este componente, separado de la prop.
const copia = ref(crearCopia())

const errores = ref({ nombre: '', precio: '' })

function validar() {
  errores.value = { nombre: '', precio: '' }
  let esValido = true

  if (!copia.value.nombre || !copia.value.nombre.trim()) {
    errores.value.nombre = 'El nombre es obligatorio.'
    esValido = false
  }

  if (!copia.value.precio || copia.value.precio <= 0) {
    errores.value.precio = 'El precio debe ser mayor que 0.'
    esValido = false
  }

  return esValido
}

function guardar() {
  if (!validar()) {
    return // No se emite nada: AdminView nunca se entera de un formulario inválido.
  }
  emit('guardar', { ...copia.value })
}
</script>

<template>
  <div class="pizza-form">
    <form class="pizza-form__campos" @submit.prevent="guardar">
      <h2>{{ pizza ? 'Editar pizza' : 'Nueva pizza' }}</h2>

      <label class="pizza-form__campo">
        Nombre
        <input v-model="copia.nombre" maxlength="50" />
        <small>{{ copia.nombre.length }} / 50</small>
        <span v-if="errores.nombre" class="pizza-form__error">{{ errores.nombre }}</span>
      </label>

      <label class="pizza-form__campo">
        Descripción
        <textarea v-model="copia.descripcion" maxlength="200"></textarea>
        <small>{{ (copia.descripcion || '').length }} / 200</small>
      </label>

      <label class="pizza-form__campo">
        Precio
        <!-- v-model.number convierte lo que escribe el usuario (siempre texto) a número;
             sin ".number", copia.precio sería el texto "32000" en vez del número 32000,
             y la vista previa (PizzaCard) fallaría al formatearlo con toLocaleString. -->
        <input type="number" v-model.number="copia.precio" min="0" />
        <span v-if="errores.precio" class="pizza-form__error">{{ errores.precio }}</span>
      </label>

      <label class="pizza-form__campo">
        Imagen
        <select v-model="copia.imagen">
          <option value="">Sin imagen</option>
          <option v-for="img in imagenesDisponibles" :key="img" :value="img">{{ img }}</option>
        </select>
      </label>

      <div class="pizza-form__botones">
        <button type="button" class="pizza-form__cancelar" @click="emit('cancelar')">
          Cancelar
        </button>
        <button type="submit" class="pizza-form__guardar">Guardar</button>
      </div>
    </form>

    <!-- Vista previa en vivo: la MISMA tarjeta que se ve en el menú público, alimentada
         por "copia". Como copia es reactiva, cada letra que se escribe en el formulario
         se refleja aquí de inmediato, sin que haya que hacer nada extra. -->
    <div class="pizza-form__vista-previa">
      <h3>Vista previa en la carta</h3>
      <PizzaCard :pizza="copia" />
    </div>
  </div>
</template>

<style scoped>
.pizza-form {
  display: flex;
  flex-wrap: wrap;
  gap: 2rem;
}

.pizza-form__campos {
  flex: 1 1 280px;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.pizza-form__campos h2 {
  margin: 0;
  color: #a62518;
}

.pizza-form__campo {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  color: #2a170f;
  font-size: 0.9rem;
}

.pizza-form__campo input,
.pizza-form__campo textarea,
.pizza-form__campo select {
  padding: 0.5rem;
  border: 1px solid #2a170f;
  border-radius: 6px;
  font-size: 1rem;
  font-family: inherit;
}

.pizza-form__error {
  color: #a62518;
  font-size: 0.8rem;
}

.pizza-form__botones {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.pizza-form__guardar,
.pizza-form__cancelar {
  flex: 1;
  padding: 0.6rem;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
}

.pizza-form__guardar {
  border: none;
  background-color: #2a6a48;
  color: #fff8f6;
}

.pizza-form__cancelar {
  border: 1px solid #2a170f;
  background-color: transparent;
  color: #2a170f;
}

.pizza-form__vista-previa {
  flex: 1 1 280px;
}

.pizza-form__vista-previa h3 {
  margin-top: 0;
  color: #2a170f;
  font-size: 1rem;
}
</style>
