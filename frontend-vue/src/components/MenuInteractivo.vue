<script>
// Funciones y constantes puras en un <script> normal (no reactivo): viven fuera del
// <script setup> para que NO se vuelvan a crear en cada instancia del componente (en
// esta página solo hay una, pero es la misma razón por la que en React estas cosas se
// sacan del cuerpo del componente).

// Cuanto más lejos del centro queda una pizza, más chica y más transparente se ve.
// "distancia" puede ser fraccionaria (ej. 0.4) mientras se arrastra a medio camino
// entre dos pizzas, no solo valores enteros.
const ESCALA_MINIMA = 0.72
const OPACIDAD_MINIMA = 0.35

// Separación horizontal entre el borde de una tarjeta y la siguiente. Las tarjetas
// son position:absolute (para poder deslizarlas con un solo número, "posicion", en
// vez de mover 6 elementos distintos), así que CSS "gap" no les sirve: cada una debe
// decir explícitamente a cuántos píxeles de la tarjeta 0 le toca pararse.
const SEPARACION = 28

function escalaPorDistancia(distancia) {
  return Math.max(ESCALA_MINIMA, 1 - 0.22 * Math.abs(distancia))
}

function opacidadPorDistancia(distancia) {
  return Math.max(OPACIDAD_MINIMA, 1 - 0.45 * Math.abs(distancia))
}

// $32.000 en vez de 32000: misma idea que formatearPrecio() en PizzaCard.vue.
function formatearPrecio(precio) {
  return '$' + Math.round(Number(precio || 0)).toLocaleString('es-CO')
}

function limitar(valor, minimo, maximo) {
  return Math.min(maximo, Math.max(minimo, valor))
}

// "Mozzarella, pepperoni, tomate y oregano" -> ["Mozzarella", "pepperoni", "tomate",
// "oregano"]. Se separa tanto por comas como por la palabra " y " (la que conecta el
// último ingrediente con el resto) porque la descripción se escribió en PizzaForm.vue
// como una frase para leer, no como una lista: el ticket necesita la lista suelta.
function dividirIngredientes(descripcion) {
  return (descripcion || '')
    .split(/,| y /)
    .map((texto) => texto.trim())
    .filter(Boolean)
}

// Punto sobre un círculo de radio "radioPorcentaje" (en % del ancho/alto del elemento),
// a "grados" de ángulo. Se resta 90° al construir cada porción (ver construirPorciones)
// para que la primera tajada empiece arriba, como al cortar una pizza real desde las 12.
function anguloAPunto(grados, radioPorcentaje) {
  const radianes = (grados * Math.PI) / 180
  return { x: 50 + radioPorcentaje * Math.cos(radianes), y: 50 + radioPorcentaje * Math.sin(radianes) }
}

// clip-path no tiene curvas, así que el borde exterior de cada porción (un arco) se
// aproxima con varios segmentos rectos cortos (SEGMENTOS_ARCO por tajada): con 6
// segmentos por cada 45°, el "redondeo" ya se ve liso a simple vista.
const TOTAL_PORCIONES = 8
const SEGMENTOS_ARCO = 6

function clipPorcion(indice) {
  const anguloPorcion = 360 / TOTAL_PORCIONES
  const inicio = indice * anguloPorcion - 90
  const puntos = ['50% 50%']
  for (let s = 0; s <= SEGMENTOS_ARCO; s++) {
    const grados = inicio + (anguloPorcion * s) / SEGMENTOS_ARCO
    const { x, y } = anguloAPunto(grados, 50)
    puntos.push(`${x}% ${y}%`)
  }
  return `polygon(${puntos.join(', ')})`
}

// Vector (dx, dy) hacia donde se aleja cada porción al "servirse": la dirección es el
// ángulo medio de la tajada (su bisectriz), no cualquiera de sus dos bordes.
function separacionPorcion(indice, distanciaPx) {
  const anguloPorcion = 360 / TOTAL_PORCIONES
  const medio = indice * anguloPorcion + anguloPorcion / 2 - 90
  const radianes = (medio * Math.PI) / 180
  return { x: Math.cos(radianes) * distanciaPx, y: Math.sin(radianes) * distanciaPx }
}

// Se calcula una sola vez (no es reactivo: la geometría de las 8 porciones es siempre
// la misma, sin importar qué pizza esté abierta) y se reutiliza en cada apertura.
function construirPorciones(distanciaPx = 46) {
  return Array.from({ length: TOTAL_PORCIONES }, (_, indice) => ({
    clip: clipPorcion(indice),
    ...separacionPorcion(indice, distanciaPx),
  }))
}

// Borde de "ticket de papel" con cortes en zigzag arriba y abajo: se generan los
// mismos puntos en diente de sierra que se usarían a mano en un editor de gráficos,
// pero calculados (DIENTES, PROFUNDIDAD) para no tener que escribir un polygon() larguísimo.
function recorteTicket(dientes = 14, profundidadPorciento = 5) {
  const arriba = []
  const abajo = []
  for (let i = 0; i <= dientes; i++) {
    const x = (i / dientes) * 100
    const esPico = i % 2 === 0
    arriba.push(`${x}% ${esPico ? 0 : profundidadPorciento}%`)
    abajo.push(`${x}% ${esPico ? 100 : 100 - profundidadPorciento}%`)
  }
  // El borde de abajo se recorre al revés (derecha a izquierda) para que el polígono
  // cierre su contorno sin cruzarse consigo mismo.
  return `polygon(${arriba.join(', ')}, ${abajo.reverse().join(', ')})`
}
</script>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
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
const pizzaActiva = computed(() => cartas.value[activa.value])

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
const carruselEl = ref(null)
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
  // Se mide la "pista" (el carril por donde se deslizan las tarjetas), no la sección
  // completa: la sección también incluye las dos flechas, así que su ancho total NO
  // es el espacio real disponible para centrar una tarjeta.
  if (pistaEl.value) anchoContenedor.value = pistaEl.value.getBoundingClientRect().width
}

// Distancia (en píxeles) entre el inicio de una tarjeta y el inicio de la siguiente:
// es la unidad que se usa para todo el cálculo de posiciones y de arrastre. "posicion"
// nunca está en píxeles sueltos, siempre en "cuántos pasos de este tamaño".
const paso = computed(() => anchoTarjeta.value + SEPARACION)

// Traslado en píxeles de toda la cinta: centra la tarjeta "posicion" restando la
// mitad de su ancho y usando la mitad del contenedor como punto central.
const trasladoPx = computed(() => anchoContenedor.value / 2 - anchoTarjeta.value / 2 - posicion.value * paso.value)

function estiloCarta(indice) {
  const distancia = indice - posicion.value
  return {
    // Cada tarjeta se ubica a "indice * paso" de la primera; la cinta completa se
    // desplaza aparte con trasladoPx (ver :style del contenedor .pista).
    left: `${indice * paso.value}px`,
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
// (por ejemplo, la inercia todavía frenando, o el sello girando sin fin) no se entera
// solo de que el componente desapareció. Sin revert(), ese tween seguiría corriendo en
// segundo plano y, peor, seguiría escribiendo en refs de una instancia de Vue que ya
// no existe.
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

// --- Arrastre con el mouse y con el dedo (Pointer Events cubre ambos casos) -----

let inicio = null // { x, posicionInicial, muestras: [{t, posicion}] }

function alPresionar(evento) {
  if (detalleAbierto.value) return
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
  let nueva = inicio.posicionInicial - deltaPx / paso.value
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

// --- Teclado del carrusel --------------------------------------------------------

function alPresionarTecla(evento) {
  if (evento.key === 'ArrowRight') {
    siguiente()
    evento.preventDefault()
  } else if (evento.key === 'ArrowLeft') {
    anterior()
    evento.preventDefault()
  }
}

// --- Título que se "enciende" letra por letra -----------------------------------

// Array.from (no .split('')) para separar bien cualquier caracter especial del
// nombre (tildes, ñ) en lugar de cortar a mitad de un caracter compuesto.
const letrasNombre = computed(() => Array.from(pizzaActiva.value?.nombre ?? ''))
const letraEls = ref([])
function guardarLetraEl(el, indice) {
  if (el) letraEls.value[indice] = el
}

function animarTitulo() {
  if (!letraEls.value.length) return
  if (prefiereReducido.value) {
    gsap.set(letraEls.value, { color: '#fff8f6' })
    return
  }
  ctx.add(() => {
    // fromTo (no solo "to") para que cada vez que cambia la pizza activa el título
    // vuelva a arrancar "apagado", sin importar en qué color haya quedado el tween
    // anterior sobre esos mismos elementos del DOM.
    gsap.fromTo(
      letraEls.value,
      { color: 'rgba(255, 248, 246, 0.35)' },
      { color: '#fff8f6', duration: 0.05, ease: 'none', stagger: 0.035 }
    )
  })
}

// Cada vez que se asienta en una pizza nueva, las letras de esa pizza son elementos
// del DOM recién creados (el nombre cambió de largo), así que primero se vacía
// "letraEls" (si no, quedarían índices viejos de un nombre más largo) y luego, ya
// con el DOM actualizado (nextTick), se vuelve a juntar la lista y se anima.
watch(activa, async () => {
  letraEls.value = []
  await nextTick()
  animarTitulo()
})

// --- Detalle: la pizza se abre en porciones --------------------------------------

const detalleAbierto = ref(false)
const pizzaDetalle = ref(null)
const ingredientesDetalle = computed(() => (pizzaDetalle.value ? dividirIngredientes(pizzaDetalle.value.descripcion) : []))
const precioMostrado = ref(0)
const precioMostradoTexto = computed(() => formatearPrecio(precioMostrado.value))

// Geometría de las 8 porciones (igual para cualquier pizza) y del recorte del ticket:
// se calculan una sola vez al cargar el componente, no en cada apertura.
const porciones = construirPorciones()
const recorteTicketCss = recorteTicket()

const panelEl = ref(null)
const fotoCompletaEl = ref(null)
const porcionEls = ref([])
function guardarPorcionEl(el, indice) {
  if (el) porcionEls.value[indice] = el
}
const ticketLineaEls = ref([])
function guardarTicketLineaEl(el, indice) {
  if (el) ticketLineaEls.value[indice] = el
}
const selloEl = ref(null)
const botonCerrarEl = ref(null)

let timelineDetalle = null
let tweenSello = null

function abrirDetalle() {
  if (!pizzaActiva.value) return
  timelineDetalle?.kill()
  tweenSello?.kill()
  porcionEls.value = []
  ticketLineaEls.value = []
  precioMostrado.value = 0
  pizzaDetalle.value = pizzaActiva.value
  detalleAbierto.value = true
  nextTick(construirAnimacionApertura)
}

function construirAnimacionApertura() {
  const precioFinal = pizzaDetalle.value?.precio || 0

  if (prefiereReducido.value) {
    // Sin giro, sin inercia, sin conteo: con "reducir movimiento" activado la pizza
    // aparece ya abierta, de una sola vez.
    gsap.set(fotoCompletaEl.value, { opacity: 0 })
    gsap.set(porcionEls.value, { x: (indice) => porciones[indice].x, y: (indice) => porciones[indice].y, opacity: 1 })
    gsap.set(ticketLineaEls.value, { opacity: 1, y: 0 })
    precioMostrado.value = precioFinal
    botonCerrarEl.value?.focus()
    return
  }

  ctx.add(() => {
    timelineDetalle = gsap
      .timeline({ onComplete: () => botonCerrarEl.value?.focus() })
      // a) la tarjeta "crece" hasta el panel centrado (el fondo oscuro ya está
      // visible por CSS en cuanto detalleAbierto es true; aquí solo se anima el
      // contenido para que no aparezca de golpe).
      .fromTo(panelEl.value, { opacity: 0, scale: 0.4 }, { opacity: 1, scale: 1, duration: 0.5, ease: 'power3.out' })
      // b) una vuelta completa y luego el corte: la foto entera gira, y al terminar
      // se apaga mientras las 8 porciones (ya en su sitio, superpuestas formando el
      // mismo círculo) se separan hacia afuera como al servir.
      .to(fotoCompletaEl.value, { rotation: 360, duration: 0.9, ease: 'power2.inOut' }, 0)
      .to(fotoCompletaEl.value, { opacity: 0, duration: 0.2 })
      .fromTo(
        porcionEls.value,
        { x: 0, y: 0, opacity: 0 },
        {
          x: (indice) => porciones[indice].x,
          y: (indice) => porciones[indice].y,
          opacity: 1,
          duration: 0.5,
          ease: 'back.out(1.4)',
          stagger: 0.04,
        }
      )
      // c) el ticket se "escribe" línea por línea (nombre, ingredientes, punteada, total).
      .fromTo(ticketLineaEls.value, { opacity: 0, y: 8 }, { opacity: 1, y: 0, duration: 0.25, stagger: 0.12 }, '-=0.2')
      // e) el total cuenta desde 0 hasta el precio real en 1 segundo, en paralelo a
      // que se van escribiendo las últimas líneas del ticket.
      .to(precioMostrado, { value: precioFinal, duration: 1, ease: 'power1.out' }, '<')

    // d) el sello gira lentamente y sin parar mientras el panel esté abierto; por eso
    // vive FUERA de la timeline (que sí termina) y se detiene a mano en cerrarDetalle.
    tweenSello = gsap.to(selloEl.value, { rotation: 360, duration: 9, repeat: -1, ease: 'none' })
  })
}

function cerrarDetalle() {
  tweenSello?.kill()

  const terminar = () => {
    detalleAbierto.value = false
    pizzaDetalle.value = null
    // Devuelve el foco al carrusel (y no se queda "perdido" en un botón que acaba de
    // desaparecer de la vista).
    carruselEl.value?.focus()
  }

  if (prefiereReducido.value || !timelineDetalle) {
    terminar()
    return
  }

  // 1.4x más rápido al cerrar que al abrir: la apertura es la que "cuenta la
  // historia" (girar, cortar, escribir el ticket); cerrar es solo volver atrás.
  timelineDetalle.timeScale(1.4)
  timelineDetalle.eventCallback('onReverseComplete', terminar)
  timelineDetalle.reverse()
}

function alPresionarTeclaGlobal(evento) {
  if (evento.key === 'Escape' && detalleAbierto.value) cerrarDetalle()
}

// --- Ciclo de vida ---------------------------------------------------------------

let observador = null

onMounted(async () => {
  mediaReducido = window.matchMedia?.('(prefers-reduced-motion: reduce)') || null
  actualizarReducido()
  mediaReducido?.addEventListener?.('change', actualizarReducido)
  window.addEventListener('keydown', alPresionarTeclaGlobal)

  await nextTick() // espera a que v-for dibuje las tarjetas antes de medirlas
  ctx = gsap.context(() => {}, raiz.value)
  medir()
  posicion.value = activa.value
  animarTitulo()

  observador = new ResizeObserver(medir)
  if (raiz.value) observador.observe(raiz.value)
})

onUnmounted(() => {
  mediaReducido?.removeEventListener?.('change', actualizarReducido)
  window.removeEventListener('keydown', alPresionarTeclaGlobal)
  observador?.disconnect()
  tweenPosicion?.kill()
  tweenSello?.kill()
  timelineDetalle?.kill()
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
      ref="carruselEl"
      role="region"
      aria-roledescription="carrusel"
      aria-label="Carrusel de pizzas"
      :tabindex="detalleAbierto ? -1 : 0"
      :inert="detalleAbierto"
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

    <!-- Título que se "enciende": cada letra es un span propio para poder animar su
         color de a una (stagger de GSAP). Las letras sueltas van aria-hidden porque un
         lector de pantalla leyéndolas una por una ("P", "e", "p"...) sonaría roto; el
         span oculto visualmente de al lado trae el nombre completo y normal. -->
    <div class="menu-interactivo__titulo-pizza" v-if="pizzaActiva">
      <h3 class="menu-interactivo__nombre">
        <span class="menu-interactivo__oculto">{{ pizzaActiva.nombre }}</span>
        <span aria-hidden="true">
          <span
            v-for="(letra, indice) in letrasNombre"
            :key="indice"
            class="menu-interactivo__letra"
            :ref="(el) => guardarLetraEl(el, indice)"
            >{{ letra }}</span
          >
        </span>
      </h3>
      <p class="menu-interactivo__precio">{{ formatearPrecio(pizzaActiva.precio) }}</p>
      <p class="menu-interactivo__ayuda">Toca la pizza para verla por dentro</p>
    </div>

    <!-- Panel de detalle: siempre está en el DOM (v-show, no v-if) para no perder las
         referencias de GSAP entre una apertura y la siguiente; mientras está cerrado
         queda con display:none y fuera del paso del teclado. -->
    <div
      class="menu-interactivo__panel"
      v-show="detalleAbierto"
      ref="panelEl"
      role="dialog"
      aria-modal="true"
      :aria-label="pizzaDetalle ? `${pizzaDetalle.nombre} por dentro` : 'Pizza por dentro'"
    >
      <button type="button" class="menu-interactivo__cerrar" ref="botonCerrarEl" @click="cerrarDetalle">
        Cerrar ✕
      </button>

      <template v-if="pizzaDetalle">
        <div class="menu-interactivo__pizza-abierta">
          <div class="menu-interactivo__circulo">
            <img
              ref="fotoCompletaEl"
              :src="`/images/pizzas/${pizzaDetalle.imagen}`"
              :alt="pizzaDetalle.nombre"
              class="menu-interactivo__foto-completa"
            />
            <div
              v-for="(porcion, indice) in porciones"
              :key="indice"
              class="menu-interactivo__porcion"
              :ref="(el) => guardarPorcionEl(el, indice)"
              :style="{ clipPath: porcion.clip, backgroundImage: `url(/images/pizzas/${pizzaDetalle.imagen})` }"
            ></div>
          </div>

          <!-- Sello circular: el <div> que gira es solo el círculo de texto (la svg);
               el texto del centro vive afuera, como hermano, para que no gire con él. -->
          <div class="menu-interactivo__sello-caja" aria-hidden="true">
            <div class="menu-interactivo__sello" ref="selloEl">
              <svg viewBox="0 0 200 200" class="menu-interactivo__sello-svg">
                <defs>
                  <path id="menuInteractivoCirculoSello" d="M100,100 m-82,0 a82,82 0 1,1 164,0 a82,82 0 1,1 -164,0" />
                </defs>
                <text font-size="11.5" letter-spacing="3" fill="#fff8f6">
                  <textPath href="#menuInteractivoCirculoSello">
                    HECHA EN HORNO DE LEÑA • HECHA EN HORNO DE LEÑA •
                  </textPath>
                </text>
              </svg>
            </div>
            <p class="menu-interactivo__sello-centro">100%<br />artesanal</p>
          </div>
        </div>

        <div class="menu-interactivo__ticket" :style="{ clipPath: recorteTicketCss }">
          <p class="menu-interactivo__ticket-nombre" :ref="(el) => guardarTicketLineaEl(el, 0)">
            {{ pizzaDetalle.nombre }}
          </p>
          <p
            v-for="(ingrediente, indice) in ingredientesDetalle"
            :key="ingrediente"
            class="menu-interactivo__ticket-linea"
            :ref="(el) => guardarTicketLineaEl(el, indice + 1)"
          >
            {{ ingrediente }}
          </p>
          <p
            class="menu-interactivo__ticket-punteada"
            :ref="(el) => guardarTicketLineaEl(el, ingredientesDetalle.length + 1)"
            aria-hidden="true"
          >
            - - - - - - - - - - - - - - -
          </p>
          <p class="menu-interactivo__ticket-total" :ref="(el) => guardarTicketLineaEl(el, ingredientesDetalle.length + 2)">
            <span>TOTAL</span>
            <span>{{ precioMostradoTexto }}</span>
          </p>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
/* Reutiliza los colores de marca de tokens.css (coinciden con el rojo/crema que ya
   usa el resto del sitio público) pero NO sus fuentes: Merriweather/Plus Jakarta Sans
   son solo del panel de admin. Marcellus/Baloo 2/Figtree siguen escritas literal,
   igual que en NavBar.vue y MenuPizzas.vue, porque tokens.css no las define. */
.menu-interactivo {
  position: relative; /* el panel de detalle (position:absolute) se ubica contra esta sección */
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

/* --- Título que se enciende ----------------------------------------------------- */

.menu-interactivo__titulo-pizza {
  margin-top: 2rem;
}

.menu-interactivo__nombre {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: clamp(1.8rem, 4vw, 2.6rem);
  margin: 0;
}

/* Visualmente oculto pero presente para lectores de pantalla: igual técnica que un
   "sr-only" clásico (no se usa display:none porque algunos lectores lo ignoran). */
.menu-interactivo__oculto {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
}

.menu-interactivo__letra {
  color: rgba(255, 248, 246, 0.35); /* "apagado": el mismo valor inicial que usa animarTitulo() */
}

.menu-interactivo__precio {
  font-family: 'Figtree', system-ui, sans-serif;
  font-weight: 700;
  font-size: 1.3rem;
  color: var(--primary, #a62518);
  margin: 0.6rem 0 0;
}

.menu-interactivo__ayuda {
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.85rem;
  color: rgba(255, 248, 246, 0.55);
  margin: 0.3rem 0 0;
}

/* --- Panel de detalle: la pizza abierta ------------------------------------------ */

.menu-interactivo__panel {
  /* position:fixed (no absolute) y z-index por encima del NavBar (z-index:50, ver
     NavBar.vue): como el NavBar es sticky, queda "flotando" sobre cualquier cosa que
     pase por debajo al hacer scroll, así que si el panel solo cubriera la sección
     (position:absolute) el botón Cerrar podía terminar tapado por el NavBar según
     cuánto se hubiera scrolleado. Fijo al viewport, el panel y su botón siempre
     quedan completos en pantalla sin importar el scroll. */
  position: fixed;
  inset: 0;
  z-index: 60;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2.5rem;
  flex-wrap: wrap;
  padding: 2rem;
  background-color: rgba(10, 6, 3, 0.94);
  overflow: auto;
}

.menu-interactivo__cerrar {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 248, 246, 0.35);
  background-color: transparent;
  color: #fff8f6;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.85rem;
  cursor: pointer;
}

.menu-interactivo__cerrar:hover {
  background-color: var(--primary);
  border-color: var(--primary);
}

.menu-interactivo__pizza-abierta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.menu-interactivo__circulo {
  position: relative;
  width: 230px;
  height: 230px;
  /* overflow:visible (no hidden): las porciones deben poder "salirse" del círculo al
     separarse hacia afuera, como una pizza servida en el plato. */
  overflow: visible;
}

.menu-interactivo__foto-completa {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.menu-interactivo__porcion {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  background-size: 230px 230px; /* mismo tamaño que .circulo: cada porción muestra "su" pedazo de la foto completa */
  background-position: center;
  filter: drop-shadow(0 6px 10px rgba(0, 0, 0, 0.5));
}

.menu-interactivo__sello-caja {
  position: relative;
  width: 110px;
  height: 110px;
}

.menu-interactivo__sello,
.menu-interactivo__sello-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.menu-interactivo__sello-centro {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0;
  text-align: center;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.7rem;
  font-weight: 700;
  color: #fff8f6;
  line-height: 1.2;
}

.menu-interactivo__ticket {
  width: 260px;
  padding: 1.5rem 1.25rem;
  background-color: #fbf3e7; /* papel: mismo tono cálido que usa NavBar para su texto claro */
  color: #2a170f;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.9rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}

.menu-interactivo__ticket-nombre {
  margin: 0 0 0.75rem;
  font-family: 'Marcellus', Georgia, serif;
  font-size: 1.1rem;
  text-align: center;
}

.menu-interactivo__ticket-linea {
  margin: 0.3rem 0;
  text-transform: capitalize;
}

.menu-interactivo__ticket-linea::before {
  content: '— ';
}

.menu-interactivo__ticket-punteada {
  margin: 0.75rem 0;
  letter-spacing: 0.05em;
  opacity: 0.5;
}

.menu-interactivo__ticket-total {
  display: flex;
  justify-content: space-between;
  margin: 0;
  font-weight: 700;
  font-size: 1.05rem;
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

/* El ticket se corre debajo de la pizza (en vez de al lado) desde el momento en que
   ambos ya no entran cómodos uno junto al otro, no solo en celulares angostos. */
@media (max-width: 700px) {
  .menu-interactivo__panel {
    flex-direction: column;
    padding-top: 3.5rem;
  }
}
</style>
