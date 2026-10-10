import { createRouter, createWebHistory } from 'vue-router'
import InicioView from '../views/InicioView.vue'
import LoginView from '../views/LoginView.vue'
import AdminView from '../views/AdminView.vue'
import { usuario, verificarSesion } from '../sesion.js'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'inicio', component: InicioView },
    { path: '/login', name: 'login', component: LoginView },
    // meta.requiereSesion no hace nada por sí solo: es solo una etiqueta que este
    // objeto de ruta lleva pegada. Quien le da sentido es el guard "beforeEach" que
    // se agrega más abajo.
    { path: '/admin', name: 'admin', component: AdminView, meta: { requiereSesion: true } },
  ],
  // Sin esto, cuando el link de NavBar lleva a algo como "/#nosotros", el navegador
  // cambiaría la URL pero se quedaría quieto. scrollBehavior le dice al router qué
  // hacer en CADA navegación: si la ruta de destino trae un "hash" (la parte después
  // del #), baja hasta el elemento con ese id; si no trae hash, sube al inicio de la
  // página (por ejemplo, al entrar a /login después de haber scrolleado en el inicio).
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0 }
  },
})

// beforeEach se ejecuta ANTES de dibujar cualquier ruta nueva. Si la ruta de destino
// no pidió sesión (meta.requiereSesion no existe), lo dejamos pasar de inmediato.
router.beforeEach(async (hacia) => {
  if (!hacia.meta.requiereSesion) {
    return true
  }

  // Se vuelve a preguntar al backend aunque "usuario" ya tenga un valor: es la única
  // forma de que esto siga funcionando después de recargar la página con F5. Al recargar,
  // TODO el JavaScript (incluido este módulo) se reinicia desde cero y "usuario" vuelve
  // a valer null, aunque la cookie de sesión del navegador siga viva.
  await verificarSesion()

  // Importante: este guard es solo comodidad para la interfaz (evita mostrar el panel un
  // instante para luego sacar al usuario). La seguridad real está en el backend: si
  // alguien se lo saltara editando el JavaScript del navegador, PizzaServlet seguiría
  // respondiendo 401 a cualquier POST/PUT/DELETE sin sesión válida.
  if (!usuario.value) {
    return '/login'
  }
  return true
})

export default router
