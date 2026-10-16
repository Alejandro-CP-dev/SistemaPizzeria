<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// Cuántos fotogramas hay en public/frames/ (frame-001.jpg ... frame-079.jpg).
const TOTAL_FOTOGRAMAS = 79

const seccion = ref(null) // referencia al <section> del template (ver "ref" más abajo)
const canvas = ref(null) // referencia al <canvas> del template
const imagenes = [] // una posición por fotograma; se llena una sola vez en onMounted

/** Arma la ruta del archivo de un fotograma. padStart rellena con ceros: 1 -> "001". */
function nombreArchivo(numero) {
  return `/frames/frame-${String(numero).padStart(3, '0')}.jpg`
}

/** Crea los 79 objetos Image() de una vez, para que al dibujar ya estén listos y no
 *  se vea un "parpadeo" esperando a que cada imagen se descargue justo cuando le toca. */
function precargarImagenes() {
  for (let numero = 1; numero <= TOTAL_FOTOGRAMAS; numero++) {
    const imagen = new Image()
    imagen.src = nombreArchivo(numero)
    imagenes[numero] = imagen
  }
}

/** Dibuja un fotograma en el canvas, escalado para tapar todo el espacio sin deformarse
 *  (lo mismo que hace "object-fit: cover" en CSS, pero a mano, porque <canvas> no tiene
 *  esa propiedad). */
function dibujar(numero) {
  const lienzo = canvas.value
  const imagen = imagenes[numero]
  if (!lienzo || !imagen || !imagen.complete) return

  const contexto = lienzo.getContext('2d')
  const escala = Math.max(lienzo.width / imagen.width, lienzo.height / imagen.height)
  const anchoDestino = imagen.width * escala
  const altoDestino = imagen.height * escala
  const x = (lienzo.width - anchoDestino) / 2
  const y = (lienzo.height - altoDestino) / 2

  contexto.clearRect(0, 0, lienzo.width, lienzo.height)
  contexto.drawImage(imagen, x, y, anchoDestino, altoDestino)
}

/** El canvas necesita su "width"/"height" en píxeles reales (no solo en CSS) para no
 *  verse borroso ni recortado; por eso se ajusta a mano cada vez que cambia el tamaño
 *  de la ventana. */
function ajustarTamano() {
  if (!canvas.value) return
  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  dibujar(fotogramaActual())
}

// Guardamos el último fotograma dibujado en una variable normal (no un ref) porque no
// se usa en el <template>: solo la necesita esta misma función para no repetir el
// dibujo si el fotograma no cambió. Un ref aquí solo obligaría a Vue a vigilar algo
// que ninguna parte visual de la página necesita leer.
let ultimoFotograma = 1
function fotogramaActual() {
  return ultimoFotograma
}

/** Calcula, según cuánto se ha scrolleado DENTRO de la sección, qué fotograma toca. */
function alHacerScroll() {
  if (!seccion.value) return

  // .hero mide 300vh (ver <style> más abajo): "recorrido" es cuánto scroll cabe dentro
  // de esa altura extra, una vez descontada la pantalla que ya ocupa lo pineado.
  const recorrido = seccion.value.offsetHeight - window.innerHeight
  // getBoundingClientRect().top es 0 cuando la sección empieza justo en la parte de
  // arriba de la ventana, y se vuelve más negativo mientras más se ha scrolleado hacia
  // abajo dentro de ella. Por eso "avance" (con el signo invertido) crece de 0 en
  // adelante a medida que se avanza.
  const avance = -seccion.value.getBoundingClientRect().top
  // Math.min/Math.max "recortan" el resultado para que progreso nunca sea menor que 0
  // (antes de llegar a la sección) ni mayor que 1 (después de salir de ella).
  const progreso = Math.min(Math.max(avance / recorrido, 0), 1)

  const numero = Math.round(1 + progreso * (TOTAL_FOTOGRAMAS - 1))
  if (numero !== ultimoFotograma) {
    ultimoFotograma = numero
    dibujar(numero)
  }
}

onMounted(() => {
  precargarImagenes()
  ajustarTamano()
  dibujar(1)

  // El scroll de la página completa no es el scroll de un elemento (un <div> con su
  // propia barra), así que no se puede escuchar con @scroll en el template: hay que
  // agregarlo a mano sobre "window". Igual con el tamaño de la ventana.
  window.addEventListener('scroll', alHacerScroll)
  window.addEventListener('resize', ajustarTamano)
})

onUnmounted(() => {
  // Si no se quitan estos listeners, cuando el usuario navegue a /login o /admin este
  // componente se destruye pero "window" seguiría llamando a alHacerScroll y a
  // ajustarTamano en cada scroll/resize de esas otras páginas: trabajo desperdiciado
  // (calculan sobre un <section> que ya no está en pantalla) y una fuga de memoria,
  // porque esas funciones nunca se liberarían solas.
  window.removeEventListener('scroll', alHacerScroll)
  window.removeEventListener('resize', ajustarTamano)
})
</script>

<template>
  <section id="inicio" ref="seccion" class="hero">
    <!-- .hero__pin se queda "pegado" (sticky) arriba de la pantalla mientras el usuario
         scrollea las 3 pantallas de alto que mide .hero; eso es lo que crea el efecto
         de ver la pizza "fija" en el centro mientras los fotogramas van cambiando. -->
    <div class="hero__pin">
      <canvas ref="canvas" class="hero__canvas"></canvas>
      <!-- Degradado oscuro encima del canvas: sin él, el texto y los botones nuevos de
           abajo perdían contraste según qué tan clara saliera la pizza del fotograma
           actual. Solo decoración, por eso aria-hidden. -->
      <div class="hero__velo" aria-hidden="true"></div>

      <div class="hero__contenido">
        <p class="hero__ojo">Horno de leña · Sogamoso</p>
        <h1 class="hero__titulo">PIZZA</h1>
        <p class="hero__bajada">
          Masa de 24 horas, horno de leña y productos de pequeños productores. Cada pizza
          se sirve como un plato, no como una entrega rápida.
        </p>
        <div class="hero__botones">
          <RouterLink :to="{ path: '/', hash: '#carta' }" class="hero__boton hero__boton--relleno">
            Ver la carta
          </RouterLink>
          <RouterLink :to="{ path: '/', hash: '#nosotros' }" class="hero__boton hero__boton--contorno">
            Nuestra historia
          </RouterLink>
        </div>
      </div>

      <div class="hero__desliza" aria-hidden="true">
        <span>Desliza</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M6 9l6 6 6-6" />
        </svg>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  height: 300vh; /* 3 pantallas de alto: el espacio de scroll donde "vive" la animación */
  /* El navbar ahora es "sticky" (NavBar.vue) y queda fijo arriba de la pantalla: sin este
     margen, al hacer clic en "Inicio" el navegador alinearía el borde de esta sección
     justo con el borde de la ventana, dejándola tapada detrás del navbar. */
  scroll-margin-top: 80px;
}

.hero__pin {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2a170f; /* café: se ve un instante mientras cargan las imágenes */
}

.hero__canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.hero__velo {
  position: absolute;
  inset: 0;
  /* Más claro arriba (donde no hay texto) y bien oscuro abajo (donde están la bajada y
     los botones), para que el fotograma de la pizza siga siendo protagonista arriba. */
  background: linear-gradient(180deg, rgba(27, 17, 10, 0.1) 0%, rgba(27, 17, 10, 0.35) 48%, rgba(18, 11, 6, 0.82) 100%);
}

.hero__contenido {
  position: relative;
  z-index: 1; /* por encima del canvas y del velo, que no tienen z-index y quedan "debajo" */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.1rem;
  max-width: 640px;
  padding: 0 1.5rem;
  text-align: center;
}

.hero__ojo {
  margin: 0;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.28em;
  text-transform: uppercase;
  color: rgba(255, 248, 246, 0.88);
}

.hero__titulo {
  margin: 0;
  color: #fff8f6;
  font-size: clamp(3rem, 15vw, 9rem); /* clamp: nunca más chico que 3rem ni más grande que 9rem */
  letter-spacing: 0.15em;
  line-height: 1;
  text-shadow: 0 4px 24px rgba(0, 0, 0, 0.6);
}

.hero__bajada {
  margin: 0;
  max-width: 34rem;
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-style: italic;
  font-size: 1.35rem;
  line-height: 1.5;
  color: rgba(255, 248, 246, 0.92);
}

.hero__botones {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1rem;
  margin-top: 0.5rem;
}

.hero__boton {
  padding: 0.85rem 2rem;
  border-radius: 2px;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  text-decoration: none;
  color: #fff8f6;
}

.hero__boton--relleno {
  background-color: var(--primary, #a62518);
}

.hero__boton--relleno:hover {
  background-color: #8e1f14;
}

.hero__boton--contorno {
  border: 1px solid rgba(255, 248, 246, 0.55);
}

.hero__boton--contorno:hover {
  border-color: #fff8f6;
}

.hero__desliza {
  position: absolute;
  left: 50%;
  bottom: 1.75rem;
  z-index: 1;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  color: rgba(255, 248, 246, 0.65);
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.65rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

@media (max-width: 560px) {
  .hero__desliza {
    display: none; /* en celular, donde la "pantalla completa" ya mide poco, estorba más de lo que ayuda */
  }
}
</style>
