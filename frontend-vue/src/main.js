import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'

const app = createApp(App)
// app.use(router) registra el router en toda la aplicación: a partir de aquí,
// <RouterView> y <RouterLink> quedan disponibles en cualquier componente sin
// necesidad de importarlos uno por uno.
app.use(router)
app.mount('#app')
