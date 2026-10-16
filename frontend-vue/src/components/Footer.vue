<script setup>
// Sin props ni fetch, igual que SobreNosotros.vue: solo cierra la página pública con
// la marca, la navegación repetida y una nota del proyecto. No existía ningún pie de
// página público antes de esto (AdminFooter.vue es solo del panel).
</script>

<template>
  <footer class="footer">
    <div class="footer__fila">
      <div class="footer__marca">
        <RouterLink :to="{ path: '/', hash: '#inicio' }" class="footer__logo-enlace">
          <img src="/images/logo-fondo.png" alt="Logo Sistema Pizzería" class="footer__logo" />
          <span>Pizzería Sogamoso</span>
        </RouterLink>
        <p class="footer__eslogan">Pizza artesanal hecha con calma: horno de leña y masa de 24 horas.</p>

        <!-- Botones sin href real (no hay cuentas de redes todavía): aria-label los
             identifica igual para quien use un lector de pantalla, y por eso son
             <button>, no <a href="#">, que prometería un destino que no existe. -->
        <div class="footer__redes">
          <button type="button" aria-label="Instagram" class="footer__red">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <rect x="3" y="3" width="18" height="18" rx="5" />
              <circle cx="12" cy="12" r="4" />
              <circle cx="17.2" cy="6.8" r="1" />
            </svg>
          </button>
          <button type="button" aria-label="Facebook" class="footer__red">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M15 4h-2a4 4 0 00-4 4v3H7v4h2v7h4v-7h3l1-4h-4V8a1 1 0 011-1h3z" />
            </svg>
          </button>
          <button type="button" aria-label="WhatsApp" class="footer__red">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M21 12a9 9 0 11-4.1-7.56L21 3l-1.2 4.1A8.96 8.96 0 0121 12z" />
              <path d="M8.5 9.5c.3 2.8 2.7 5.2 5.5 5.5" />
            </svg>
          </button>
        </div>
      </div>

      <div class="footer__columna">
        <p class="footer__titulo-columna">Navegación</p>
        <RouterLink :to="{ path: '/', hash: '#inicio' }">Inicio</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#nosotros' }">Nosotros</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#carta' }">Carta</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#menu' }">Menú</RouterLink>
      </div>

      <div class="footer__columna">
        <p class="footer__titulo-columna">Proyecto</p>
        <p class="footer__texto">
          Proyecto académico SENA<br />
          Tecnología ADSO<br />
          Vue.js + Java (Tomcat/MySQL)
        </p>
      </div>
    </div>

    <div class="footer__barra">
      <span>© 2026 Pizzería Sogamoso</span>
      <span>Hecho con Vue.js</span>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background-color: #120b06; /* más oscuro que el espresso (#1b110a) del resto: marca el cierre de la página */
  padding: 4.5rem 1.5rem 2rem;
}

.footer__fila {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 3rem;
  max-width: 1100px;
  margin: 0 auto;
}

.footer__marca {
  flex: 1 1 280px;
  min-width: 240px;
}

.footer__logo-enlace {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  text-decoration: none;
  color: #fff8f6;
  font-family: 'Marcellus', Georgia, serif;
  font-size: 1rem;
}

.footer__logo {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.footer__eslogan {
  margin: 0.9rem 0 0;
  max-width: 280px;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.82rem;
  line-height: 1.6;
  color: rgba(255, 248, 246, 0.6);
}

.footer__redes {
  display: flex;
  gap: 0.6rem;
  margin-top: 1.25rem;
}

.footer__red {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid rgba(255, 248, 246, 0.28);
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 248, 246, 0.8);
  cursor: pointer;
}

.footer__red:hover {
  border-color: var(--primary, #a62518);
  color: #fff8f6;
}

.footer__columna {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.footer__titulo-columna {
  margin: 0 0 0.3rem;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: rgba(255, 248, 246, 0.42);
}

.footer__columna a {
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.85rem;
  color: rgba(255, 248, 246, 0.72);
  text-decoration: none;
}

.footer__columna a:hover {
  color: #fff8f6;
}

.footer__texto {
  margin: 0;
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.85rem;
  line-height: 1.9;
  color: rgba(255, 248, 246, 0.72);
}

.footer__barra {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 0.75rem;
  max-width: 1100px;
  margin: 2.75rem auto 0;
  padding-top: 1.4rem;
  border-top: 1px solid rgba(255, 248, 246, 0.1);
  font-family: 'Figtree', system-ui, sans-serif;
  font-size: 0.75rem;
  color: rgba(255, 248, 246, 0.42);
}
</style>
