<script setup>
import { usuario, cerrarSesion } from '../sesion.js'
import { useRouter } from 'vue-router'

// useRouter() es la forma de pedir el router dentro de <script setup> cuando lo
// necesitamos para algo más que un simple link (acá, para mandar al usuario a "/"
// justo después de cerrar sesión).
const router = useRouter()

async function salir() {
  await cerrarSesion()
  router.push('/')
}
</script>

<template>
  <nav class="navbar">
    <!-- :to="{ path: '/', hash: '#inicio' }" funciona sin importar en qué página estemos:
         si ya estamos en "/", el router solo baja hasta esa sección (gracias al
         scrollBehavior que quedó configurado en router/index.js); si estamos en /login o
         /admin, primero navega a "/" y, apenas llega, baja hasta la sección. Es el mismo
         link para los dos casos: no hace falta ninguna lógica especial para distinguirlos. -->
    <RouterLink :to="{ path: '/', hash: '#inicio' }" class="navbar__marca">
      <img src="/images/logo-png.png" alt="Logo Sistema Pizzería" class="navbar__logo" />
      <span>Pizzería Sogamoso</span>
    </RouterLink>

    <div class="navbar__enlaces">
      <RouterLink :to="{ path: '/', hash: '#inicio' }">Inicio</RouterLink>
      <RouterLink :to="{ path: '/', hash: '#nosotros' }">Nosotros</RouterLink>
      <RouterLink :to="{ path: '/', hash: '#menu' }">Menú</RouterLink>
    </div>

    <!-- v-if / v-else: solo una de las dos ramas se dibuja, según si hay sesión activa.
         "usuario" viene de sesion.js, así que se actualiza solo en cuanto alguien inicia
         o cierra sesión en cualquier otra parte de la aplicación. -->
    <div class="navbar__sesion">
      <RouterLink v-if="!usuario" to="/login" class="navbar__boton">Iniciar sesión</RouterLink>
      <template v-else>
        <RouterLink to="/admin" class="navbar__boton">Panel</RouterLink>
        <button type="button" class="navbar__boton navbar__boton--secundario" @click="salir">
          Cerrar sesión
        </button>
      </template>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  display: flex;
  flex-wrap: wrap; /* en pantallas angostas los enlaces pasan a una segunda fila en vez de desbordarse */
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem;
  background-color: #2a170f; /* café: fondo oscuro para que la barra resalte sobre el resto */
}

.navbar__marca {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #fff8f6;
  font-weight: bold;
  text-decoration: none;
  margin-right: auto; /* empuja los enlaces y los botones de sesión hacia la derecha */
}

.navbar__logo {
  height: 32px;
  width: 32px;
  object-fit: contain;
}

.navbar__enlaces {
  display: flex;
  gap: 1.25rem;
}

.navbar__enlaces a {
  color: #fff8f6;
  text-decoration: none;
}

.navbar__enlaces a:hover {
  color: #a62518; /* rojo tomate: mismo acento que el resto del sitio, al pasar el mouse */
}

.navbar__sesion {
  display: flex;
  gap: 0.75rem;
}

.navbar__boton {
  padding: 0.4rem 0.9rem;
  border-radius: 6px;
  border: 1px solid #fff8f6;
  background-color: transparent;
  color: #fff8f6;
  text-decoration: none;
  font-size: 0.95rem;
  cursor: pointer;
}

.navbar__boton--secundario {
  border-color: #a62518;
  background-color: #a62518;
}
</style>
