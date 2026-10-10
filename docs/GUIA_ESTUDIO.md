# Guía de estudio — Sistema Pizzería

Esta guía es para prepararte la exposición oral del Tema 6 (Frontend con Vue.js). No
repite el código línea por línea (eso ya está comentado en cada archivo); te explica
**qué hace cada archivo y por qué existe**, cómo viaja una petición de principio a fin, y
el patrón más importante de Vue que vas a tener que defender: *las props bajan, los
eventos suben*.

## 1. Mapa de archivos

### Frontend (`frontend-vue/src/`)

| Archivo | Qué hace | Por qué existe |
|---|---|---|
| `main.js` | Crea la aplicación de Vue, registra el router y la monta en `#app`. | Es el único punto de entrada: todo lo demás se cuelga de aquí. |
| `App.vue` | Pone `<NavBar />` (siempre visible) y `<RouterView />` (cambia según la URL). | Es el "marco" que envuelve cualquier página. |
| `router/index.js` | Define las 3 rutas (`/`, `/login`, `/admin`), el `scrollBehavior` (bajar a un `#ancla`) y el guard que protege `/admin`. | Sin él, cambiar de URL no cambiaría qué se ve en pantalla: Vue por sí solo no sabe de rutas. |
| `sesion.js` | Expone `usuario` (un `ref` compartido) y las funciones `verificarSesion`, `iniciarSesion`, `cerrarSesion`. | Es el "estado global" del login, sin necesitar Pinia: todo componente que lo importa ve la misma variable. |
| `views/InicioView.vue` | Carga las pizzas (`GET /api/pizzas`) y arma la página pública con `HeroScroll`, `SobreNosotros` y `MenuPizzas`. | Es la pantalla de la ruta `/`. |
| `views/LoginView.vue` | Formulario de correo/clave; llama a `iniciarSesion` y redirige a `/admin` si funciona. | Pantalla de la ruta `/login`. |
| `views/AdminView.vue` | Carga las pizzas, decide POST/PUT/DELETE según la acción, coordina el modal del formulario y el de confirmación. | Pantalla de la ruta `/admin`; es el "cerebro" del CRUD. |
| `components/NavBar.vue` | Logo, enlaces a las secciones de `/` y botones de sesión. | Se repite en todas las páginas, así que vive en `App.vue`, no en cada vista. |
| `components/HeroScroll.vue` | Dibuja en un `<canvas>` el fotograma de `public/frames/` que corresponde al scroll. | Es el efecto visual de bienvenida de la página de inicio. |
| `components/SobreNosotros.vue` | Texto fijo sobre la pizzería. | Sección estática, sin props ni fetch: lo más simple que hay en el proyecto. |
| `components/MenuPizzas.vue` | Recibe `pizzas` (prop) y dibuja una `PizzaCard` por cada una. | Separa "mostrar la cuadrícula" de "conseguir los datos" (eso lo hace `InicioView`). |
| `components/PizzaCard.vue` | Una tarjeta: foto (o "Sin foto"), nombre, descripción, precio formateado. | Se reutiliza en 3 lugares: el menú público, la vista previa del formulario y (si quisieras) la tabla del admin. |
| `components/PizzaForm.vue` | Formulario de crear/editar con vista previa en vivo; valida y emite, nunca hace fetch. | Separa "la forma de los datos" de "qué hacer con ellos" (eso lo decide `AdminView`). |
| `components/PizzaTabla.vue` | Tabla del panel con foto, nombre, descripción, precio y botones. | Es la vista de "lista" del admin, paralela a `MenuPizzas` en la vista pública. |
| `components/ModalConfirmar.vue` | Pregunta "¿Eliminar X?" antes de borrar. | Evita que un clic accidental en "Eliminar" borre algo sin avisar. |
| `vite.config.js` | Configura el proxy de `/api` hacia Tomcat (puerto 8080, ruta `/pizzeria`) y el `cookiePathRewrite`. | Sin esto, el navegador vería dos orígenes distintos (5173 y 8080) y la sesión (cookie) no sobreviviría entre peticiones. |

### Backend (`backend-java/src/main/java/.../`)

| Archivo | Qué hace | Por qué existe |
|---|---|---|
| `servlets/PizzaServlet.java` | Atiende `/api/pizzas` y `/api/pizzas/{id}`: GET público, POST/PUT/DELETE con sesión. | Es la puerta de entrada HTTP para todo lo de pizzas. |
| `servlets/AuthServlet.java` | Atiende `/api/login`, `/api/logout`, `/api/sesion`. | Es la puerta de entrada HTTP para todo lo de sesión. |
| `datos/PizzaDAO.java` | Las 5 consultas SQL de la tabla `Pizza` (listar, buscar, insertar, actualizar, eliminar). | Separa "hablar HTTP" (el Servlet) de "hablar SQL" (el DAO): si un día cambiaras de MySQL a otra base, solo tocarías esta clase. |
| `datos/UsuarioDAO.java` | Valida login y busca un usuario por id (sin exponer nunca la clave). | Mismo motivo: aísla el SQL de la tabla `Usuario`. |
| `datos/Conexion.java` | Lee `db.properties` y entrega una `Connection` nueva cada vez que se le pide. | Un solo lugar donde cambiar la URL/usuario/clave de MySQL si algo cambia. |
| `modelo/Pizza.java` | Clase con los 5 campos de una pizza (camelCase) y sus getters/setters. | Gson la usa como "molde" para convertir filas de la BD en JSON y viceversa. |
| `modelo/Usuario.java` | Clase con los datos del usuario, **sin el campo clave**. | Así es imposible que la contraseña salga en una respuesta JSON por accidente. |
| `util/JsonUtil.java` | Leer el cuerpo de una petición como JSON y escribir una respuesta JSON, en un solo lugar. | Evita repetir el mismo código de Gson en cada servlet. |

### Configuración y datos

| Archivo | Qué hace |
|---|---|
| `backend-java/database/pizzeria.sql` | Crea la base `pizzeria`, las tablas `Pizza`/`Usuario`, y carga 6 pizzas + 1 administrador. |
| `backend-java/src/main/resources/db.properties(.example)` | Usuario/clave/URL de MySQL que usa `Conexion.java`. |
| `backend-java/src/main/webapp/WEB-INF/web.xml` | Configuración mínima del tiempo de sesión (30 minutos). |
| `backend-java/src/main/webapp/META-INF/context.xml` | Fija la ruta `/pizzeria` de la aplicación dentro de Tomcat. |

## 2. El viaje completo de una petición

Ejemplo: alguien abre `http://localhost:5173/` y el navegador pide el menú.

1. **Vue** (`InicioView.vue`, dentro de `onMounted`) ejecuta `fetch('/api/pizzas')`.
2. El navegador le manda esa petición al servidor que sirve la página: **Vite**, en el
   puerto 5173 (no a Tomcat directamente; Vue no sabe nada de puertos ni de Tomcat).
3. **Vite** ve que la ruta empieza por `/api` y, según el `proxy` de `vite.config.js`,
   la reenvía a `http://localhost:8080`, cambiando la ruta a `/pizzeria/api/pizzas`
   (por el `rewrite`).
4. **Tomcat**, en el puerto 8080, recibe `GET /pizzeria/api/pizzas` y, por el
   `@WebServlet(urlPatterns = {"/api/pizzas", ...})` de **`PizzaServlet`**, sabe que esa
   petición le toca a esa clase. Llama a su método `doGet`.
5. `doGet` llama a **`PizzaDAO.listar()`**.
6. `PizzaDAO` le pide a **`Conexion.obtener()`** una conexión a MySQL y ejecuta
   `SELECT ... FROM Pizza ORDER BY Id` con un `PreparedStatement`.
7. **MySQL** devuelve las filas; `PizzaDAO` las convierte en una lista de objetos
   **`Pizza`** (el método `mapear`).
8. `PizzaServlet.doGet` recibe esa lista y llama a **`JsonUtil.escribir`**, que usa Gson
   para convertirla a texto JSON y la escribe en la respuesta HTTP, con status 200.
9. La respuesta recorre el camino de vuelta: Tomcat → Vite (que la reenvía tal cual,
   gracias a `changeOrigin`) → el navegador.
10. De vuelta en **Vue**, `await fetch(...)` se resuelve; `respuesta.json()` convierte el
    texto en un arreglo de JavaScript; ese arreglo se guarda en `pizzas` (un `ref`); Vue
    detecta el cambio y vuelve a dibujar `MenuPizzas`, que crea una `PizzaCard` por cada
    pizza.

Para una petición que **necesita sesión** (por ejemplo `POST /api/pizzas` desde
`AdminView`), el camino es el mismo hasta el paso 4, pero antes de llamar al DAO,
`PizzaServlet.estaAutenticado(request)` revisa si existe la cookie `JSESSIONID` y si esa
sesión tiene guardado un `usuarioId` (eso lo dejó `AuthServlet.login` cuando la persona
inició sesión). Si no hay sesión, responde 401 de una vez, sin tocar la base de datos.

## 3. El patrón "las props bajan, los eventos suben"

Es la regla más importante de Vue (y de casi cualquier framework de componentes): **un
componente hijo nunca modifica directamente lo que le pasó su padre**. Si necesita que
algo cambie "arriba", avisa con un evento; quien decide qué hacer con ese aviso es
siempre el padre.

Ejemplo completo con `PizzaForm` (hijo) y `AdminView` (padre):

```
AdminView                                    PizzaForm
   |                                              |
   |-- :pizza="formularioAbierto" ---------------->|   (prop: baja el dato)
   |                                              |
   |                                              |  copia = { ...props.pizza }
   |                                              |  (copia local, nunca se toca
   |                                              |   la prop directamente)
   |                                              |
   |                                              |  el usuario escribe, hace clic
   |                                              |  en "Guardar"; PizzaForm valida
   |                                              |
   |<----------- emit('guardar', copia) ----------|   (evento: sube el aviso + los datos)
   |
   |  guardarPizza(datos) decide:
   |  ¿datos.id existe? -> PUT /api/pizzas/{id}
   |  si no -> POST /api/pizzas
   |
   |  actualiza "pizzas" (la lista que ve toda la pantalla)
```

`PizzaForm` **no sabe** si lo que escribió el usuario termina en un POST o un PUT, ni
siquiera sabe que existe una API. Solo sabe mostrar datos y avisar "el usuario quiere
guardar esto". `AdminView` tampoco sabe nada de `<input>`, `maxlength` ni validaciones de
formulario: solo reacciona al evento. Cada uno tiene una sola responsabilidad, y por eso
es fácil de explicar por separado en la exposición.

Lo mismo pasa, más simple, entre `PizzaTabla` (hijo) y `AdminView` (padre): la tabla
recibe `pizzas` por prop y, cuando hacen clic en "Eliminar", no borra nada ella misma
(ni siquiera sabe que existe un DELETE): emite `eliminar` con la pizza, y es `AdminView`
quien decide abrir el modal de confirmación y, si se confirma, llamar al backend.

## 4. Preguntas de examen (sin respuestas)

1. ¿Por qué `fetch('/api/pizzas')` en el código de Vue nunca escribe `localhost:8080`? ¿Quién se encarga de llegar hasta Tomcat?
2. ¿Qué pasaría si borraras el `rewrite` del proxy en `vite.config.js`? ¿A qué URL llegaría la petición dentro de Tomcat?
3. ¿Por qué `PizzaServlet.doGet` no revisa si hay sesión, pero `doPost`, `doPut` y `doDelete` sí?
4. ¿Dónde se guarda exactamente el hecho de que un usuario inició sesión? ¿Qué pasaría si se reiniciara Tomcat con una sesión activa?
5. ¿Por qué la clase `Usuario.java` del backend no tiene un campo `clave`? ¿Qué problema evita eso?
6. En `sesion.js`, ¿por qué dos componentes distintos que importan `usuario` ven siempre el mismo valor, sin usar Pinia?
7. ¿Qué hace exactamente `router.beforeEach` cuando alguien entra a `/admin` directamente (sin pasar por login)? ¿Por qué llama a `verificarSesion()` en vez de solo revisar si `usuario.value` ya tiene algo?
8. Si alguien edita el JavaScript del navegador para saltarse el guard del router y entra a `/admin` sin sesión, ¿qué pasaría al intentar borrar una pizza? ¿Por qué sigue estando protegido el sistema?
9. En `PizzaForm.vue`, ¿qué pasaría si, en vez de `copia = { ...props.pizza }`, se hubiera escrito `copia = props.pizza`?
10. ¿Por qué `PizzaForm` no hace ningún `fetch`? ¿Quién decide si lo que emite termina en un POST o en un PUT?
11. En `AdminView.vue`, ¿cómo se decide si al guardar se debe mandar un POST o un PUT?
12. ¿Por qué `PizzaCard.vue` no usa `computed` para formatear el precio?
13. Explica, en tus palabras, qué hace `v-model.number` en el campo de precio del formulario y qué pasaría sin el `.number`.
14. En `HeroScroll.vue`, ¿por qué la sección mide `300vh` en vez de `100vh`? ¿Qué pasaría con la animación si midiera solo una pantalla?
15. ¿Por qué es necesario quitar el listener de scroll en `onUnmounted`? ¿Qué problema concreto pasaría si no se quitara?
