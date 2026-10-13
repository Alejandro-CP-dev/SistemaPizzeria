<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminHeader from '../components/AdminHeader.vue'
import AdminFooter from '../components/AdminFooter.vue'
import PizzaTabla from '../components/PizzaTabla.vue'
import PizzaForm from '../components/PizzaForm.vue'
import ModalConfirmar from '../components/ModalConfirmar.vue'
import NotificacionToast from '../components/NotificacionToast.vue'

const router = useRouter()

const pizzas = ref([])
const cargando = ref(true)
const error = ref('')
const mensaje = ref('') // texto del toast de éxito; '' = no se muestra (ver NotificacionToast)

// null -> el panel de la derecha está en modo "nueva pizza". Un objeto -> está editando
// esa pizza. A diferencia de la versión anterior (formularioAbierto + un modal), el
// panel del diseño de referencia NUNCA se oculta: siempre hay un formulario visible,
// solo cambia si es de alta o de edición.
const pizzaSeleccionada = ref(null)
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

function abrirCrear() {
  pizzaSeleccionada.value = null
}

function abrirEditar(pizza) {
  pizzaSeleccionada.value = pizza
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

    pizzaSeleccionada.value = null // vuelve a modo "nueva pizza" y quita el resaltado de la tabla
    mensaje.value = esEdicion ? 'Pizza actualizada correctamente' : 'Pizza guardada correctamente'
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
    // Si justo se estaba editando la pizza que se acaba de eliminar, el panel se queda
    // sin con qué trabajar: se vuelve a modo "nueva pizza" para no dejarlo editando un
    // registro que ya no existe.
    if (pizzaSeleccionada.value && pizzaSeleccionada.value.id === pizza.id) {
      pizzaSeleccionada.value = null
    }
    mensaje.value = 'Pizza eliminada'
  } catch (e) {
    console.error(e)
    error.value = 'No se pudo conectar con el servidor.'
  }
}

const textoConteo = computed(() => {
  const cantidad = pizzas.value.length
  return `${cantidad} ${cantidad === 1 ? 'pizza' : 'pizzas'} en el menú`
})
</script>

<template>
  <div class="admin-pagina">
    <AdminHeader />

    <main class="admin">
      <div class="admin__contenido">
        <p v-if="error" class="admin__error">{{ error }}</p>

        <!-- Encabezado de la sección: ícono, título, insignia con el conteo real y el
             punto verde animado, texto descriptivo, píldora de moneda y botón de alta. -->
        <div class="admin__seccion-encabezado">
          <div class="admin__seccion-titulo-bloque">
            <div class="admin__seccion-icono">
              <span class="material-symbols-outlined">local_pizza</span>
            </div>
            <div class="admin__seccion-textos">
              <div class="admin__seccion-titulo-fila">
                <h1 class="admin__seccion-titulo">Gestión de pizzas</h1>
                <span class="admin__insignia-conteo">
                  <span class="admin__punto-animado"></span>
                  {{ textoConteo }}
                </span>
              </div>
              <p class="admin__seccion-descripcion">
                Control artesanal de recetas, precios al consumidor en pesos colombianos y
                fichas de menú.
              </p>
            </div>
          </div>

          <div class="admin__seccion-acciones">
            <span class="admin__moneda">
              <span class="material-symbols-outlined">monetization_on</span>
              <span>Moneda: COP (Sin decimales)</span>
            </span>
            <button type="button" class="admin__agregar" @click="abrirCrear">
              <span class="material-symbols-outlined">add</span>
              <span>Agregar pizza</span>
            </button>
          </div>
        </div>

        <p v-if="cargando" class="admin__cargando">Cargando pizzas...</p>

        <!-- Distribución 7/12 (tabla) + 5/12 (formulario + vista previa) en pantalla
             grande; en celular, una sola columna (ver <style>). -->
        <div v-else class="admin__grid">
          <div class="admin__grid-tabla">
            <PizzaTabla
              :pizzas="pizzas"
              :id-editando="pizzaSeleccionada ? pizzaSeleccionada.id : null"
              @editar="abrirEditar"
              @eliminar="pedirEliminar"
              @agregar="abrirCrear"
            />
          </div>

          <!-- :key fuerza a Vue a destruir y volver a crear PizzaForm cada vez que cambia
               qué se está editando (o se vuelve a modo "nueva pizza"): así su copia local
               (ver PizzaForm.vue) siempre arranca fresca a partir de la prop "pizza" que
               le toque en ese momento, sin necesitar un watcher manual. -->
          <div class="admin__grid-form">
            <PizzaForm
              :key="pizzaSeleccionada ? pizzaSeleccionada.id : 'nueva'"
              :pizza="pizzaSeleccionada"
              @guardar="guardarPizza"
              @cancelar="pizzaSeleccionada = null"
            />
          </div>
        </div>
      </div>
    </main>

    <AdminFooter />

    <ModalConfirmar
      v-if="pizzaAEliminar"
      :nombre="pizzaAEliminar.nombre"
      @confirmar="eliminarConfirmado"
      @cancelar="pizzaAEliminar = null"
    />

    <NotificacionToast v-if="mensaje" :mensaje="mensaje" @cerrar="mensaje = ''" />
  </div>
</template>

<style scoped>
.admin-pagina {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--surface);
}

.admin {
  flex: 1;
  width: 100%;
  /* 80px = la altura fija de AdminHeader (ver AdminHeader.vue): sin este padding, el
     encabezado "flotante" taparía el primer tramo del contenido. */
  padding-top: 80px;
}

.admin__contenido {
  width: 100%;
  padding: var(--space-lg) var(--margin-mobile);
}

@media (min-width: 768px) {
  .admin__contenido {
    padding: var(--space-lg) var(--margin);
  }
}

.admin__error {
  margin: 0 0 var(--space-md);
  padding: var(--space-sm) var(--space-md);
  border-left: 3px solid var(--error);
  background-color: var(--error-container);
  color: var(--on-error-container);
  font: var(--fw-body-md) var(--fs-body-md)/var(--lh-body-md) var(--font-body);
  border-radius: var(--radius);
}

.admin__cargando {
  text-align: center;
  padding: var(--space-xl) 0;
  color: var(--on-surface-variant);
  font: var(--fw-body-md) var(--fs-body-md)/var(--lh-body-md) var(--font-body);
}

/* ---- Encabezado de la sección ---- */
.admin__seccion-encabezado {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

@media (min-width: 768px) {
  .admin__seccion-encabezado {
    flex-direction: row;
    align-items: center;
  }
}

.admin__seccion-titulo-bloque {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.admin__seccion-icono {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-xl);
  background-color: var(--surface-container-high);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(44, 24, 16, 0.08);
}

.admin__seccion-icono .material-symbols-outlined {
  font-size: 28px;
}

.admin__seccion-textos {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin__seccion-titulo-fila {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.admin__seccion-titulo {
  margin: 0;
  font: var(--fw-headline-lg-mobile) var(--fs-headline-lg-mobile)/var(--lh-headline-lg-mobile) var(--font-headline);
  color: var(--on-surface);
  letter-spacing: -0.01em;
}

@media (min-width: 768px) {
  .admin__seccion-titulo {
    font-size: var(--fs-headline-lg);
    line-height: var(--lh-headline-lg);
  }
}

.admin__insignia-conteo {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.125rem var(--space-sm);
  border-radius: var(--radius-full);
  background-color: rgba(42, 106, 72, 0.1); /* --secondary al 10% */
  color: var(--secondary);
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
}

.admin__punto-animado {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  background-color: var(--secondary);
  animation: admin-pulso 1.6s ease-in-out infinite;
}

@keyframes admin-pulso {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.35;
  }
}

.admin__seccion-descripcion {
  margin: 0;
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: var(--on-surface-variant);
}

.admin__seccion-acciones {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-shrink: 0;
}

/* "Moneda: COP" se oculta por debajo de 1280px, igual que el diseño de referencia
   (hidden xl:flex): en pantallas medianas ya no queda espacio cómodo para esta píldora
   junto al botón de agregar. */
.admin__moneda {
  display: none;
  align-items: center;
  gap: 0.3rem;
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-lg);
  background-color: var(--surface-container-low);
  color: var(--on-surface-variant);
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  white-space: nowrap;
}

.admin__moneda .material-symbols-outlined {
  font-size: 18px;
  color: var(--tertiary);
}

@media (min-width: 1280px) {
  .admin__moneda {
    display: flex;
  }
}

.admin__agregar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: var(--space-sm) var(--space-lg);
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--primary);
  color: var(--on-primary);
  font: var(--fw-label-lg) var(--fs-label-lg)/var(--lh-label-lg) var(--font-body);
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(166, 37, 24, 0.3);
  transition: background-color 0.15s, transform 0.1s;
}

.admin__agregar .material-symbols-outlined {
  font-size: 18px;
}

.admin__agregar:active {
  transform: scale(0.95);
}

.admin__agregar:hover {
  background-color: var(--primary-container);
}

/* ---- Distribución principal: tabla + formulario ---- */
.admin__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--gutter-mobile);
  align-items: start;
}

@media (min-width: 1024px) {
  .admin__grid {
    grid-template-columns: repeat(12, 1fr);
    gap: var(--gutter);
  }

  .admin__grid-tabla {
    grid-column: span 7;
  }

  .admin__grid-form {
    grid-column: span 5;
  }
}
</style>
