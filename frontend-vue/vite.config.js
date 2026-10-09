import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      // El frontend llama a rutas propias como /api/pizzas; Vite las reenvía
      // a Tomcat, que las tiene publicadas bajo /pizzeria/api/... (ver
      // <finalName>pizzeria</finalName> en backend-java/pom.xml).
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (ruta) => '/pizzeria' + ruta,
        // Tomcat crea la cookie de sesión (JSESSIONID) con Path=/pizzeria,
        // porque esa es la ruta real de la app dentro de Tomcat. Pero el
        // navegador ve el sitio como http://localhost:5173/ (sin /pizzeria),
        // así que si no se reescribe el Path, el navegador descarta la
        // cookie o no la reenvía en las siguientes peticiones y la sesión
        // se "pierde" en cada request. cookiePathRewrite la cambia a "/"
        // para que el navegador la guarde y la mande siempre.
        cookiePathRewrite: '/',
      },
    },
  },
})
