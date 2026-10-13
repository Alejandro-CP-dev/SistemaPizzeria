<script setup>
import { usuario, cerrarSesion } from '../sesion.js'
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * A diferencia de NavBar.vue (que usa @click porque solo necesita volver a "/"), aquí
 * además hay que ESPERAR a que cerrarSesion() termine su fetch a /api/logout antes de
 * navegar: si se navegara primero, router.push podría disparar el guard de /admin con la
 * sesión todavía a medio cerrar en el servidor.
 */
async function salir() {
  await cerrarSesion()
  router.push('/login')
}
</script>

<template>
  <!-- position: fixed + backdrop-filter: blur: el encabezado queda "flotando" sobre el
       contenido mientras se hace scroll dentro de /admin, con el fondo translúcido del
       diseño de referencia (no un color sólido), igual que docs/diseno-admin-stitch.html. -->
  <header class="admin-header">
    <div class="admin-header__fila">
      <div class="admin-header__marca">
        <img src="/images/logo-png.png" alt="Logo Sistema Pizzería" class="admin-header__logo" />
        <div class="admin-header__titulos">
          <span class="admin-header__titulo">Panel de administración</span>
          <span class="admin-header__subtitulo">Sistema Pizzería</span>
        </div>
      </div>

      <!-- El diseño original trae 3 enlaces (Catálogo de Pizzas / Categorías & Masas /
           Ingredientes & Costos); los dos últimos no existen en este proyecto (no hay
           pantallas de categorías ni de costos), así que solo queda el enlace real: volver
           a ver el menú tal como lo ve un cliente. -->
      <nav class="admin-header__nav">
        <RouterLink to="/" class="admin-header__nav-enlace">Ver carta pública</RouterLink>
      </nav>

      <div class="admin-header__sesion">
        <div class="admin-header__persona" v-if="usuario">
          <span class="material-symbols-outlined admin-header__persona-icono">person</span>
          <div class="admin-header__persona-texto">
            <span class="admin-header__persona-nombre">{{ usuario.nombre }}</span>
            <span class="admin-header__persona-rol">Admin</span>
          </div>
        </div>

        <button type="button" class="admin-header__salir" @click="salir">
          <span class="material-symbols-outlined">logout</span>
          <span>Cerrar sesión</span>
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.admin-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background-color: rgba(255, 248, 246, 0.9); /* --surface al 90%: no hay var() para "90%", así que se escribe en rgba */
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: 0 1px 8px rgba(44, 24, 16, 0.06);
}

.admin-header__fila {
  height: 80px;
  width: 100%;
  padding: 0 var(--margin-mobile);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
}

@media (min-width: 768px) {
  .admin-header__fila {
    padding: 0 var(--margin);
  }
}

.admin-header__marca {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  min-width: 0; /* permite que el texto se trunque si el logo + nav + sesión no caben */
}

.admin-header__logo {
  height: 32px;
  width: auto;
  object-fit: contain;
}

.admin-header__titulos {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-header__titulo {
  font: var(--fw-headline-sm) var(--fs-headline-sm)/var(--lh-headline-sm) var(--font-headline);
  color: var(--on-surface);
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-header__subtitulo {
  font: var(--fw-label-sm) var(--fs-label-sm)/var(--lh-label-sm) var(--font-body);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--on-surface-variant);
}

/* El enlace "Ver carta pública" solo compite por espacio en pantallas medianas en
   adelante; en celular, logo + pill de usuario + botón de salir ya llenan el ancho. */
.admin-header__nav {
  display: none;
}

@media (min-width: 768px) {
  .admin-header__nav {
    display: flex;
  }
}

.admin-header__nav-enlace {
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-lg);
  background-color: var(--surface-container-low);
  color: var(--on-surface-variant);
  text-decoration: none;
  font: var(--fw-label-lg) var(--fs-label-lg)/var(--lh-label-lg) var(--font-body);
  transition: color 0.15s, background-color 0.15s;
}

.admin-header__nav-enlace:hover {
  color: var(--on-surface);
  background-color: var(--surface-container);
}

.admin-header__sesion {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.admin-header__persona {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  background-color: var(--surface-container-low);
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-full);
  box-shadow: 0 1px 4px rgba(44, 24, 16, 0.04);
}

.admin-header__persona-icono {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background-color: var(--primary);
  color: var(--on-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px !important;
}

.admin-header__persona-texto {
  display: none;
  flex-direction: column;
  text-align: left;
  line-height: 1;
}

@media (min-width: 480px) {
  .admin-header__persona-texto {
    display: flex;
  }
}

.admin-header__persona-nombre {
  font: var(--fw-label-md) var(--fs-label-md)/1 var(--font-body);
  color: var(--on-surface);
}

.admin-header__persona-rol {
  font: var(--fw-label-sm) var(--fs-label-sm)/1 var(--font-body);
  color: var(--primary);
}

.admin-header__salir {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-lg);
  background-color: var(--surface-container);
  color: var(--on-surface);
  font: var(--fw-label-md) var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  cursor: pointer;
  transition: background-color 0.15s;
}

.admin-header__salir .material-symbols-outlined {
  font-size: 16px;
}

.admin-header__salir:hover {
  background-color: var(--surface-container-high);
}
</style>
