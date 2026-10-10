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
  /* position: sticky + top: 0 lo deja pegado arriba de la pantalla mientras se scrollea
     el resto de la página, en vez de desaparecer apenas se baja un poco. z-index: 50 lo
     mantiene por encima del <canvas> de HeroScroll, que también usa "sticky" para fijar
     la animación de la pizza: sin un z-index más alto aquí, el canvas (que aparece
     después en el HTML) terminaría pintándose encima del navbar. */
  position: sticky;
  top: 0;
  z-index: 50;

  display: flex;
  flex-wrap: wrap; /* en pantallas angostas los enlaces pasan a una segunda fila en vez de desbordarse */
  align-items: center;
  gap: 1rem;
  padding: 0.85rem 1.5rem;
  background-color: #1b110a; /* espresso: mismo tono oscuro que ya usa SobreNosotros, para
                                 que el navbar se sienta parte del mismo sistema visual */
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.3); /* le da la sensación de "flotar" sobre el contenido */
}

.navbar__marca {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  color: #fbf3e7; /* papel: el mismo tono cálido que el texto claro de SobreNosotros */
  text-decoration: none;
  margin-right: auto; /* empuja los enlaces y los botones de sesión hacia la derecha */
  font-family: 'Marcellus', Georgia, serif;
  font-size: 1.15rem;
}

.navbar__logo {
  height: 36px;
  width: 36px;
  object-fit: cover;
  border-radius: 50%; /* foto circular, igual que las fotos de PizzaCard */
  border: 1.5px solid rgba(166, 37, 24, 0.45); /* mismo anillo rojo tomate tenue que las tarjetas del menú */
}

.navbar__enlaces {
  display: flex;
  gap: 1.9rem;
}

.navbar__enlaces a {
  color: rgba(251, 243, 231, 0.65);
  text-decoration: none;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.8rem;
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding-bottom: 4px;
  border-bottom: 1px solid transparent; /* reserva el espacio del subrayado para que el
                                            texto no "salte" cuando aparece en hover */
  transition: color 0.15s, border-color 0.15s;
}

.navbar__enlaces a:hover {
  color: #fbf3e7;
  border-bottom-color: #a62518; /* rojo tomate: el mismo acento que usa el resto del sitio */
}

.navbar__sesion {
  display: flex;
  gap: 0.75rem;
}

.navbar__boton {
  padding: 0.5rem 1.1rem;
  border-radius: 2px; /* casi recto en vez de pastilla: se siente más de carta fina que de app */
  border: 1px solid rgba(251, 243, 231, 0.4);
  background-color: transparent;
  color: #fbf3e7;
  text-decoration: none;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
}

.navbar__boton--secundario {
  border-color: #a62518;
  background-color: #a62518;
}
</style>
