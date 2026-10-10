import { createRouter, createWebHistory } from 'vue-router'
import InicioView from '../views/InicioView.vue'
import LoginView from '../views/LoginView.vue'
import AdminView from '../views/AdminView.vue'

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

export default router
