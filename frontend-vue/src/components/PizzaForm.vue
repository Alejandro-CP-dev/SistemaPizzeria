<script setup>
import { onUnmounted, ref } from 'vue'
import PizzaCard from './PizzaCard.vue'

// Mismos límites en los dos lados: aquí se avisa rápido, sin esperar una respuesta del
// servidor; en ImagenServlet.java se vuelven a revisar de verdad, porque el navegador
// se puede saltar (con curl, Postman, etc.) y el backend no puede confiar en que el
// archivo que le llega ya viene validado.
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp']
const TAMANO_MAXIMO = 5 * 1024 * 1024 // 5 MB

const props = defineProps({
  // null -> se está creando una pizza nueva. Un objeto -> se está editando esa pizza.
  pizza: { type: Object, default: null },
})

const emit = defineEmits(['guardar', 'cancelar'])

/**
 * Arma el objeto inicial del formulario: vacío para crear, o una COPIA de "pizza" para
 * editar. El diseño de referencia no trae un campo "destacado" (esa marca de "Favorita"
 * quedó fuera también en PizzaCard.vue, por ser un dato inventado por la herramienta de
 * diseño): por eso la copia tampoco lo incluye aquí.
 */
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

// El archivo que la persona elige con "Examinar" (o null si no eligió uno nuevo: al
// editar una pizza que ya tenía foto, esta copia conserva su "imagen" de siempre sin
// que haga falta volver a subir nada). Se guarda APARTE de "copia" porque un File no es
// un dato que vaya a viajar como JSON a PizzaServlet: primero hay que subirlo a
// ImagenServlet, que es quien de verdad devuelve el nombre final del archivo (ver
// guardar() más abajo y guardarPizza() en AdminView.vue).
const archivoImagen = ref(null)
const inputArchivo = ref(null)

// Empieza vacío a propósito: mientras "errores.nombre"/"errores.precio"/"errores.imagen"
// sean cadenas vacías, el <template> no dibuja ningún estado de error (ver v-if más
// abajo). Las de nombre/precio solo se llenan dentro de validar(), al intentar guardar;
// la de imagen se llena apenas se elige un archivo inválido (ver alElegirArchivo), para
// avisar de inmediato en vez de esperar a que la persona haga clic en Guardar.
const errores = ref({ nombre: '', precio: '', imagen: '' })

function validar() {
  errores.value.nombre = ''
  errores.value.precio = ''
  // errores.imagen NO se reinicia aquí: lo controla alElegirArchivo, no un intento de
  // guardar. Si ya hay un archivo inválido elegido, debe seguir bloqueando el guardado.
  let esValido = !errores.value.imagen

  if (!copia.value.nombre || !copia.value.nombre.trim()) {
    errores.value.nombre = 'El nombre de la pizza es obligatorio'
    esValido = false
  }

  if (!copia.value.precio || copia.value.precio <= 0) {
    errores.value.precio = 'El precio de venta al público es obligatorio'
    esValido = false
  }

  return esValido
}

/** Revoca la URL temporal anterior (si había) antes de reemplazarla o al cerrar el formulario. */
function limpiarVistaPreviaAnterior() {
  if (copia.value.imagenVistaPrevia) {
    // Un blob: URL ocupa memoria en el navegador hasta que se revoca a mano; a
    // diferencia de la memoria normal de JavaScript, el recolector de basura no la
    // libera solo. Si no se revocara, cada foto nueva que alguien probara aquí (antes
    // de decidirse a guardar) se quedaría ocupando espacio el resto de la sesión.
    URL.revokeObjectURL(copia.value.imagenVistaPrevia)
  }
}

function alElegirArchivo(evento) {
  const archivo = evento.target.files[0]
  // Limpia el <input> para que, si más adelante la persona vuelve a elegir el MISMO
  // archivo (por ejemplo, después de cancelar), el navegador dispare @change otra vez:
  // sin esto, elegir dos veces seguidas el mismo archivo no generaría ningún evento.
  evento.target.value = ''
  if (!archivo) {
    return
  }

  if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
    errores.value.imagen = 'Solo se permiten imágenes JPG, PNG o WEBP.'
    return
  }
  if (archivo.size > TAMANO_MAXIMO) {
    errores.value.imagen = 'La imagen no puede pesar más de 5 MB.'
    return
  }

  errores.value.imagen = ''
  limpiarVistaPreviaAnterior()
  archivoImagen.value = archivo
  // URL.createObjectURL crea una dirección temporal (blob:http://...) que el propio
  // navegador entiende para mostrar un archivo que el usuario eligió en su computador,
  // SIN haberlo subido todavía a ningún servidor. Es justo lo que hace falta para la
  // vista previa en vivo: el archivo no existe en el backend hasta que se guarda.
  copia.value.imagenVistaPrevia = URL.createObjectURL(archivo)
}

// Si la persona cierra el formulario (Cancelar, o cambia a editar otra pizza) sin haber
// guardado, la URL temporal de la vista previa se queda "viva" en el navegador a menos
// que se revoque aquí. onUnmounted se dispara siempre en ese momento porque AdminView.vue
// monta un PizzaForm nuevo con :key cada vez que cambia qué se está editando (ver
// AdminView.vue): el PizzaForm anterior se destruye de verdad, no se reutiliza.
onUnmounted(limpiarVistaPreviaAnterior)

function guardar() {
  if (!validar()) {
    return // No se emite nada: AdminView nunca se entera de un formulario inválido.
  }
  // "imagenVistaPrevia" es un dato de solo este componente (la URL temporal del blob);
  // Pizza.java no tiene ese campo y nunca debería viajar al backend como si fuera parte
  // de la pizza. "archivo" viaja APARTE, no dentro de "datos", porque AdminView.vue
  // necesita subirlo primero (a ImagenServlet) y recién después armar el POST/PUT de la
  // pizza con el nombre de archivo que el servidor le devuelva.
  const datos = { ...copia.value }
  delete datos.imagenVistaPrevia
  emit('guardar', { datos, archivo: archivoImagen.value })
}
</script>

<template>
  <div class="pizza-form">
    <!-- Barra de degradado: puramente decorativa (aria-hidden), marca visualmente que
         este panel es "el editor", igual que en el diseño de referencia. -->
    <div class="pizza-form__barra" aria-hidden="true"></div>

    <div class="pizza-form__contenido">
      <div class="pizza-form__encabezado">
        <div class="pizza-form__encabezado-titulo">
          <h2 class="pizza-form__titulo">{{ pizza ? 'Editar pizza' : 'Agregar pizza' }}</h2>
          <!-- La insignia con el nombre y el "ID #" solo tienen sentido editando: una
               pizza nueva todavía no tiene nombre guardado ni id en la base de datos. -->
          <span v-if="pizza" class="pizza-form__insignia">{{ copia.nombre }}</span>
        </div>
        <span v-if="pizza" class="pizza-form__id">ID #{{ String(copia.id).padStart(2, '0') }}</span>
      </div>

      <form class="pizza-form__campos" @submit.prevent="guardar">
        <!-- Campo 1: Imagen -->
        <div class="pizza-form__campo">
          <label class="pizza-form__etiqueta-fila">
            <span>Fotografía de la pizza</span>
            <span class="pizza-form__ayuda-inline">Recomendado 800x800px</span>
          </label>
          <div class="pizza-form__imagen-caja">
            <div class="pizza-form__imagen-miniatura">
              <div v-if="!copia.imagen && !copia.imagenVistaPrevia" class="pizza-form__imagen-vacia">Sin foto</div>
              <img v-else :src="copia.imagenVistaPrevia || `/images/pizzas/${copia.imagen}`" :alt="copia.nombre" />
            </div>
            <div class="pizza-form__imagen-controles">
              <div class="pizza-form__imagen-fila">
                <!-- readonly: este campo solo MUESTRA qué archivo quedó elegido, nunca se
                     escribe directo en él. El nombre real de la foto lo decide siempre
                     ImagenServlet (ver guardarPizza() en AdminView.vue), nunca el navegador. -->
                <input
                  type="text"
                  readonly
                  class="pizza-form__imagen-nombre"
                  :value="archivoImagen ? archivoImagen.name : copia.imagen || ''"
                  placeholder="Ningún archivo elegido"
                />
                <!-- El <input type="file"> real queda oculto (ver .pizza-form__input-oculto):
                     los navegadores no dejan diseñar ese control directamente, así que este
                     botón normal lo "dispara" por código con inputArchivo.click(). -->
                <button type="button" class="pizza-form__examinar" @click="inputArchivo.click()">
                  Examinar
                </button>
              </div>
              <span v-if="archivoImagen" class="pizza-form__imagen-ok">
                <span class="material-symbols-outlined">cloud_upload</span>
                <span>Se subirá al guardar</span>
              </span>
            </div>
            <input
              ref="inputArchivo"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              class="pizza-form__input-oculto"
              @change="alElegirArchivo"
            />
          </div>
          <p v-if="errores.imagen" class="pizza-form__mensaje-error">
            <span class="material-symbols-outlined">info</span>
            <span>{{ errores.imagen }}</span>
          </p>
        </div>

        <!-- Campo 2: Nombre -->
        <div class="pizza-form__campo">
          <div class="pizza-form__etiqueta-fila">
            <label class="pizza-form__etiqueta" for="input-nombre">
              <span>Nombre de la pizza</span>
              <span class="pizza-form__obligatorio">*</span>
            </label>
            <span class="pizza-form__contador" :class="{ 'pizza-form__contador--error': errores.nombre }">
              {{ copia.nombre.length }} / 50
            </span>
          </div>
          <div class="pizza-form__input-envoltorio">
            <input
              id="input-nombre"
              v-model="copia.nombre"
              type="text"
              maxlength="50"
              placeholder="Ej. Pizza Hawaiana Tradicional"
              class="pizza-form__input"
              :class="{ 'pizza-form__input--error': errores.nombre }"
            />
            <span v-if="errores.nombre" class="material-symbols-outlined pizza-form__input-icono-error">error</span>
          </div>
          <p v-if="errores.nombre" class="pizza-form__mensaje-error">
            <span class="material-symbols-outlined">info</span>
            <span>{{ errores.nombre }}</span>
          </p>
        </div>

        <!-- Campo 3: Descripción -->
        <div class="pizza-form__campo">
          <div class="pizza-form__etiqueta-fila">
            <label class="pizza-form__etiqueta" for="input-desc">Descripción para la carta</label>
            <span class="pizza-form__contador">{{ (copia.descripcion || '').length }} / 200</span>
          </div>
          <textarea
            id="input-desc"
            v-model="copia.descripcion"
            maxlength="200"
            rows="3"
            class="pizza-form__textarea"
          ></textarea>
          <span class="pizza-form__ayuda">Describe los ingredientes claves para atraer a los comensales.</span>
        </div>

        <!-- Campo 4: Precio (COP sin decimales) -->
        <div class="pizza-form__campo">
          <label class="pizza-form__etiqueta" for="input-precio">
            <span>Precio de venta al público</span>
            <span class="pizza-form__obligatorio">*</span>
          </label>
          <div class="pizza-form__precio-envoltorio">
            <span class="pizza-form__precio-prefijo">$ COP</span>
            <!-- v-model.number convierte lo que escribe el usuario (siempre texto) a número;
                 sin ".number", copia.precio sería el texto "32000" en vez del número 32000,
                 y la vista previa (PizzaCard) fallaría al formatearlo con toLocaleString. -->
            <input
              id="input-precio"
              v-model.number="copia.precio"
              type="number"
              step="1000"
              min="0"
              class="pizza-form__input pizza-form__precio-input"
              :class="{ 'pizza-form__input--error': errores.precio }"
            />
          </div>
          <span class="pizza-form__ayuda">Precio final en pesos colombianos, sin decimales.</span>
          <p v-if="errores.precio" class="pizza-form__mensaje-error">
            <span class="material-symbols-outlined">info</span>
            <span>{{ errores.precio }}</span>
          </p>
        </div>

        <div class="pizza-form__botones">
          <button type="button" class="pizza-form__cancelar" @click="emit('cancelar')">Cancelar</button>
          <button type="submit" class="pizza-form__guardar">
            <span class="material-symbols-outlined">save</span>
            <span>Guardar</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Vista previa en vivo: la MISMA tarjeta que se ve en el menú público, alimentada
         por "copia". Como copia es reactiva, cada letra que se escribe en el formulario
         se refleja aquí de inmediato, sin que haya que hacer nada extra. -->
    <div class="pizza-form__vista-previa">
      <div class="pizza-form__vista-previa-encabezado">
        <span class="pizza-form__vista-previa-titulo">
          <span class="material-symbols-outlined">visibility</span>
          <span>Vista previa en la carta</span>
        </span>
        <span class="pizza-form__vista-previa-nota">Se actualiza automáticamente al escribir</span>
      </div>
      <PizzaCard :pizza="copia" />
    </div>
  </div>
</template>

<style scoped>
.pizza-form {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  background-color: var(--surface-container-lowest);
  border-radius: var(--radius-xl);
  box-shadow: 0 4px 20px rgba(44, 24, 16, 0.1);
}

/* h-1.5 del diseño (6px): un valor decorativo puntual, no hace falta un token nuevo
   solo para esta franja. */
.pizza-form__barra {
  height: 6px;
  width: 100%;
  background: linear-gradient(to right, var(--primary), var(--tertiary-fixed-dim), var(--secondary));
}

.pizza-form__contenido {
  padding: 0 var(--space-lg) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.pizza-form__encabezado {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-sm);
  padding-bottom: var(--space-sm);
}

.pizza-form__encabezado-titulo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-width: 0;
}

.pizza-form__titulo {
  margin: 0;
  font: var(--fw-headline-sm) var(--fs-headline-sm)/var(--lh-headline-sm) var(--font-headline);
  color: var(--on-surface);
}

.pizza-form__insignia {
  padding: 0.125rem var(--space-sm);
  border-radius: var(--radius-lg);
  background-color: var(--primary-fixed);
  color: var(--on-primary-fixed-variant);
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pizza-form__id {
  flex-shrink: 0;
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  color: var(--on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.pizza-form__campos {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding-bottom: var(--space-lg);
}

.pizza-form__campo {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.pizza-form__etiqueta-fila {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-sm);
}

.pizza-form__etiqueta {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-surface);
}

.pizza-form__ayuda-inline {
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-form__obligatorio {
  color: var(--error);
  font-weight: 700;
}

.pizza-form__contador {
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-form__contador--error {
  color: var(--error);
}

.pizza-form__imagen-caja {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-sm);
  background-color: var(--surface-container-low);
  border-radius: var(--radius-xl);
}

.pizza-form__imagen-miniatura {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(44, 24, 16, 0.12);
}

.pizza-form__imagen-miniatura img,
.pizza-form__imagen-vacia {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pizza-form__imagen-vacia {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--inverse-surface);
  color: var(--inverse-on-surface);
  font: var(--fw-label-sm) var(--fs-label-sm)/1.1 var(--font-body);
  text-align: center;
  padding: 0.2rem;
}

.pizza-form__imagen-controles {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.pizza-form__imagen-fila {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.pizza-form__imagen-nombre {
  flex: 1;
  min-width: 0;
  padding: 0.4rem var(--space-sm);
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--surface-container-lowest);
  color: var(--on-surface);
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
}

.pizza-form__imagen-nombre::placeholder {
  color: rgba(89, 65, 61, 0.5); /* --on-surface-variant al 50% */
}

.pizza-form__examinar {
  flex-shrink: 0;
  padding: 0.4rem var(--space-sm);
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--surface-container);
  color: var(--on-surface);
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  cursor: pointer;
  transition: background-color 0.15s;
}

.pizza-form__examinar:hover {
  background-color: var(--surface-container-high);
}

.pizza-form__imagen-ok {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--secondary);
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
}

.pizza-form__imagen-ok .material-symbols-outlined {
  font-size: 14px !important;
}

/* El <input type="file"> nativo no se puede re-diseñar de forma confiable entre
   navegadores (cada uno dibuja su propio botón "Elegir archivo"); por eso se mantiene
   en el DOM (para que siga funcionando con teclado y lectores de pantalla) pero
   invisible, y el botón ".pizza-form__examinar" de arriba lo abre por código. */
.pizza-form__input-oculto {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

.pizza-form__input-envoltorio {
  position: relative;
}

.pizza-form__input,
.pizza-form__textarea {
  width: 100%;
  padding: 0.6rem var(--space-md);
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--surface-container-low);
  color: var(--on-surface);
  font: var(--fw-body-md) var(--fs-body-md)/var(--lh-body-md) var(--font-body);
  box-shadow: 0 1px 2px rgba(44, 24, 16, 0.05);
  transition: box-shadow 0.15s;
}

.pizza-form__textarea {
  resize: none;
}

.pizza-form__input:focus,
.pizza-form__textarea:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(166, 37, 24, 0.4);
}

/* Estado de error: fondo rojizo + anillo rojo + ícono "error", igual al demo del diseño.
   Solo se aplica cuando "errores.nombre"/"errores.precio" tienen texto (ver validar()
   en el script): nunca antes del primer intento de guardar con datos inválidos. */
.pizza-form__input--error {
  background-color: rgba(255, 218, 214, 0.3); /* --error-container al 30% */
  box-shadow: 0 1px 2px rgba(44, 24, 16, 0.05), 0 0 0 2px rgba(186, 26, 26, 0.8);
  padding-right: 2.5rem;
}

.pizza-form__input-icono-error {
  position: absolute;
  right: var(--space-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--error);
  font-size: 20px !important;
}

.pizza-form__mensaje-error {
  margin: 0.15rem 0 0;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  color: var(--error);
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
}

.pizza-form__mensaje-error .material-symbols-outlined {
  font-size: 16px !important;
}

.pizza-form__ayuda {
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-form__precio-envoltorio {
  position: relative;
  display: flex;
  align-items: center;
}

.pizza-form__precio-prefijo {
  position: absolute;
  left: var(--space-md);
  font: 700 var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-form__precio-input {
  padding-left: 4.5rem;
  font: var(--fw-metric-num) var(--fs-metric-num)/var(--lh-metric-num) var(--font-body);
}

.pizza-form__botones {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-xs);
}

.pizza-form__cancelar,
.pizza-form__guardar {
  padding: 0.65rem var(--space-lg);
  border: none;
  border-radius: var(--radius-lg);
  font: var(--fw-label-lg) var(--fs-label-lg)/var(--lh-label-lg) var(--font-body);
  cursor: pointer;
  transition: background-color 0.15s, transform 0.1s;
}

/* active:scale-95 del diseño: el botón se "hunde" un poco al hacer clic. */
.pizza-form__cancelar:active,
.pizza-form__guardar:active {
  transform: scale(0.95);
}

.pizza-form__cancelar {
  background-color: var(--surface-container);
  color: var(--on-surface);
}

.pizza-form__cancelar:hover {
  background-color: var(--surface-container-high);
}

.pizza-form__guardar {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding-left: var(--space-xl);
  padding-right: var(--space-xl);
  background-color: var(--primary);
  color: var(--on-primary);
  box-shadow: 0 4px 14px rgba(166, 37, 24, 0.3);
}

.pizza-form__guardar .material-symbols-outlined {
  font-size: 18px;
}

.pizza-form__guardar:hover {
  background-color: var(--primary-container);
}

.pizza-form__vista-previa {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  padding: 0 var(--space-lg) var(--space-lg);
}

.pizza-form__vista-previa-encabezado {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-sm);
  padding: 0 var(--space-xs);
  flex-wrap: wrap;
}

.pizza-form__vista-previa-titulo {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-surface-variant);
}

.pizza-form__vista-previa-titulo .material-symbols-outlined {
  font-size: 18px;
  color: var(--tertiary);
}

.pizza-form__vista-previa-nota {
  font-style: italic;
  font: italic var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: rgba(89, 65, 61, 0.8); /* --on-surface-variant al 80% */
}
</style>
