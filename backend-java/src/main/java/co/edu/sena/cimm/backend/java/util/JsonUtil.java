package co.edu.sena.cimm.backend.java.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Agrupa las dos cosas que todos los servlets hacen con el cuerpo JSON
 * (leerlo con Gson y escribir la respuesta con Gson), para no repetir el
 * mismo código en PizzaServlet y AuthServlet.
 */
public final class JsonUtil {

    private static final Gson GSON = new Gson();

    private JsonUtil() {
    }

    /** Lee el cuerpo de la petición y lo convierte en un objeto de la clase indicada. */
    public static <T> T leer(HttpServletRequest request, Class<T> clase) throws IOException, JsonSyntaxException {
        return GSON.fromJson(request.getReader(), clase);
    }

    /** Escribe "objeto" como JSON en la respuesta, con el status HTTP indicado. */
    public static void escribir(HttpServletResponse response, int status, Object objeto) throws IOException {
        response.setStatus(status);
        response.getWriter().write(GSON.toJson(objeto));
    }

    /** Atajo para respuestas de error: siempre con la forma {"error": "mensaje"}. */
    public static void error(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.getWriter().write(GSON.toJson(new ErrorJson(mensaje)));
    }

    /** Clase mínima solo para que Gson genere {"error": "..."}. */
    private static class ErrorJson {
        final String error;

        ErrorJson(String error) {
            this.error = error;
        }
    }
}
