<script>
// Funciones y constantes puras en un <script> normal (no reactivo): igual que hace
// CircularCarousel.vue, viven fuera del <script setup> para que NO se vuelvan a crear
// en cada instancia del componente (en esta página solo hay una, pero es la misma
// razón por la que en React estas cosas se sacan del cuerpo del componente).

// Cuanto más lejos del centro queda una pizza, más chica y más transparente se ve.
// "distancia" puede ser fraccionaria (ej. 0.4) mientras se arrastra a medio camino
// entre dos pizzas, no solo valores enteros.
const ESCALA_MINIMA = 0.72
const OPACIDAD_MINIMA = 0.35

function escalaPorDistancia(distancia) {
  return Math.max(ESCALA_MINIMA, 1 - 0.22 * Math.abs(distancia))
}

function opacidadPorDistancia(distancia) {
  return Math.max(OPACIDAD_MINIMA, 1 - 0.45 * Math.abs(distancia))
}

// $32.000 en vez de 32000: misma idea que formatearPrecio() en PizzaCard.vue.
function formatearPrecio(precio) {
  return '$' + Number(precio || 0).toLocaleString('es-CO')
}

function limitar(valor, minimo, maximo) {
  return Math.min(maximo, Math.max(minimo, valor))
}
</script>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import gsap from 'gsap'

const props = defineProps({
  pizzas: { type: Array, default: () => [] },
})

// El carrusel ignora las pizzas sin foto: no tiene sentido abrir un "interior" de una
// pizza que no tiene imagen para cortar en rebanadas.
const cartas = computed(() => props.pizzas.filter((pizza) => pizza.imagen))
const total = computed(() => cartas.value.length)

// --- Estado del carrusel ------------------------------------------------------

// Índice entero de la pizza "activa" (centrada, quieta). Es lo único que usan el
// título y el precio de abajo: no les importa la posición exacta durante el
// arrastre, solo "en cuál pizza nos detuvimos".
const activa = ref(0)

// Posición continua (puede tener decimales) de la cinta de pizzas, en "unidades de
// tarjeta": 2.35 significa "a medio camino entre la tarjeta 2 y la 3, más cerca de
// la 2". Mientras no se arrastra ni hay inercia en curso, vale lo mismo que
// "activa". Se anima con GSAP (no con una animación nuestra a mano) porque así el
// freno suave de la inercia y el de "ir a la tarjeta 3 con las flechas" usan
// exactamente el mismo mecanismo: un tween con una curva de desaceleración (ease).
const posicion = ref(0)
const arrastrando = ref(false)

const raiz = ref(null)
const pistaEl = ref(null)
const cartaEls = ref([])
function guardarCartaEl(el, indice) {
  if (el) cartaEls.value[indice] = el
}

// Ancho real de una tarjeta + su separación, medido del DOM (no un número fijo a
// mano) para que el cálculo de arrastre/inercia siga siendo correcto aunque el CSS
// cambie el tamaño de las tarjetas en pantallas angostas (@media abajo).
const anchoTarjeta = ref(240)
const anchoContenedor = ref(960)

function medir() {
  const primeraCarta = cartaEls.value[0]
  if (primeraCarta) anchoTarjeta.value = primeraCarta.getBoundingClientRect().width
  if (raiz.value) anchoContenedor.value = raiz.value.getBoundingClientRect().width
}

// Traslado en píxeles de toda la cinta: centra la tarjeta "posicion" restando la
// mitad de su ancho y usando la mitad del contenedor como punto central.
const trasladoPx = computed(
  () => anchoContenedor.value / 2 - anchoTarjeta.value / 2 - posicion.value * anchoTarjeta.value
)

function estiloCarta(indice) {
  const distancia = indice - posicion.value
  return {
    transform: `scale(${escalaPorDistancia(distancia)})`,
    opacity: opacidadPorDistancia(distancia),
  }
}

// --- Reducir movimiento --------------------------------------------------------

const prefiereReducido = ref(false)
let mediaReducido = null
function actualizarReducido() {
  prefiereReducido.value = !!mediaReducido?.matches
}

// --- Animación con GSAP ---------------------------------------------------------

// gsap.context() agrupa TODAS las animaciones creadas dentro (o añadidas después con
// ctx.add) para poder deshacerlas de un solo golpe con ctx.revert(). Es necesario
// porque esta vista es una SPA: si la persona entra a "/login" y vuelve, Vue destruye
// y vuelve a crear este componente, pero un tween de GSAP que haya quedado animando
// (por ejemplo, la inercia todavía frenando) no se entera solo de que el componente
// desapareció. Sin revert(), ese tween seguiría corriendo en segundo plano y, peor,
// seguiría escribiendo en "posicion.value" de una instancia de Vue que ya no existe.
let ctx = null
let tweenPosicion = null

function irA(objetivo, { duracion = 0.5 } = {}) {
  const destino = limitar(objetivo, 0, total.value - 1)
  tweenPosicion?.kill()
  if (prefiereReducido.value) {
    // Con "reducir movimiento" activado no hay freno ni deslizamiento: el cambio es
    // inmediato (duration: 0 sigue pasando por GSAP para no duplicar el onComplete).
    tweenPosicion = gsap.to(posicion, { value: destino, duration: 0, onComplete: () => (activa.value = destino) })
    return
  }
  ctx.add(() => {
    tweenPosicion = gsap.to(posicion, {
      value: destino,
      duration: duracion,
      ease: 'power3.out', // arranca rápido y frena suave: así se siente la "llegada" a la tarjeta
      onComplete: () => {
        activa.value = destino
      },
    })
  })
}

function siguiente() {
  // "% total.value" hace que, al pasar de la última pizza (índice total-1), el
  // resultado vuelva a 0 en vez de seguir creciendo: (total-1+1) % total = 0.
  irA((activa.value + 1) % total.value)
}

function anterior() {
  // Sumar "+ total.value" antes del módulo evita un resultado negativo en JavaScript:
  // (0 - 1) % total daría -1, no total-1. Con la suma, (0 - 1 + total) % total sí da
  // total-1, es decir, la última pizza.
  irA((activa.value - 1 + total.value) % total.value)
}

function alHacerClicEnCarta(indice) {
  if (indice !== activa.value) {
    irA(indice)
    return
  }
  abrirDetalle()
}

// Se completa cuando se agrega el panel de detalle (abre la pizza en rebanadas).
function abrirDetalle() {}

// --- Arrastre con el mouse y con el dedo (Pointer Events cubre ambos casos) -----

let inicio = null // { x, posicionInicial, muestras: [{t, posicion}] }

function alPresionar(evento) {
  if (evento.button !== undefined && evento.button !== 0) return
  tweenPosicion?.kill()
  arrastrando.value = true
  inicio = {
    x: evento.clientX,
    posicionInicial: posicion.value,
    muestras: [{ t: performance.now(), posicion: posicion.value }],
  }
  raiz.value?.setPointerCapture?.(evento.pointerId)
}

function alMover(evento) {
  if (!inicio) return
  const deltaPx = evento.clientX - inicio.x
  // Arrastrar hacia la izquierda (deltaPx negativo) debe AVANZAR el índice (como si
  // empujáramos las tarjetas hacia la izquierda para traer la siguiente al centro);
  // por eso se resta, no se suma.
  let nueva = inicio.posicionInicial - deltaPx / anchoTarjeta.value
  // "Resorte" en los extremos: más allá de la primera o la última pizza, el
  // movimiento se frena a un 30% en vez de permitir arrastrar libremente al vacío.
  if (nueva < 0) nueva *= 0.3
  if (nueva > total.value - 1) nueva = total.value - 1 + (nueva - (total.value - 1)) * 0.3
  posicion.value = nueva

  const ahora = performance.now()
  inicio.muestras.push({ t: ahora, posicion: nueva })
  // Solo se conservan las muestras de los últimos ~100ms: la velocidad de soltar debe
  // reflejar el gesto final de la persona, no todo el arrastre desde el principio.
  while (inicio.muestras.length > 2 && ahora - inicio.muestras[0].t > 100) inicio.muestras.shift()
}

function alSoltar() {
  if (!inicio) return
  arrastrando.value = false
  const primera = inicio.muestras[0]
  const ultima = inicio.muestras[inicio.muestras.length - 1]
  const segundos = (ultima.t - primera.t) / 1000
  // Velocidad en "tarjetas por segundo": cuánto cambió "posicion" entre las dos
  // últimas muestras, dividido por cuánto tiempo pasó. Si el gesto fue demasiado
  // corto (menos de 8ms) se descarta para no dividir por un número casi cero.
  const velocidad = segundos > 0.008 ? (ultima.posicion - primera.posicion) / segundos : 0
  inicio = null

  if (prefiereReducido.value) {
    irA(Math.round(posicion.value))
    return
  }

  // Proyección de la frenada: si la persona suelta moviéndose rápido, la pizza no se
  // detiene en la tarjeta más cercana AHORA, sino en la más cercana a donde habría
  // llegado "deslizando" un poco más por la inercia. 0.18 es ese "poco más": a mayor
  // velocidad, más lejos cae el objetivo proyectado.
  const objetivoProyectado = posicion.value + velocidad * 0.18
  const objetivo = Math.round(limitar(objetivoProyectado, 0, total.value - 1))
  // La duración del frenado depende de qué tan lejos quedó esa tarjeta: cerca (ya
  // casi detenida) frena rápido; lejos (se soltó con mucho impulso) tarda más en
  // asentarse, para que no se sienta como un salto brusco.
  const distancia = Math.abs(objetivo - posicion.value)
  irA(objetivo, { duracion: limitar(0.25 + distancia * 0.15, 0.25, 0.9) })
}

// --- Teclado -------------------------------------------------------------------

function alPresionarTecla(evento) {
  if (evento.key === 'ArrowRight') {
    siguiente()
    evento.preventDefault()
  } else if (evento.key === 'ArrowLeft') {
    anterior()
    evento.preventDefault()
  }
}

// --- Ciclo de vida ---------------------------------------------------------------

let observador = null

onMounted(async () => {
  mediaReducido = window.matchMedia?.('(prefers-reduced-motion: reduce)') || null
  actualizarReducido()
  mediaReducido?.addEventListener?.('change', actualizarReducido)

  await nextTick() // espera a que v-for dibuje las tarjetas antes de medirlas
  ctx = gsap.context(() => {}, raiz.value)
  medir()
  posicion.value = activa.value

  observador = new ResizeObserver(medir)
  if (raiz.value) observador.observe(raiz.value)
})

onUnmounted(() => {
  mediaReducido?.removeEventListener?.('change', actualizarReducido)
  observador?.disconnect()
  tweenPosicion?.kill()
  ctx?.revert()
})
</script>

<template>
  <section v-if="total" id="carta" class="menu-interactivo" ref="raiz">
    <div class="menu-interactivo__encabezado">
      <p class="menu-interactivo__ojo">Para abrir boca</p>
      <h2 class="menu-interactivo__titulo-seccion">La carta en movimiento</h2>
    </div>

    <div
      class="menu-interactivo__carrusel"
      role="region"
      aria-roledescription="carrusel"
      aria-label="Carrusel de pizzas"
      tabindex="0"
      :data-arrastrando="arrastrando ? '' : null"
      @pointerdown="alPresionar"
      @pointermove="alMover"
      @pointerup="alSoltar"
      @pointercancel="alSoltar"
      @keydown="alPresionarTecla"
    >
      <button
        type="button"
        class="menu-interactivo__flecha menu-interactivo__flecha--izq"
        aria-label="Pizza anterior"
        @click="anterior"
      >
        ‹
      </button>

      <div class="menu-interactivo__pista" ref="pistaEl" :style="{ transform: `translateX(${trasladoPx}px)` }">
        <button
          v-for="(pizza, indice) in cartas"
          :key="pizza.id"
          type="button"
          class="menu-interactivo__carta"
          :class="{ 'menu-interactivo__carta--activa': indice === activa }"
          :ref="(el) => guardarCartaEl(el, indice)"
          :style="estiloCarta(indice)"
          :aria-current="indice === activa ? 'true' : null"
          :aria-label="indice === activa ? `Ver por dentro ${pizza.nombre}` : `Ir a ${pizza.nombre}`"
          @click="alHacerClicEnCarta(indice)"
        >
          <img :src="`/images/pizzas/${pizza.imagen}`" :alt="pizza.nombre" class="menu-interactivo__foto" draggable="false" />
        </button>
      </div>

      <button
        type="button"
        class="menu-interactivo__flecha menu-interactivo__flecha--der"
        aria-label="Pizza siguiente"
        @click="siguiente"
      >
        ›
      </button>
    </div>
  </section>
</template>

<style scoped>
/* Reutiliza los colores de marca de tokens.css (coinciden con el rojo/crema que ya
   usa el resto del sitio público) pero NO sus fuentes: Merriweather/Plus Jakarta Sans
   son solo del panel de admin. Marcellus/Baloo 2/Figtree siguen escritas literal,
   igual que en NavBar.vue y MenuPizzas.vue, porque tokens.css no las define. */
.menu-interactivo {
  background-color: #1b110a;
  padding: 5rem 1.5rem 4rem;
  text-align: center;
  scroll-margin-top: 80px;
}

.menu-interactivo__encabezado {
  margin-bottom: 2.5rem;
}

.menu-interactivo__ojo {
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--primary);
  font-weight: 600;
  margin: 0 0 0.75rem;
  font-family: 'Figtree', system-ui, sans-serif;
}

.menu-interactivo__titulo-seccion {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 2.4rem;
  color: #fff8f6;
  margin: 0;
}

.menu-interactivo__carrusel {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  max-width: 1100px;
  height: 320px;
  margin: 0 auto;
  overflow: hidden;
  touch-action: pan-y; /* deja que el dedo siga arrastrando horizontal sin bloquear el scroll vertical de la página */
  outline: none;
}

.menu-interactivo__carrusel:focus-visible {
  box-shadow: inset 0 0 0 2px rgba(255, 248, 246, 0.4);
}

.menu-interactivo__carrusel[data-arrastrando] {
  cursor: grabbing;
}

.menu-interactivo__pista {
  position: relative;
  flex: 1;
  height: 100%;
}

.menu-interactivo__carta {
  position: absolute;
  top: 0;
  left: 0;
  width: 220px;
  height: 100%;
  padding: 0;
  border: none;
  background: none;
  cursor: grab;
  /* El "scale" y "opacity" de estiloCarta() ya mueven la tarjeta visualmente; esta
     transición solo suaviza el cambio de escala/opacidad entre fotogramas cuando NO
     está siendo arrastrada (al soltar, por ejemplo), sin interferir con el translateX
     de la pista, que ya anima GSAP. */
  transition: filter 0.2s;
}

.menu-interactivo__carta--activa {
  cursor: pointer;
}

.menu-interactivo__foto {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 999px; /* las fotos de pizza del catálogo ya vienen recortadas en círculo */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.45);
  pointer-events: none; /* el click lo maneja el <button> que envuelve la foto, no la imagen */
  -webkit-user-drag: none;
}

.menu-interactivo__flecha {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: 1px solid rgba(255, 248, 246, 0.35);
  background-color: transparent;
  color: #fff8f6;
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  z-index: 1;
}

.menu-interactivo__flecha:hover {
  background-color: var(--primary);
  border-color: var(--primary);
}

@media (max-width: 560px) {
  .menu-interactivo__carrusel {
    height: 240px;
  }

  .menu-interactivo__carta {
    width: 160px;
  }

  .menu-interactivo__flecha {
    width: 36px;
    height: 36px;
    font-size: 1.25rem;
  }
}
</style>
