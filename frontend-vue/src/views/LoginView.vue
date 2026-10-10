<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { iniciarSesion } from '../sesion.js'

const router = useRouter()

// Los campos empiezan VACÍOS a propósito: nunca se escribe una contraseña real en el
// código, ni siquiera para "probar rápido". Quien use el login la escribe a mano.
const correo = ref('')
const clave = ref('')
const verClave = ref(false) // controla si el campo de clave se ve como texto o como "••••"
const error = ref('')
const enviando = ref(false)

async function enviar() {
  error.value = ''

  if (!correo.value.trim() || !clave.value.trim()) {
    error.value = 'Completa correo y contraseña.'
    return
  }

  enviando.value = true
  try {
    const respuesta = await iniciarSesion(correo.value.trim(), clave.value)
    if (respuesta.ok) {
      router.push('/admin')
    } else if (respuesta.status === 401) {
      error.value = 'Correo o contraseña incorrectos.'
    } else {
      error.value = 'No se pudo iniciar sesión. Intenta de nuevo más tarde.'
    }
  } catch (e) {
    console.error(e)
    error.value = 'No se pudo conectar con el servidor.'
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <section class="login">
    <form class="login__formulario" @submit.prevent="enviar">
      <h1>Iniciar sesión</h1>

      <label class="login__campo">
        Correo
        <input v-model="correo" type="email" autocomplete="username" />
      </label>

      <label class="login__campo">
        Contraseña
        <div class="login__clave">
          <!-- :type cambia entre "password" y "text" según el valor de verClave: es un
               v-bind normal, solo que lo que varía no es un color ni un src, sino el
               atributo type del input. Por eso el botón de al lado solo necesita invertir
               un booleano, nada más. -->
          <input
            :type="verClave ? 'text' : 'password'"
            v-model="clave"
            autocomplete="current-password"
          />
          <button type="button" class="login__ver" @click="verClave = !verClave">
            {{ verClave ? 'Ocultar' : 'Ver' }}
          </button>
        </div>
      </label>

      <p v-if="error" class="login__error">{{ error }}</p>

      <button type="submit" class="login__boton" :disabled="enviando">
        {{ enviando ? 'Entrando...' : 'Entrar' }}
      </button>

      <RouterLink to="/" class="login__volver">Volver al inicio</RouterLink>
    </form>
  </section>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff8f6;
  padding: 1.5rem;
}

.login__formulario {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  max-width: 360px;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(42, 23, 15, 0.15);
}

.login__formulario h1 {
  margin: 0 0 0.5rem;
  color: #a62518;
  font-size: 1.4rem;
  text-align: center;
}

.login__campo {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  color: #2a170f;
  font-size: 0.9rem;
}

.login__campo input {
  padding: 0.5rem;
  border: 1px solid #2a170f;
  border-radius: 6px;
  font-size: 1rem;
}

.login__clave {
  display: flex;
  gap: 0.5rem;
}

.login__clave input {
  flex: 1;
}

.login__ver {
  padding: 0 0.75rem;
  border: 1px solid #2a170f;
  border-radius: 6px;
  background-color: #fff8f6;
  cursor: pointer;
}

.login__error {
  margin: 0;
  color: #a62518;
  font-size: 0.9rem;
}

.login__boton {
  padding: 0.6rem;
  border: none;
  border-radius: 6px;
  background-color: #2a6a48;
  color: #fff8f6;
  font-size: 1rem;
  cursor: pointer;
}

.login__boton:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login__volver {
  text-align: center;
  color: #2a170f;
  font-size: 0.85rem;
}
</style>
