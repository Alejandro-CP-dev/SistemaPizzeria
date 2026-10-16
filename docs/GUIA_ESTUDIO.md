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
| `App.vue` | Pone `<NavBar />` (oculto solo en `/admin`, que trae su propio `AdminHeader`) y `<RouterView />` (cambia según la URL). | Es el "marco" que envuelve cualquier página. |
| `assets/tokens.css` | Variables CSS (colores, espaciados, radios, tipografía) del panel de administración. | Ver la sección "Panel de administración" más abajo: evita repetir los mismos valores en cada componente del panel. |
| `router/index.js` | Define las 3 rutas (`/`, `/login`, `/admin`), el `scrollBehavior` (bajar a un `#ancla`) y el guard que protege `/admin`. | Sin él, cambiar de URL no cambiaría qué se ve en pantalla: Vue por sí solo no sabe de rutas. |
| `sesion.js` | Expone `usuario` (un `ref` compartido) y las funciones `verificarSesion`, `iniciarSesion`, `cerrarSesion`. | Es el "estado global" del login, sin necesitar Pinia: todo componente que lo importa ve la misma variable. |
| `views/InicioView.vue` | Carga las pizzas (`GET /api/pizzas`) y arma la página pública con `HeroScroll`, `SobreNosotros` y `MenuPizzas`. | Es la pantalla de la ruta `/`. |
| `views/LoginView.vue` | Formulario de correo/clave; llama a `iniciarSesion` y redirige a `/admin` si funciona. | Pantalla de la ruta `/login`. |
| `views/AdminView.vue` | Carga las pizzas, decide POST/PUT/DELETE según la acción, decide qué pizza está seleccionada (`pizzaSeleccionada`) y arma la cuadrícula tabla + formulario. | Pantalla de la ruta `/admin`; es el "cerebro" del CRUD. |
| `components/NavBar.vue` | Logo, enlaces a las secciones de `/` y botones de sesión. | Se repite en las páginas públicas, así que vive en `App.vue`, no en cada vista (se oculta en `/admin`). |
| `components/HeroScroll.vue` | Dibuja en un `<canvas>` el fotograma de `public/frames/` que corresponde al scroll. | Es el efecto visual de bienvenida de la página de inicio. |
| `components/SobreNosotros.vue` | Texto fijo sobre la pizzería. | Sección estática, sin props ni fetch: lo más simple que hay en el proyecto. |
| `components/MenuInteractivo.vue` | Carrusel arrastrable de pizzas (con inercia) que, al hacer clic en la pizza del centro, la "abre" en 8 porciones con un ticket y un sello animados. | Ver la sección "Menú interactivo" más abajo: reemplaza a los antiguos `CarruselPizzas.vue`/`CircularCarousel.vue`. |
| `components/MenuPizzas.vue` | Recibe `pizzas` (prop) y dibuja una `PizzaCard` por cada una. | Separa "mostrar la cuadrícula" de "conseguir los datos" (eso lo hace `InicioView`). |
| `components/PizzaCard.vue` | Una tarjeta: foto (o "Sin foto"), nombre, precio (o "Sin precio") y descripción. | Se reutiliza en 2 lugares: el menú público y la vista previa en vivo de `PizzaForm`. |
| `components/PizzaForm.vue` | Formulario de crear/editar (panel fijo, no modal) con vista previa en vivo y subida de foto real (`<input type="file">`); valida y emite, nunca hace fetch. | Separa "la forma de los datos" de "qué hacer con ellos" (eso lo decide `AdminView`). |
| `components/PizzaTabla.vue` | Tabla del panel con buscador, foto, nombre, descripción, precio y botones. | Es la vista de "lista" del admin, paralela a `MenuPizzas` en la vista pública. |
| `components/ModalConfirmar.vue` | Pregunta "¿Eliminar X?" antes de borrar (se cierra con Cancelar, Escape o clic afuera). | Evita que un clic accidental en "Eliminar" borre algo sin avisar. |
| `components/AdminHeader.vue` | Encabezado fijo de `/admin`: logo, enlace a la carta pública, nombre real del usuario y botón de cerrar sesión. | Reemplaza a `NavBar` dentro del panel: la navegación pública no tiene sentido ahí. |
| `components/AdminFooter.vue` | Pie de página fijo del panel. | Mismo rol que `SobreNosotros`: texto estático, sin lógica. |
| `components/NotificacionToast.vue` | Aviso flotante verde ("Pizza guardada correctamente"...), se cierra solo a los 4 segundos. | Antes era un párrafo con `Transition` dentro de `AdminView`; se independizó para que maneje su propio temporizador. |
| `vite.config.js` | Configura el proxy de `/api` hacia Tomcat (puerto 8080, ruta `/pizzeria`) y el `cookiePathRewrite`. | Sin esto, el navegador vería dos orígenes distintos (5173 y 8080) y la sesión (cookie) no sobreviviría entre peticiones. |

### Backend (`backend-java/src/main/java/.../`)

| Archivo | Qué hace | Por qué existe |
|---|---|---|
| `servlets/PizzaServlet.java` | Atiende `/api/pizzas` y `/api/pizzas/{id}`: GET público, POST/PUT/DELETE con sesión. | Es la puerta de entrada HTTP para todo lo de pizzas. |
| `servlets/AuthServlet.java` | Atiende `/api/login`, `/api/logout`, `/api/sesion`. | Es la puerta de entrada HTTP para todo lo de sesión. |
| `servlets/ImagenServlet.java` | Atiende `POST /api/imagenes`: recibe la foto que se sube desde PizzaForm.vue (`multipart/form-data`, campo `archivo`) y la guarda en `imagenes.directorio`. | Separado de PizzaServlet porque atiende un formato de petición distinto (no JSON): mezclar los dos habría obligado a revisar el Content-Type a mano dentro del mismo método. |
| `datos/PizzaDAO.java` | Las 5 consultas SQL de la tabla `Pizza` (listar, buscar, insertar, actualizar, eliminar). | Separa "hablar HTTP" (el Servlet) de "hablar SQL" (el DAO): si un día cambiaras de MySQL a otra base, solo tocarías esta clase. |
| `datos/UsuarioDAO.java` | Valida login y busca un usuario por id (sin exponer nunca la clave). | Mismo motivo: aísla el SQL de la tabla `Usuario`. |
| `datos/Conexion.java` | Lee `db.properties` y entrega una `Connection` nueva cada vez que se le pide; también expone `propiedad(nombre)` para leer cualquier otra propiedad del mismo archivo (como `imagenes.directorio`). | Un solo lugar donde cambiar la URL/usuario/clave de MySQL (o cualquier otra configuración de esta máquina) si algo cambia. |
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

## 4. Panel de administración

El panel (`/admin`) se rediseñó para verse igual a un mockup hecho con Google Stitch
(`frontend-vue/docs/diseno-admin-stitch.html`, HTML + Tailwind por CDN). Como este
proyecto no usa Tailwind, ese diseño se "tradujo" a CSS normal: cada medida, color y
tamaño de letra del mockup pasó a ser una variable en `tokens.css`, y cada componente
nuevo o rediseñado la usa con `var(--nombre)`.

### `tokens.css`: qué es y por qué existe

Es un archivo de solo variables (`:root { --primary: #a62518; ... }`), sin ninguna regla
que dibuje nada. Se importa una sola vez en `main.js`, junto con `main.css`, así que
sus variables quedan disponibles para **cualquier** `<style scoped>` de cualquier
componente, sin tener que importarlo de nuevo en cada uno.

La razón de existir es simple: el diseño de Stitch define sus colores, espaciados y
tipografía una sola vez (en un `tailwind.config`) y cada elemento del HTML los usa por
nombre (`bg-primary`, `text-on-surface-variant`...). `tokens.css` hace lo mismo con CSS
puro. La ventaja concreta: si mañana la pizzería cambia su color de marca, se edita una
sola línea en `tokens.css` y los 7 componentes del panel cambian solos, en vez de buscar
"#a62518" a mano en cada archivo (y arriesgarse a dejar alguno olvidado). Es la misma
idea que ya usa el proyecto en otras partes (por ejemplo, `JsonUtil.java` centraliza
cómo se escribe JSON para no repetirlo en cada servlet), aplicada esta vez al diseño
visual en vez de al backend.

Nota: el sitio público (`/`, `/login`) NO usa `tokens.css` ni estas variables; sigue con
sus propios colores sueltos en cada componente (Marcellus, Figtree, el rojo `#a62518`
escrito directamente). Son dos sistemas de diseño separados a propósito, cada uno con
su propia tipografía (Merriweather/Plus Jakarta Sans en el panel; Marcellus/Figtree en
el sitio público) conviviendo en el mismo `index.html`.

### Componentes nuevos del panel

| Componente | Props | Emits | Qué hace |
|---|---|---|---|
| `AdminHeader.vue` | — | — | Encabezado fijo de `/admin`. Lee `usuario` directamente de `sesion.js` (no por prop) para mostrar el nombre real de quien inició sesión, y llama a `cerrarSesion()` + redirige a `/login` al hacer clic en "Cerrar sesión". |
| `AdminFooter.vue` | — | — | Pie de página fijo, texto estático. |
| `NotificacionToast.vue` | `mensaje` (String, obligatorio) | `cerrar` | El texto a mostrar (ej. "Pizza eliminada"). Arranca un `setTimeout` de 4 segundos en `onMounted` que emite `cerrar`; el botón de cerrar hace lo mismo antes de tiempo. Quien la usa (`AdminView`) decide qué hacer con `cerrar`: en este caso, borrar el mensaje para que `v-if="mensaje"` la oculte. |

### Componentes que cambiaron de forma de trabajar (no solo de estilo)

- **`AdminView.vue`**: antes tenía `formularioAbierto` (`null` / `'nueva'` / una pizza) y
  mostraba `PizzaForm` dentro de un modal que aparecía y desaparecía. El diseño de
  Stitch no tiene modal para el formulario: el panel de la derecha **siempre** está
  visible. Por eso ahora el estado se llama `pizzaSeleccionada` (`null` = modo "nueva
  pizza"), y "Cancelar" ya no oculta nada: solo regresa el panel a modo "nueva pizza".
  `PizzaForm` se sigue destruyendo y recreando al cambiar de pizza (con `:key`), para
  que su copia local arranque fresca; antes eso lo lograba un `v-if`, ahora lo logra el
  `:key` en un elemento que nunca deja de estar presente.
- **`PizzaTabla.vue`**: ahora recibe también `idEditando` (la pizza que está abierta en
  el formulario) para resaltar su fila, y trae un buscador real (`busqueda`, con una
  función simple en vez de un `computed`, filtrando por nombre o descripción).
- **`PizzaCard.vue`**: mismo propósito (mostrar una pizza), aspecto nuevo (foto cuadrada
  en vez del círculo rojo) y un `v-else` nuevo para cuando `precio` todavía es `null`
  (pasaba al crear una pizza nueva y antes no estaba cubierto).

### Subir una foto real (PizzaForm.vue + ImagenServlet.java)

Al principio, elegir la foto de una pizza era solo escoger un nombre de archivo de una
lista (`<select>`) con las fotos que ya venían cargadas de fábrica. Ahora `PizzaForm.vue`
tiene un `<input type="file">` (oculto, disparado por el botón "Examinar") que deja subir
CUALQUIER imagen JPG/PNG/WEBP desde el computador de quien administra el panel. Guardar
una pizza con foto nueva pasa por dos pasos, uno después del otro:

1. **Elegir el archivo** (`alElegirArchivo` en `PizzaForm.vue`): valida tipo y tamaño
   (máx. 5 MB) y, si está bien, crea una URL temporal con `URL.createObjectURL(archivo)`
   para la vista previa en vivo. En este paso **nada** viaja todavía al servidor: el
   archivo solo vive en la memoria del navegador. Por eso se guarda en un campo nuevo,
   `copia.imagenVistaPrevia`, que **no** es un campo real de la tabla `Pizza` (por eso
   `guardar()` lo borra de `datos` antes de emitir: ver la última línea de esa función).
2. **Guardar** (`guardarPizza` en `AdminView.vue`): si `PizzaForm` emitió un `archivo`
   (no `null`), primero lo sube con un `POST /api/imagenes` (`FormData`, no JSON) a
   `ImagenServlet.java`, que lo guarda en disco con un nombre nuevo (la hora exacta en
   milisegundos, nunca el nombre original: ver el porqué en los comentarios de esa
   clase) y responde `{"imagen": "169....jpg"}`. Solo CON ESE nombre de vuelta se sigue
   con el `POST`/`PUT` de siempre hacia `PizzaServlet`, igual que antes de este cambio.

`ImagenServlet` guarda el archivo directamente en `frontend-vue/public/images/pizzas/`
(la ruta exacta la dice `imagenes.directorio` en `db.properties`, ver `Conexion.java`):
es la MISMA carpeta que Vite ya servía para las 6 fotos originales, así que una foto
subida por el panel se ve en `/images/pizzas/...` exactamente igual que las de fábrica,
sin tocar nada del lado del menú público.

### Preguntas de examen sobre el panel (sin respuestas)

1. ¿Por qué `tokens.css` no tiene ninguna clase ni selector de elemento, solo variables dentro de `:root`?
2. Si la pizzería quisiera cambiar el rojo de marca por otro color, ¿cuántos archivos habría que tocar? ¿Por qué tan pocos?
3. ¿Por qué el buscador de `PizzaTabla.vue` usa una función normal y no un `computed`, mientras que "Promedio menú" sí es un `computed`?
4. En `AdminView.vue`, ¿qué pasaría si `PizzaForm` no tuviera el `:key="pizzaSeleccionada ? pizzaSeleccionada.id : 'nueva'"`? Pista: piensa en qué pasa con su `copia` interna al hacer clic en "Editar" sobre una pizza distinta.
5. ¿Por qué `NotificacionToast.vue` limpia su `setTimeout` en `onUnmounted`?
6. `AdminHeader.vue` no recibe `usuario` por prop: lo importa directo de `sesion.js`. ¿Por qué eso funciona igual de bien que pasarlo por prop, en este caso particular?
7. ¿Por qué `ImagenServlet` nunca usa el nombre original del archivo que subió la persona (ej. "mi foto.jpg") para guardarlo en disco?
8. En `AdminView.vue`, ¿por qué `subirImagen` se llama ANTES de armar el `POST`/`PUT` hacia `/api/pizzas`, y no al revés?
9. `@MultipartConfig(maxFileSize = ...)` en `ImagenServlet.java` y la revisión de `archivo.size` en `PizzaForm.vue` imponen el mismo límite de 5 MB dos veces. ¿Por qué no alcanza con revisarlo solo en el navegador?

## 5. Preguntas de examen (sin respuestas)

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

## 6. Menú interactivo (`MenuInteractivo.vue`)

Esta sección (`id="carta"`, justo después de "Nosotros") reemplazó a los antiguos
`CarruselPizzas.vue` + `CircularCarousel.vue`. Hace tres cosas: un carrusel que se
arrastra con inercia, un título que se "enciende" letra por letra, y un panel donde la
pizza activa se abre en 8 porciones con un ticket y un sello animados. Las pizzas le
llegan por la prop `pizzas` (el mismo arreglo que ya carga `InicioView.vue`): este
componente nunca hace `fetch`.

### El `ref` del índice activo

`activa` es un `ref` con el **índice entero** de la pizza que está centrada y quieta
(la que ven el título, el precio y el botón "abrir detalle"). Es distinto de
`posicion`, que es la posición **continua** de la cinta (puede valer `2.35`, a medio
camino entre la tarjeta 2 y la 3, mientras se arrastra). `posicion` se anima con GSAP;
`activa` solo cambia una vez, al final de esa animación (`onComplete` de `irA`), para
no "parpadear" el título mientras la cinta todavía se está moviendo.

### Cómo funciona la inercia

Al soltar el arrastre (`alSoltar`), el componente no frena en la tarjeta más cercana a
donde está la cinta en ese instante: proyecta a dónde habría llegado si siguiera
deslizando un poco más por el impulso del gesto. Esto pasa en 3 pasos:

1. **Medir la velocidad**: mientras se arrastra, `alMover` va guardando `{tiempo,
   posicion}` en `inicio.muestras`, pero solo se queda con las de los últimos ~100ms
   (si guardara todo el arrastre, un gesto que empezó lento y terminó rápido daría una
   velocidad promedio equivocada). Al soltar, la velocidad es
   `(posicionFinal - posicionInicial) / segundosTranscurridos`, usando solo la primera
   y la última muestra de esa ventana de 100ms.
2. **Proyectar el destino**: `posicion.value + velocidad * 0.18` — a mayor velocidad,
   más lejos "cae" el objetivo proyectado, como si la cinta siguiera deslizando un
   poco sola antes de pararse.
3. **Frenar con una curva, no con un salto**: ese objetivo se redondea a la tarjeta
   entera más cercana y se anima con `gsap.to(posicion, { value: destino, ease:
   'power3.out' })`, que arranca rápido y frena suave. La duración varía según qué tan
   lejos quedó esa tarjeta (`0.25 + distancia * 0.15`, acotada entre 0.25s y 0.9s):
   cerca frena rápido, lejos tarda más, para que nunca se sienta como un salto brusco.

### Por qué `gsap.context()` y por qué hay que limpiarlo

El sitio es una SPA: Vue **no recarga la página** al navegar a `/login` o `/admin`,
solo destruye el componente actual y monta el nuevo. Si en ese momento había una
animación de GSAP corriendo (la cinta todavía frenando por inercia, o el sello del
panel de detalle girando sin fin con `repeat: -1`), esa animación **no se entera** de
que el componente desapareció: sigue corriendo en segundo plano y, en cada fotograma,
sigue escribiendo en un `ref` (`posicion`, `precioMostrado`...) que pertenece a una
instancia de Vue que ya no existe. Eso es, como mínimo, trabajo desperdiciado, y en el
peor caso puede lanzar errores al intentar tocar un elemento del DOM que ya se quitó.

`gsap.context(() => {}, raiz.value)` crea un "cajón" que agrupa animaciones para poder
deshacerlas todas de una vez. Las creadas de forma **síncrona** dentro de esa función
quedarían registradas solas, pero casi todas las de este componente se crean después,
disparadas por un evento (arrastrar, hacer clic, cambiar de pizza) — por eso se
envuelven en `ctx.add(() => { ... })`, que las registra en el mismo cajón aunque se
creen más tarde. `onUnmounted` llama a `ctx.revert()`, que mata de un golpe **todas**
las animaciones registradas (la inercia, el stagger del título, la timeline del panel,
el giro del sello) y además deshace cualquier cambio de estilo inline que GSAP haya
dejado, dejando el DOM limpio justo antes de que Vue lo destruya.

### Cómo se corta la pizza con `clip-path`

`clip-path` no tiene curvas: solo puede recortar con líneas rectas (`polygon(...)`).
Una porción de pizza, en cambio, tiene un borde curvo (el arco exterior del círculo).
La solución (función `clipPorcion` en el `<script>` del componente) es **aproximar esa
curva con varios segmentos rectos cortos**: para cada una de las 8 porciones (45° cada
una), se calculan 6 puntos a lo largo de su arco exterior (con trigonometría:
`anguloAPunto` usa `cos`/`sin` para ubicar un punto a cierto radio y ángulo) y se arma
un `polygon()` que va del centro (50%, 50%) a esos puntos y de vuelta. Con 6 segmentos
por cada 45° el "redondeo" ya se ve liso a simple vista, aunque técnicamente sea una
figura de líneas rectas. Las 8 porciones son 8 copias de la **misma** foto (mismo
`background-image`, mismo tamaño), cada una recortada con su propio `polygon()`;
luego, para que se vean "servidas", cada una se traslada (`x`, `y`) en la dirección de
su propio ángulo medio (`separacionPorcion`), alejándose del centro.

### Preguntas de examen (sin respuestas)

1. ¿Por qué el componente usa dos variables de estado distintas para la posición del
   carrusel (`posicion` y `activa`) en vez de una sola?
2. En `alSoltar`, ¿qué pasaría si se usara la velocidad promedio de **todo** el
   arrastre (desde el primer `pointerdown`) en vez de solo la de los últimos 100ms?
3. ¿Por qué las animaciones creadas dentro de un `setTimeout` o un manejador de
   eventos necesitan `ctx.add(() => {...})`, mientras que las que se crean
   directamente dentro de `gsap.context(() => {...})` no lo necesitan?
4. Si `MenuInteractivo.vue` no llamara a `ctx.revert()` en `onUnmounted`, ¿qué se
   podría observar en la consola del navegador después de entrar a `/login` y volver
   varias veces seguidas a `/`?
5. `clipPorcion` no dibuja un arco real, sino un polígono con varios segmentos rectos.
   ¿Qué pasaría visualmente con las porciones si `SEGMENTOS_ARCO` valiera 1 en vez de
   6? ¿Y si valiera 50?
