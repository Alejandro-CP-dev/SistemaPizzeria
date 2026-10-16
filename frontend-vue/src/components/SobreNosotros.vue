<script setup>
// Sin props: esta sección siempre muestra el mismo texto fijo, no depende de nada
// que llegue del componente que la usa.
</script>

<template>
  <section id="nosotros" class="nosotros">
    <div class="nosotros__contenido">
      <div class="nosotros__texto">
        <p class="nosotros__ojo">Nuestra historia</p>
        <h2 class="nosotros__titulo">Pizza artesanal,<br />hecha con calma.</h2>
        <div class="nosotros__linea"></div>
        <p class="nosotros__parrafo">
          Masa fermentada veinticuatro horas, horno de leña y productos de pequeños
          productores de Sogamoso. Una mesa donde cada pizza se sirve como un plato,
          no como una entrega rápida.
        </p>

        <!-- Mismos 3 datos que ya cuenta el párrafo de arriba (24 horas, horno de leña,
             productores de Sogamoso), solo que resaltados aparte: le dan al texto un
             segundo punto de apoyo visual, en vez de depender solo del párrafo largo. -->
        <div class="nosotros__estadisticas">
          <div class="nosotros__estadistica">
            <span class="nosotros__estadistica-numero">24H</span>
            <span class="nosotros__estadistica-etiqueta">Fermentación lenta</span>
          </div>
          <div class="nosotros__separador" aria-hidden="true"></div>
          <div class="nosotros__estadistica">
            <span class="nosotros__estadistica-numero">100%</span>
            <span class="nosotros__estadistica-etiqueta">Horno de leña</span>
          </div>
          <div class="nosotros__separador" aria-hidden="true"></div>
          <div class="nosotros__estadistica">
            <span class="nosotros__estadistica-numero">Km 0</span>
            <span class="nosotros__estadistica-etiqueta">Productores de Sogamoso</span>
          </div>
        </div>
      </div>

      <div class="nosotros__foto">
        <img src="/images/background.jpg" alt="Horno de la pizzería" class="nosotros__imagen" />
        <div class="nosotros__insignia">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#a62518" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M8.5 14.5A2.5 2.5 0 0011 17c1.5 0 2.5-1 2.5-2.5 0-1.5-1.5-2-1-3.5.5 1 2 1.5 2 3.5a4.5 4.5 0 01-9 0C5.5 10 8 8.5 7.5 4c3 1 5.5 5 3.5 7.5 1-.5 2-2 1.5-4 2 1.5 3.5 4 3.5 7a7 7 0 01-14 0c0-4 3-6 4-10" />
          </svg>
          <span>Horno de leña real</span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* Fondo oscuro (espresso) a propósito: crea contraste con el hero y con el menú claro
   de abajo, en vez de que toda la página sea del mismo tono de crema. */
.nosotros {
  background-color: #1b110a;
  padding: 7rem 1.5rem;
  /* Mismo motivo que en HeroScroll.vue: compensa el navbar "sticky" al saltar aquí
     desde el enlace "Nosotros". */
  scroll-margin-top: 80px;
}

.nosotros__contenido {
  display: flex;
  flex-wrap: wrap; /* en celular la imagen y el texto quedan uno debajo del otro */
  align-items: center;
  gap: 3.5rem;
  max-width: 1100px;
  margin: 0 auto;
}

.nosotros__texto {
  flex: 1 1 320px;
}

.nosotros__ojo {
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a62518; /* rojo tomate: mismo acento de marca, usado aquí como etiqueta pequeña */
  font-weight: 600;
  margin: 0 0 1.25rem;
}

.nosotros__titulo {
  font-family: 'Marcellus', Georgia, serif;
  font-weight: 400;
  font-size: 2.6rem;
  line-height: 1.2;
  color: #fff8f6;
  margin: 0 0 1.5rem;
}

.nosotros__linea {
  width: 56px;
  height: 1px;
  background-color: #a62518;
  margin-bottom: 1.5rem;
}

.nosotros__parrafo {
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-style: italic;
  font-size: 1.2rem;
  line-height: 1.75;
  color: rgba(255, 248, 246, 0.72);
  max-width: 460px;
  margin: 0;
}

.nosotros__estadisticas {
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-top: 2.75rem;
}

.nosotros__estadistica {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nosotros__estadistica-numero {
  font-family: 'Marcellus', Georgia, serif;
  font-size: 1.6rem;
  color: #fff8f6;
}

.nosotros__estadistica-etiqueta {
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.68rem;
  font-weight: 500;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: rgba(255, 248, 246, 0.6);
}

.nosotros__separador {
  width: 1px;
  height: 2.4rem;
  background-color: rgba(255, 248, 246, 0.18);
}

.nosotros__foto {
  position: relative;
  flex: 1 1 320px;
  min-width: 280px;
  max-width: 480px;
}

.nosotros__imagen {
  display: block;
  width: 100%;
  height: 320px;
  object-fit: cover;
  border-radius: 4px;
  /* sepia + saturate + brightness bajan un poco la intensidad de la foto para que no
     compita con el texto claro encima del fondo oscuro; es el mismo truco que se usó
     en el mockup aprobado. */
  filter: sepia(10%) saturate(90%) brightness(0.9);
}

.nosotros__insignia {
  position: absolute;
  left: 1.5rem;
  bottom: -1.25rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.85rem 1.25rem;
  background-color: #fff8f6;
  color: #1b110a;
  border-radius: 2px;
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.35);
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

@media (max-width: 560px) {
  .nosotros__estadisticas {
    flex-wrap: wrap;
    gap: 1.25rem 2rem;
  }

  .nosotros__insignia {
    left: 1rem;
    bottom: -1rem;
    padding: 0.7rem 1rem;
    font-size: 0.72rem;
  }
}
</style>
