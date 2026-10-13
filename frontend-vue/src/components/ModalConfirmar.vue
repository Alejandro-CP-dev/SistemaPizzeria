<script setup>
import { onMounted, onUnmounted } from 'vue'

defineProps({
  nombre: String,
})

const emit = defineEmits(['confirmar', 'cancelar'])

/** Permite cerrar el modal con la tecla Escape, además del botón Cancelar. */
function alPresionarTecla(evento) {
  if (evento.key === 'Escape') {
    emit('cancelar')
  }
}

// El listener se agrega a "window" (no a un elemento del template) porque Escape debe
// funcionar sin importar qué tenga el foco en ese momento; se quita en onUnmounted para
// que, una vez cerrado el modal, dejar de escuchar teclas no le corresponda a nadie.
onMounted(() => window.addEventListener('keydown', alPresionarTecla))
onUnmounted(() => window.removeEventListener('keydown', alPresionarTecla))
</script>

<template>
  <!-- El fondo oscuro cubre toda la pantalla y hacer clic en él también cancela: es el
       mismo comportamiento que casi cualquier modal que el usuario ya conoce, además de
       Cancelar y Escape. -->
  <div class="modal__fondo" @click="emit('cancelar')">
    <!-- @click.stop evita que el clic DENTRO de la tarjeta "suba" hasta el fondo y la
         cierre por accidente. -->
    <div class="modal__tarjeta" @click.stop>
      <div class="modal__franja"></div>

      <div class="modal__icono">
        <span class="material-symbols-outlined">warning</span>
      </div>

      <h3 class="modal__titulo">¿Eliminar la pizza {{ nombre }}?</h3>
      <p class="modal__texto">
        Esta acción no se puede deshacer. La pizza se retirará de inmediato del menú público.
      </p>

      <div class="modal__botones">
        <button type="button" class="modal__cancelar" @click="emit('cancelar')">Cancelar</button>
        <button type="button" class="modal__eliminar" @click="emit('confirmar')">
          <span class="material-symbols-outlined">delete_forever</span>
          <span>Eliminar</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal__fondo {
  position: fixed;
  inset: 0;
  background-color: rgba(27, 17, 10, 0.65); /* espresso con transparencia: oscurece el fondo */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-md);
  z-index: 70;
}

.modal__tarjeta {
  position: relative;
  overflow: hidden;
  background-color: var(--surface-container-lowest);
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 45px rgba(27, 17, 10, 0.3);
  padding: var(--space-lg);
  max-width: 380px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  font-family: var(--font-body);
}

.modal__franja {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background-color: var(--error);
}

.modal__icono {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  background-color: var(--error-container);
  color: var(--error);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 1px 4px rgba(0, 0, 0, 0.15);
  margin-bottom: var(--space-sm);
}

.modal__icono .material-symbols-outlined {
  font-size: 32px;
}

.modal__titulo {
  font: var(--fw-headline-sm) var(--fs-headline-sm)/var(--lh-headline-sm) var(--font-headline);
  color: var(--on-surface);
  margin: var(--space-xs) 0 0;
}

.modal__texto {
  font: var(--fw-body-md) var(--fs-body-md)/var(--lh-body-md) var(--font-body);
  color: var(--on-surface-variant);
  margin: var(--space-sm) 0 0;
}

.modal__botones {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
  width: 100%;
}

.modal__cancelar,
.modal__eliminar {
  flex: 1 1 50%;
  padding: 0.65rem;
  border: none;
  border-radius: var(--radius-lg);
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  cursor: pointer;
  transition: background-color 0.15s, transform 0.1s;
}

/* active:scale-95 del diseño: un pequeño "hundimiento" al hacer clic, para que el botón
   se sienta físico en vez de solo cambiar de color. */
.modal__cancelar:active,
.modal__eliminar:active {
  transform: scale(0.95);
}

.modal__cancelar {
  background-color: var(--surface-container);
  color: var(--on-surface);
}

.modal__cancelar:hover {
  background-color: var(--surface-container-high);
}

.modal__eliminar {
  background-color: var(--error);
  color: var(--on-error);
  box-shadow: 0 4px 14px rgba(186, 26, 26, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
}

.modal__eliminar .material-symbols-outlined {
  font-size: 16px;
}

.modal__eliminar:hover {
  background-color: var(--on-error-container);
}
</style>
