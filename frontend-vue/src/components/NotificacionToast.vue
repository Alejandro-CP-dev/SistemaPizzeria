<script setup>
import { onMounted, onUnmounted } from 'vue'

// "mensaje" no es un booleano: es el TEXTO a mostrar ("Pizza guardada correctamente",
// "Pizza eliminada"...). Quien lo usa (AdminView) decide cuándo mostrarlo con v-if="mensaje":
// mientras mensaje sea una cadena vacía, este componente ni siquiera se crea.
defineProps({
  mensaje: { type: String, required: true },
})

const emit = defineEmits(['cerrar'])

// El diseño dice "Persistencia: 4 segundos": el toast se cierra solo, sin que el usuario
// tenga que hacer clic en nada. El botón de cerrar (abajo en el template) solo adelanta
// ese mismo evento si alguien no quiere esperar.
let temporizador = null

onMounted(() => {
  temporizador = setTimeout(() => emit('cerrar'), 4000)
})

onUnmounted(() => {
  clearTimeout(temporizador)
})
</script>

<template>
  <!-- position: fixed, no absolute: debe quedar en la misma esquina de la pantalla sin
       importar cuánto se haya scrolleado el panel por detrás. -->
  <div class="toast" role="status">
    <div class="toast__icono">
      <span class="material-symbols-outlined">check</span>
    </div>
    <div class="toast__texto">
      <span class="toast__mensaje">{{ mensaje }}</span>
      <span class="toast__momento">Hace un momento</span>
    </div>
    <button type="button" class="toast__cerrar" @click="emit('cerrar')" aria-label="Cerrar notificación">
      <span class="material-symbols-outlined">close</span>
    </button>
  </div>
</template>

<style scoped>
.toast {
  position: fixed;
  right: var(--margin-mobile);
  bottom: var(--margin-mobile);
  z-index: 60;
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  max-width: min(360px, calc(100vw - 2 * var(--margin-mobile)));
  padding: var(--space-md);
  border-radius: var(--radius-xl);
  background-color: var(--secondary-container);
  box-shadow: 0 10px 30px rgba(42, 23, 15, 0.22);
}

@media (min-width: 768px) {
  .toast {
    right: var(--margin);
    bottom: var(--margin);
  }
}

.toast__icono {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background-color: var(--secondary);
  color: var(--on-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(44, 24, 16, 0.1);
}

.toast__icono .material-symbols-outlined {
  font-size: 20px;
}

.toast__texto {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.toast__mensaje {
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-secondary-container);
}

.toast__momento {
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
  color: var(--secondary);
}

.toast__cerrar {
  flex-shrink: 0;
  margin-left: auto;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-full);
  background-color: transparent;
  color: var(--on-secondary-container);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.15s;
}

.toast__cerrar .material-symbols-outlined {
  font-size: 16px;
}

.toast__cerrar:hover {
  background-color: rgba(42, 106, 72, 0.2); /* --secondary al 20%, para el hover del botón de cerrar */
}
</style>
