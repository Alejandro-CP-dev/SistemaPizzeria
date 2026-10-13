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
    <!-- Panel izquierdo: solo de marca, se oculta en pantallas angostas (ver <style>).
         Usa la misma foto y el mismo filtro sepia que SobreNosotros.vue, para que se
         sienta parte del mismo sistema visual en vez de una pantalla aparte. -->
    <div class="login__marca">
      <img src="/images/background.jpg" alt="" class="login__marca-imagen" aria-hidden="true" />
      <div class="login__marca-contenido">
        <img src="/images/logo-png.png" alt="Logo Sistema Pizzería" class="login__marca-logo" />
        <p class="login__ojo">Acceso privado</p>
        <h1 class="login__marca-titulo">Panel<br />administrativo</h1>
        <div class="login__linea"></div>
        <p class="login__marca-frase">Gestiona la carta, un plato a la vez.</p>
      </div>
    </div>

    <div class="login__panel">
      <form class="login__formulario" @submit.prevent="enviar">
        <p class="login__ojo login__ojo--movil">Pizzería Sogamoso</p>
        <h2 class="login__titulo">Iniciar sesión</h2>
        <p class="login__subtitulo">Ingresa con tu correo y contraseña de administrador.</p>

        <label class="login__campo">
          <span>Correo</span>
          <input v-model="correo" type="email" autocomplete="username" />
        </label>

        <label class="login__campo">
          <span>Contraseña</span>
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
    </div>
  </section>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

/* --- Panel de marca (izquierda) --- */
.login__marca {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  background-color: #1b110a;
  overflow: hidden;
}

.login__marca-imagen {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.35;
  /* mismo truco que en SobreNosotros.vue: baja la intensidad de la foto para que no
     compita con el texto claro que va encima. */
  filter: sepia(15%) saturate(85%) brightness(0.55);
}

.login__marca-contenido {
  position: relative;
  max-width: 360px;
}

.login__marca-logo {
  width: 52px;
  height: 52px;
  object-fit: cover;
  border-radius: 50%;
  border: 1.5px solid rgba(166, 37, 24, 0.55);
  margin-bottom: 2rem;
}

.login__ojo {
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a62518;
  font-weight: 600;
  margin: 0 0 1.25rem;
}

.login__marca-titulo {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 2.4rem;
  line-height: 1.2;
  color: #fbf3e7;
  margin: 0 0 1.5rem;
}

.login__linea {
  width: 56px;
  height: 1px;
  background-color: #a62518;
  margin-bottom: 1.5rem;
}

.login__marca-frase {
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-style: italic;
  font-size: 1.15rem;
  line-height: 1.6;
  color: rgba(251, 243, 231, 0.72);
  margin: 0;
}

/* --- Panel del formulario (derecha) --- */
.login__panel {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff8f6;
  padding: 2.5rem 1.5rem;
}

.login__formulario {
  display: flex;
  flex-direction: column;
  gap: 1.15rem;
  width: 100%;
  max-width: 360px;
}

.login__ojo--movil {
  display: none; /* solo aparece cuando el panel de marca se oculta, ver media query */
}

.login__titulo {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  color: #2a170f;
  font-size: 1.75rem;
  margin: 0;
}

.login__subtitulo {
  font-family: 'Figtree', system-ui, sans-serif;
  color: #7a5b4c;
  font-size: 0.9rem;
  margin: -0.5rem 0 0.5rem;
}

.login__campo {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.login__campo span {
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.75rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-weight: 600;
  color: #7a5b4c;
}

.login__campo input {
  padding: 0.6rem 0.1rem;
  border: none;
  border-bottom: 1.5px solid #e0cfc3;
  background: transparent;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 1rem;
  color: #2a170f;
  transition: border-color 0.15s;
}

.login__campo input:focus {
  outline: none;
  border-bottom-color: #a62518;
}

.login__clave {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.login__clave input {
  flex: 1;
}

.login__ver {
  padding: 0.4rem 0.1rem;
  border: none;
  background: transparent;
  color: #a62518;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  cursor: pointer;
  white-space: nowrap;
}

.login__error {
  margin: 0;
  padding: 0.6rem 0.8rem;
  border-left: 3px solid #a62518;
  background-color: rgba(166, 37, 24, 0.08);
  color: #a62518;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.85rem;
}

.login__boton {
  margin-top: 0.5rem;
  padding: 0.75rem;
  border: none;
  border-radius: 2px; /* casi recto, igual que los botones del navbar */
  background-color: #a62518;
  color: #fbf3e7;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background-color 0.15s;
}

.login__boton:hover:not(:disabled) {
  background-color: #8a1f15;
}

.login__boton:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login__volver {
  text-align: center;
  color: #7a5b4c;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.8rem;
  text-decoration: none;
}

.login__volver:hover {
  color: #a62518;
}

/* Por debajo de 860px no cabe el panel de marca sin aplastar el formulario: se oculta
   y en su lugar aparece una etiqueta pequeña con el nombre de la pizzería arriba del
   formulario, para no perder del todo la identidad de marca. */
@media (max-width: 860px) {
  .login {
    grid-template-columns: 1fr;
  }

  .login__marca {
    display: none;
  }

  .login__panel {
    min-height: 100vh;
  }

  .login__ojo--movil {
    display: block;
    text-align: center;
  }
}
</style>
