<script setup>
// Sin props: esta sección siempre muestra el mismo texto fijo, no depende de nada
// que llegue del componente que la usa.
</script>

<template>
  <section id="nosotros" class="nosotros">
    <img src="/images/background.jpg" alt="Horno de la pizzería" class="nosotros__imagen" />
    <div class="nosotros__texto">
      <h2>Nosotros</h2>
      <p>
        Somos una pizzería artesanal de Sogamoso, Boyacá. Desde nuestra cocina preparamos
        cada pizza con masa hecha a mano, horneada en leña y con ingredientes frescos de
        la región. Lo que empezó como una idea familiar hoy es el lugar donde nuestros
        vecinos se reúnen a comer algo distinto, hecho con cariño y sin afán.
      </p>
    </div>
  </section>
</template>

<style scoped>
.nosotros {
  display: flex;
  flex-wrap: wrap; /* en celular la imagen y el texto quedan uno debajo del otro */
  align-items: center;
  gap: 2rem;
  padding: 3rem 1.5rem;
  max-width: 1100px;
  margin: 0 auto;
}

.nosotros__imagen {
  flex: 1 1 320px; /* crece y encoge, pero nunca por debajo de 320px de ancho */
  min-width: 280px;
  max-width: 480px;
  border-radius: 12px;
  object-fit: cover;
}

.nosotros__texto {
  flex: 1 1 320px;
  color: #2a170f; /* café: texto normal */
}

.nosotros__texto h2 {
  color: #a62518; /* rojo tomate: mismo acento que los demás títulos del sitio */
  margin-top: 0;
}

.nosotros__texto p {
  line-height: 1.6;
}
</style>
