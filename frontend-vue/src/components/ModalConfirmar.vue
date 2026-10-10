<script setup>
defineProps({
  nombre: String,
})

const emit = defineEmits(['confirmar', 'cancelar'])
</script>

<template>
  <!-- La "sombra" que cubre toda la pantalla. Hacer clic en ella equivale a cancelar:
       es el mismo comportamiento que casi cualquier modal que el usuario ya conoce. -->
  <div class="modal__fondo" @click="emit('cancelar')">
    <!-- @click.stop evita que el clic DENTRO de la tarjeta "suba" hasta el fondo y la
         cierre por accidente; sin esto, hacer clic en cualquier parte de la tarjeta
         (incluso en los botones) cancelaría el modal antes de que el botón reaccionara. -->
    <div class="modal__tarjeta" @click.stop>
      <p>¿Eliminar la pizza "{{ nombre }}"? Esta acción no se puede deshacer.</p>
      <div class="modal__botones">
        <button type="button" class="modal__cancelar" @click="emit('cancelar')">
          Cancelar
        </button>
        <button type="button" class="modal__eliminar" @click="emit('confirmar')">
          Eliminar
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal__fondo {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(42, 23, 15, 0.6); /* café con transparencia: oscurece el fondo */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 10;
}

.modal__tarjeta {
  background-color: #fff8f6;
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 360px;
  color: #2a170f;
}

.modal__botones {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.modal__cancelar,
.modal__eliminar {
  flex: 1;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
}

.modal__cancelar {
  border: 1px solid #2a170f;
  background-color: transparent;
  color: #2a170f;
}

.modal__eliminar {
  border: none;
  background-color: #a62518;
  color: #fff8f6;
}
</style>
