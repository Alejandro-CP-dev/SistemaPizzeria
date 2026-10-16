<script setup>
// Recibe la lista ya cargada desde InicioView: este componente nunca hace fetch por su
// cuenta, solo se encarga de dibujar lo que le llega.
defineProps({
  pizzas: Array,
})

// $32.000 en vez de 32000: misma idea que formatearPrecio() en PizzaCard.vue. No se
// reutiliza PizzaCard aquí a propósito: ese componente también vive dentro de
// PizzaForm.vue (vista previa del panel de admin, con los colores de tokens.css) y
// esta grilla pública necesita su propia tarjeta, más compacta y con la paleta del
// sitio público, sin arrastrar un cambio visual al panel.
function formatearPrecio(precio) {
  return '$' + Number(precio || 0).toLocaleString('es-CO')
}
</script>

<template>
  <section id="menu" class="menu">
    <div class="menu__encabezado">
      <p class="menu__ojo">Nuestra carta</p>
      <h2 class="menu__titulo">Menú</h2>
    </div>

    <!-- Línea ondulada decorativa debajo del título: un <svg> con una curva que se repite
         ("M...Q...T...T..."), igual que la carta de referencia. Es solo decoración (por eso
         aria-hidden="true": un lector de pantalla no gana nada anunciando una curva), no
         necesita JavaScript ni ninguna librería. -->
    <svg viewBox="0 0 1000 24" preserveAspectRatio="none" class="menu__linea" aria-hidden="true">
      <path
        d="M0 12 Q 25 0 50 12 T 100 12 T 150 12 T 200 12 T 250 12 T 300 12 T 350 12 T 400 12 T 450 12 T 500 12 T 550 12 T 600 12 T 650 12 T 700 12 T 750 12 T 800 12 T 850 12 T 900 12 T 950 12 T 1000 12"
        stroke="#a62518"
        stroke-width="5"
        fill="none"
        stroke-linecap="round"
      />
    </svg>

    <div class="menu__lista">
      <article v-for="pizza in pizzas" :key="pizza.id" class="menu__tarjeta">
        <div v-if="!pizza.imagen" class="menu__sin-foto">Sin foto</div>
        <img v-else :src="`/images/pizzas/${pizza.imagen}`" :alt="pizza.nombre" class="menu__foto" />

        <div class="menu__info">
          <div class="menu__fila">
            <h3 class="menu__nombre">{{ pizza.nombre }}</h3>
            <span class="menu__precio">{{ formatearPrecio(pizza.precio) }}</span>
          </div>
          <p class="menu__descripcion">{{ pizza.descripcion }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.menu {
  background-color: #f7ead9; /* crema cálida: separa esta sección del Nosotros oscuro de arriba */
  padding: 6rem 1.5rem;
  /* Compensa el navbar "sticky" (NavBar.vue) al saltar aquí desde el enlace "Menú". */
  scroll-margin-top: 80px;
}

.menu__encabezado {
  text-align: center;
  margin-bottom: 0.75rem;
}

.menu__ojo {
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a62518;
  font-weight: 600;
  margin: 0 0 0.6rem;
}

.menu__titulo {
  /* Marcellus en vez de Baloo 2: el resto del sitio (NavBar, Hero, Nosotros, Carta) ya
     usa Marcellus para sus títulos; Baloo 2 (redondeada, gruesa) era la única excepción
     y rompía la unidad tipográfica de la página. */
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 2.75rem;
  color: #2a170f;
  margin: 0;
}

.menu__linea {
  display: block;
  width: 100%;
  max-width: 400px;
  height: 16px;
  margin: 0 auto 3.5rem;
}

.menu__lista {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1.75rem;
  max-width: 1100px;
  margin: 0 auto;
}

.menu__tarjeta {
  display: flex;
  gap: 1.25rem;
  padding: 1.25rem;
  background-color: #fffaf6;
  border-radius: 4px;
  box-shadow: 0 6px 20px rgba(42, 23, 15, 0.08);
}

.menu__foto,
.menu__sin-foto {
  flex-shrink: 0;
  width: 112px;
  height: 112px;
  border-radius: 4px;
  object-fit: cover;
}

.menu__sin-foto {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ece0d2;
  color: #8a7261;
  font-size: 0.75rem;
  text-align: center;
}

.menu__info {
  min-width: 0;
}

.menu__fila {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.menu__nombre {
  margin: 0;
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 1.2rem;
  color: #2a170f;
}

.menu__precio {
  flex-shrink: 0;
  white-space: nowrap;
  font-family: 'Figtree', system-ui, sans-serif;
  font-weight: 700;
  font-size: 1rem;
  color: #a62518;
}

.menu__descripcion {
  margin: 0.4rem 0 0;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.82rem;
  line-height: 1.5;
  color: #5c4335;
}

@media (max-width: 720px) {
  .menu__lista {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 420px) {
  .menu__tarjeta {
    flex-direction: column;
  }

  .menu__foto,
  .menu__sin-foto {
    width: 100%;
    height: 160px;
  }
}
</style>
