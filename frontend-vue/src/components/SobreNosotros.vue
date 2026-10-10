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
      </div>
      <img src="/images/background.jpg" alt="Horno de la pizzería" class="nosotros__imagen" />
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

.nosotros__imagen {
  flex: 1 1 320px;
  min-width: 280px;
  max-width: 480px;
  height: 320px;
  object-fit: cover;
  /* sepia + saturate + brightness bajan un poco la intensidad de la foto para que no
     compita con el texto claro encima del fondo oscuro; es el mismo truco que se usó
     en el mockup aprobado. */
  filter: sepia(10%) saturate(90%) brightness(0.9);
}
</style>
