import { ref } from 'vue'

// Este "ref" se crea UNA sola vez, justo cuando este archivo se carga por primera vez
// (la primera vez que algún componente hace "import ... from './sesion.js'").
// JavaScript solo ejecuta un mismo módulo una vez y reutiliza el resultado en cada import
// siguiente: por eso NavBar, LoginView y AdminView, aunque son archivos distintos, al
// importar "usuario" están importando exactamente LA MISMA variable reactiva. Si uno de
// ellos la cambia (por ejemplo, al iniciar sesión), los demás la ven cambiada de inmediato,
// sin que nadie tenga que pasarla por props ni avisarles manualmente. Esto es lo mismo que
// resuelve una librería como Pinia, solo que aquí lo logramos con las herramientas básicas
// de Vue (ref) más una regla simple de JavaScript (los módulos se ejecutan una sola vez).
export const usuario = ref(null)

/**
 * Le pregunta al backend si hay una sesión activa en este momento.
 * Se usa, sobre todo, justo después de recargar la página (F5): en ese instante
 * "usuario" vuelve a valer null porque todo el JavaScript se reinicia desde cero,
 * aunque la cookie de sesión siga viva en el navegador. Esta función reconstruye
 * "usuario" a partir de esa cookie.
 */
export async function verificarSesion() {
  try {
    const respuesta = await fetch('/api/sesion')
    if (respuesta.ok) {
      usuario.value = await respuesta.json()
    } else {
      // 401: el Servlet dice que no hay sesión activa.
      usuario.value = null
    }
  } catch (e) {
    // No se pudo ni contactar al backend (por ejemplo, Tomcat apagado):
    // para la interfaz esto se trata igual que "no hay sesión".
    usuario.value = null
  }
}

/**
 * Intenta iniciar sesión con el correo y la clave que escribió el usuario.
 * Devuelve la respuesta COMPLETA de fetch (sin leerla como JSON todavía) para que quien
 * llama a esta función (LoginView) pueda revisar el código de estado (200, 401, 400)
 * y decidir qué mensaje mostrar en cada caso.
 */
export async function iniciarSesion(correo, clave) {
  const respuesta = await fetch('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ correo, clave }),
  })
  if (respuesta.ok) {
    usuario.value = await respuesta.json()
  }
  return respuesta
}

/** Cierra la sesión en el backend (invalida la cookie) y limpia el estado local. */
export async function cerrarSesion() {
  await fetch('/api/logout', { method: 'POST' })
  usuario.value = null
}
