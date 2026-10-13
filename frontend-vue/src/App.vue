<script setup>
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'

// AdminView.vue trae su propio encabezado (AdminHeader.vue), pensado para el panel y
// no para el sitio público: mostrar el NavBar público ENCIMA de eso duplicaría la
// navegación y no tendría sentido (NavBar lleva a secciones de "/" que no existen
// dentro de /admin). Por eso se oculta justo en esa única ruta.
const route = useRoute()
</script>

<template>
  <NavBar v-if="route.path !== '/admin'" />
  <RouterView />
</template>
