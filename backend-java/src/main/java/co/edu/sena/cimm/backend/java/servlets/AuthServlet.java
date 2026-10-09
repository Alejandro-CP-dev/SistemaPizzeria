package co.edu.sena.cimm.backend.java.servlets;

import co.edu.sena.cimm.backend.java.datos.UsuarioDAO;
import co.edu.sena.cimm.backend.java.modelo.Usuario;
import co.edu.sena.cimm.backend.java.util.JsonUtil;
import com.google.gson.JsonSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Maneja el login/logout y la pregunta "¿sigo con sesión abierta?".
 *
 * POST /api/login  -> valida correo+clave y abre sesión
 * POST /api/logout -> cierra la sesión
 * GET  /api/sesion -> dice si hay sesión activa y quién es el usuario
 */
@WebServlet(urlPatterns = {"/api/login", "/api/logout", "/api/sesion"})
public class AuthServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        super.service(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // login y logout llegan los dos por POST; se distinguen por la ruta exacta.
        String ruta = request.getServletPath();
        if ("/api/login".equals(ruta)) {
            login(request, response);
        } else if ("/api/logout".equals(ruta)) {
            logout(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Solo /api/sesion llega por GET.
        HttpSession sesion = request.getSession(false);
        Object idEnSesion = sesion == null ? null : sesion.getAttribute("usuarioId");
        if (idEnSesion == null) {
            JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "No hay sesión activa");
            return;
        }

        try {
            // Se vuelve a consultar la BD con el id guardado para devolver el
            // usuario completo (nombre, apellido, correo), aunque en la sesión
            // solo se guardó el id.
            Usuario usuario = usuarioDAO.buscarPorId((int) idEnSesion);
            if (usuario == null) {
                // El usuario existía al hacer login pero ya no está en la BD.
                sesion.invalidate();
                JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "No hay sesión activa");
                return;
            }
            JsonUtil.escribir(response, HttpServletResponse.SC_OK, usuario);
        } catch (SQLException e) {
            fallo(e, response);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Credenciales credenciales;
        try {
            credenciales = JsonUtil.leer(request, Credenciales.class);
        } catch (JsonSyntaxException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El JSON enviado no es válido");
            return;
        }

        if (credenciales == null || esVacio(credenciales.correo) || esVacio(credenciales.clave)) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "Correo y clave son obligatorios");
            return;
        }

        Usuario usuario;
        try {
            usuario = usuarioDAO.validarLogin(credenciales.correo, credenciales.clave);
        } catch (SQLException e) {
            fallo(e, response);
            return;
        }

        if (usuario == null) {
            // Mensaje genérico a propósito: si dijéramos por separado "ese correo
            // no existe" o "la clave es incorrecta", alguien podría usar esa
            // diferencia para averiguar, probando correos, cuáles están
            // registrados en el sistema (enumeración de usuarios).
            JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Correo o contraseña incorrectos");
            return;
        }

        HttpSession sesion = request.getSession(true);
        // Solo se guarda el id en la sesión; la clave jamás se guarda en ningún lado.
        sesion.setAttribute("usuarioId", usuario.getId());
        JsonUtil.escribir(response, HttpServletResponse.SC_OK, usuario);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private void fallo(SQLException e, HttpServletResponse response) throws IOException {
        getServletContext().log("Error de base de datos en AuthServlet", e);
        JsonUtil.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno");
    }

    /** Solo para leer el cuerpo de POST /api/login: {"correo": "...", "clave": "..."}. */
    private static class Credenciales {
        String correo;
        String clave;
    }
}
