# Sistema Pizzería

Proyecto académico (SENA ADSO) de un sistema de pedidos para una pizzería, dividido en dos carpetas independientes:

- **frontend-vue**: interfaz web hecha con Vue 3 y Vite.
- **backend-java**: API hecha con Servlets de Java (javax), empaquetada como `.war` para desplegar en Apache Tomcat.

## Cómo ejecutar

Hay que levantar las tres piezas **en este orden**, porque cada una depende de la
anterior: sin MySQL arriba, Tomcat despliega pero cualquier petición a la API se queda
esperando una conexión que nunca llega; sin el backend arriba, el frontend carga pero el
menú nunca termina de cargar.

### 1) MySQL (por ejemplo con XAMPP)

1. Abre XAMPP Control Panel y dale **Start** a MySQL. Espera a que el log
   (`xampp/mysql/data/mysql_error.log`) muestre la línea `ready for connections` antes de
   seguir; si MySQL se queda a medio arrancar, la API se queda colgada sin dar ningún
   error (ver nota sobre `connectTimeout` en `db.properties.example`).
2. Crea la base de datos y sus tablas corriendo el script completo:
   `backend-java/database/pizzeria.sql` (por ejemplo desde phpMyAdmin, MySQL Workbench o
   `mysql -u root < backend-java/database/pizzeria.sql`). Esto crea la base `pizzeria`,
   las tablas `Pizza` y `Usuario`, y deja cargadas 6 pizzas y un usuario administrador.
3. Copia `backend-java/src/main/resources/db.properties.example` como
   `db.properties` (mismo directorio) y ajusta `db.usuario`/`db.clave` si tu MySQL no usa
   `root` sin contraseña. Este archivo no se sube a git porque tiene una contraseña.

### 2) Backend (Java + Tomcat), desplegado en `/pizzeria`

Requiere Maven y un Apache Tomcat 9 instalado.

```bash
cd backend-java
mvn clean package
```

Esto genera `target/pizzeria.war` (el nombre viene de `<finalName>` en `pom.xml`).
Despliégalo en Tomcat (copiándolo a `webapps/`, o desde NetBeans) y verifica que quede
publicado con la ruta `/pizzeria` — es la que el proxy de Vite espera encontrar en el
puerto 8080 (`frontend-vue/vite.config.js`). Puedes probarlo directo, sin el frontend,
visitando `http://localhost:8080/pizzeria/api/pizzas`.

### 3) Frontend (Vue + Vite)

```bash
cd frontend-vue
npm install
npm run dev
```

Esto levanta un servidor de desarrollo con recarga en caliente en `http://localhost:5173`,
que es la URL que hay que abrir en el navegador (no la de Tomcat: las peticiones a `/api`
viajan desde ahí, a través del proxy de Vite, hasta el backend). Para generar la build de
producción:

```bash
npm run build
```
