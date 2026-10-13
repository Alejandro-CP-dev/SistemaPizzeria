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
      <p class="modal__ojo">Confirmar eliminación</p>
      <p class="modal__texto">¿Eliminar la pizza "{{ nombre }}"? Esta acción no se puede deshacer.</p>
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
  background-color: rgba(27, 17, 10, 0.65); /* espresso con transparencia: oscurece el fondo */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 10;
}

.modal__tarjeta {
  background-color: #fff8f6;
  border-radius: 4px;
  border-top: 3px solid #a62518;
  padding: 1.75rem;
  max-width: 360px;
  font-family: 'Figtree', system-ui, sans-serif;
}

.modal__ojo {
  margin: 0 0 0.6rem;
  font-size: 0.72rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #a62518;
  font-weight: 600;
}

.modal__texto {
  margin: 0;
  color: #2a170f;
  font-size: 0.95rem;
  line-height: 1.5;
}

.modal__botones {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.4rem;
}

.modal__cancelar,
.modal__eliminar {
  flex: 1;
  padding: 0.6rem;
  border-radius: 2px;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;
}

.modal__cancelar {
  border: 1px solid #2a170f;
  background-color: transparent;
  color: #2a170f;
}

.modal__cancelar:hover {
  background-color: #2a170f;
  color: #fff8f6;
}

.modal__eliminar {
  border: none;
  background-color: #a62518;
  color: #fff8f6;
}

.modal__eliminar:hover {
  background-color: #8a1f15;
}
</style>
