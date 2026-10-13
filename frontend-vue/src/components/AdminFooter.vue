<script setup>
// Sin props ni lógica: es texto fijo, igual que SobreNosotros.vue en el sitio público.
</script>

<template>
  <footer class="admin-footer">
    <div class="admin-footer__fila">
      <div class="admin-footer__marca">
        <span class="admin-footer__nombre">Sistema Pizzería</span>
        <span class="admin-footer__separador">•</span>
        <span class="admin-footer__lema">Artesanal &amp; Horno de Leña</span>
      </div>
      <span class="admin-footer__derechos">
        © 2026 Sistema Pizzería. Panel de Administración de Menú.
      </span>
    </div>
  </footer>
</template>

<style scoped>
.admin-footer {
  width: 100%;
  background-color: var(--surface-container-low);
  margin-top: auto;
  padding: var(--space-lg) 0;
  box-shadow: 0 -1px 6px rgba(44, 24, 16, 0.03);
}

.admin-footer__fila {
  width: 100%;
  padding: 0 var(--margin-mobile);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-sm);
  color: var(--on-surface-variant);
  text-align: center;
}

@media (min-width: 768px) {
  .admin-footer__fila {
    padding: 0 var(--margin);
    flex-direction: row;
    text-align: left;
  }
}

.admin-footer__marca {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.admin-footer__nombre {
  font: 600 var(--fs-label-md)/var(--lh-label-md) var(--font-body);
  color: var(--on-surface);
}

.admin-footer__separador {
  color: var(--outline-variant);
}

.admin-footer__lema,
.admin-footer__derechos {
  font: var(--fw-body-sm) var(--fs-body-sm)/var(--lh-body-sm) var(--font-body);
}
</style>
