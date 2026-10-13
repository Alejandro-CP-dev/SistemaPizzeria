package co.edu.sena.cimm.backend.java.servlets;

import co.edu.sena.cimm.backend.java.datos.Conexion;
import co.edu.sena.cimm.backend.java.util.JsonUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * Recibe la foto que el administrador elige en PizzaForm.vue (campo "Examinar") y la
 * guarda en disco, en la misma carpeta que Vite ya sirve en /images/pizzas/... (ver
 * "imagenes.directorio" en db.properties). Responde con el nombre final del archivo,
 * que es justo lo que PizzaServlet espera guardar en la columna "imagen" de Pizza.
 *
 * Se separó de PizzaServlet a propósito: PizzaServlet solo sabe leer/escribir JSON
 * (ver JsonUtil), y una petición con una foto adentro no es JSON, es
 * "multipart/form-data". Mezclar los dos formatos en un mismo método habría obligado a
 * revisar el Content-Type a mano en cada ruta; con un Servlet aparte, cada uno asume un
 * solo tipo de petición y se mantiene simple.
 *
 * POST /api/imagenes -> sube una foto (solo con sesión iniciada, igual que crear/editar
 * una pizza)
 */
@WebServlet("/api/imagenes")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5 MB: ver el mismo límite en PizzaForm.vue
public class ImagenServlet extends HttpServlet {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (!estaAutenticado(request)) {
            JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión");
            return;
        }

        Part archivo;
        try {
            // request.getPart() es lo que @MultipartConfig habilita: sin esa anotación,
            // esta línea lanza una excepción porque el Servlet no sabría cómo separar
            // los "part" de un cuerpo multipart/form-data. "archivo" es el mismo nombre
            // de campo que usa el FormData.append('archivo', ...) en AdminView.vue.
            archivo = request.getPart("archivo");
        } catch (Exception e) {
            // Mismo límite que maxFileSize de la anotación: si el archivo pesa más de
            // 5 MB, el servlet lanza esto en vez de dejar seguir la petición. Se revisa
            // también en el navegador (PizzaForm.vue) para avisar más rápido, pero esta
            // validación de acá es la que de verdad importa: el navegador se puede
            // saltar con herramientas como curl o Postman.
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "La imagen no puede pesar más de 5 MB");
            return;
        }

        if (archivo == null || archivo.getSize() == 0) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "No se envió ninguna imagen");
            return;
        }

        String extension = extensionDe(archivo.getSubmittedFileName());
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension)) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "Solo se permiten imágenes JPG, PNG o WEBP");
            return;
        }

        String directorio = Conexion.propiedad("imagenes.directorio");
        Path carpeta = Paths.get(directorio);
        if (!Files.isDirectory(carpeta)) {
            getServletContext().log("La carpeta de imagenes.directorio no existe: " + directorio);
            JsonUtil.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno");
            return;
        }

        // El nombre final NUNCA usa el nombre original del archivo que subió la persona:
        // ese nombre viene del navegador de quien sube el archivo, así que no es de
        // fiar (podría traer "../../otra-carpeta/algo.jpg" o caracteres raros). Usar la
        // hora exacta en milisegundos, en cambio, siempre da un nombre válido y además
        // evita que dos fotos subidas con el mismo nombre original se pisen entre sí.
        String nombreArchivo = System.currentTimeMillis() + "." + extension;
        Path destino = carpeta.resolve(nombreArchivo);

        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            getServletContext().log("Error guardando la imagen subida", e);
            JsonUtil.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno");
            return;
        }

        JsonUtil.escribir(response, HttpServletResponse.SC_OK, new ImagenGuardadaJson(nombreArchivo));
    }

    /** Mismo criterio que PizzaServlet.estaAutenticado: solo se duplica porque son 2 líneas. */
    private boolean estaAutenticado(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null && sesion.getAttribute("usuarioId") != null;
    }

    /** "Mi Foto.JPG" -> "jpg". Devuelve null si el nombre no trae extensión. */
    private String extensionDe(String nombreOriginal) {
        if (nombreOriginal == null) {
            return null;
        }
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto < 0 || punto == nombreOriginal.length() - 1) {
            return null;
        }
        return nombreOriginal.substring(punto + 1).toLowerCase();
    }

    /** Clase mínima solo para que Gson genere {"imagen": "169..."}. */
    private static class ImagenGuardadaJson {
        final String imagen;

        ImagenGuardadaJson(String imagen) {
            this.imagen = imagen;
        }
    }
}
