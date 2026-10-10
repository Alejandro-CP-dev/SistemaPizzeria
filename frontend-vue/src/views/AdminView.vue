<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PizzaTabla from '../components/PizzaTabla.vue'
import PizzaForm from '../components/PizzaForm.vue'
import ModalConfirmar from '../components/ModalConfirmar.vue'

const router = useRouter()

const pizzas = ref([])
const cargando = ref(true)
const error = ref('')
const mensaje = ref('') // mensaje corto de éxito, se borra solo (ver avisar())

// null -> el formulario está cerrado. "nueva" -> abierto para crear.
// Un objeto pizza -> abierto para editar esa pizza.
const formularioAbierto = ref(null)
const pizzaAEliminar = ref(null)

async function cargarPizzas() {
  cargando.value = true
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

/** Muestra "texto" un momento y luego lo borra solo, sin que el usuario tenga que cerrarlo. */
function avisar(texto) {
  mensaje.value = texto
  setTimeout(() => {
    mensaje.value = ''
  }, 2500)
}

function abrirCrear() {
  formularioAbierto.value = 'nueva'
}

function abrirEditar(pizza) {
  formularioAbierto.value = pizza
}

/**
 * Revisa el status de una respuesta que falló y decide qué hacer: si es 401 (la sesión
 * se venció mientras se usaba el panel), manda a /login; si es 400, muestra el mensaje
 * de validación que mandó el Servlet. Se usa igual para guardar y para eliminar.
 */
async function manejarFallo(respuesta) {
  if (respuesta.status === 401) {
    router.push('/login')
    return
  }
  const cuerpo = await respuesta.json().catch(() => null)
  error.value = cuerpo && cuerpo.error ? cuerpo.error : 'Ocurrió un error inesperado.'
}

async function guardarPizza(datos) {
  error.value = ''
  // El id solo existe si se estaba editando: así se decide entre crear o actualizar.
  const esEdicion = Boolean(datos.id)
  const url = esEdicion ? `/api/pizzas/${datos.id}` : '/api/pizzas'

  try {
    const respuesta = await fetch(url, {
      method: esEdicion ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datos),
    })

    if (!respuesta.ok) {
      await manejarFallo(respuesta)
      return
    }

    const pizzaGuardada = await respuesta.json()
    if (esEdicion) {
      const indice = pizzas.value.findIndex((p) => p.id === pizzaGuardada.id)
      pizzas.value[indice] = pizzaGuardada
    } else {
      pizzas.value.push(pizzaGuardada)
    }

    formularioAbierto.value = null
    avisar(esEdicion ? 'Pizza actualizada' : 'Pizza guardada')
  } catch (e) {
    console.error(e)
    error.value = 'No se pudo conectar con el servidor.'
  }
}

function pedirEliminar(pizza) {
  pizzaAEliminar.value = pizza
}

async function eliminarConfirmado() {
  const pizza = pizzaAEliminar.value
  pizzaAEliminar.value = null
  error.value = ''

  try {
    const respuesta = await fetch(`/api/pizzas/${pizza.id}`, { method: 'DELETE' })
    if (!respuesta.ok) {
      await manejarFallo(respuesta)
      return
    }
    pizzas.value = pizzas.value.filter((p) => p.id !== pizza.id)
    avisar('Pizza eliminada')
  } catch (e) {
    console.error(e)
    error.value = 'No se pudo conectar con el servidor.'
  }
}
</script>

<template>
  <section class="admin">
    <div class="admin__encabezado">
      <h1>Panel de administración</h1>
      <button type="button" class="admin__agregar" @click="abrirCrear">Agregar pizza</button>
    </div>

    <p v-if="mensaje" class="admin__mensaje">{{ mensaje }}</p>
    <p v-if="error" class="admin__error">{{ error }}</p>

    <p v-if="cargando">Cargando pizzas...</p>
    <PizzaTabla
      v-else
      :pizzas="pizzas"
      @editar="abrirEditar"
      @eliminar="pedirEliminar"
      @agregar="abrirCrear"
    />

    <!-- v-if (no v-show) a propósito: así cada vez que se abre el formulario es un
         PizzaForm NUEVO, que arranca su copia local desde cero a partir de la prop
         "pizza" que le llegue en ese momento (null para crear, el objeto para editar).
         Si se reutilizara la misma instancia, habría que vigilar manualmente cuándo
         cambia la prop para resetear su estado interno. -->
    <div v-if="formularioAbierto" class="admin__modal" @click.self="formularioAbierto = null">
      <div class="admin__modal-contenido">
        <PizzaForm
          :pizza="formularioAbierto === 'nueva' ? null : formularioAbierto"
          @guardar="guardarPizza"
          @cancelar="formularioAbierto = null"
        />
      </div>
    </div>

    <ModalConfirmar
      v-if="pizzaAEliminar"
      :nombre="pizzaAEliminar.nombre"
      @confirmar="eliminarConfirmado"
      @cancelar="pizzaAEliminar = null"
    />
  </section>
</template>

<style scoped>
.admin {
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.admin__encabezado {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.admin__encabezado h1 {
  color: #a62518;
  margin: 0;
}

.admin__agregar {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 6px;
  background-color: #2a6a48;
  color: #fff8f6;
  cursor: pointer;
}

.admin__mensaje {
  background-color: #2a6a48;
  color: #fff8f6;
  padding: 0.5rem 1rem;
  border-radius: 6px;
}

.admin__error {
  background-color: #a62518;
  color: #fff8f6;
  padding: 0.5rem 1rem;
  border-radius: 6px;
}

.admin__modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(42, 23, 15, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 10;
  overflow-y: auto;
}

.admin__modal-contenido {
  background-color: #fff8f6;
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 700px;
  width: 100%;
}
</style>
