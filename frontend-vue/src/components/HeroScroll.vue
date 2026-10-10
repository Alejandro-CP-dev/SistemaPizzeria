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
      <h1 class="hero__titulo">PIZZA</h1>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  height: 300vh; /* 3 pantallas de alto: el espacio de scroll donde "vive" la animación */
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

.hero__titulo {
  position: relative;
  z-index: 1; /* por encima del canvas, que no tiene z-index y queda "debajo" en el flujo normal */
  margin: 0;
  color: #fff8f6;
  font-size: clamp(3rem, 15vw, 9rem); /* clamp: nunca más chico que 3rem ni más grande que 9rem */
  letter-spacing: 0.15em;
  text-shadow: 0 4px 24px rgba(0, 0, 0, 0.6);
}
</style>
