package co.edu.sena.cimm.backend.java.servlets;

import co.edu.sena.cimm.backend.java.datos.PizzaDAO;
import co.edu.sena.cimm.backend.java.modelo.Pizza;
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
import java.util.List;

/**
 * Expone el menú de pizzas como JSON.
 *
 * GET    /api/pizzas       -> lista todas (público, cualquiera la puede ver)
 * POST   /api/pizzas       -> crea una pizza (solo con sesión iniciada)
 * PUT    /api/pizzas/{id}  -> actualiza una pizza (solo con sesión)
 * DELETE /api/pizzas/{id}  -> elimina una pizza (solo con sesión)
 */
@WebServlet(urlPatterns = {"/api/pizzas", "/api/pizzas/*"})
public class PizzaServlet extends HttpServlet {

    private final PizzaDAO pizzaDAO = new PizzaDAO();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se configura UTF-8 y el Content-Type una sola vez aquí, en vez de
        // repetirlo en doGet/doPost/doPut/doDelete.
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Pizza> pizzas = pizzaDAO.listar();
            JsonUtil.escribir(response, HttpServletResponse.SC_OK, pizzas);
        } catch (SQLException e) {
            fallo(e, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!estaAutenticado(request)) {
            JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión");
            return;
        }

        Pizza pizza;
        try {
            pizza = JsonUtil.leer(request, Pizza.class);
        } catch (JsonSyntaxException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El JSON enviado no es válido");
            return;
        }

        String errorValidacion = validar(pizza);
        if (errorValidacion != null) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        try {
            Pizza creada = pizzaDAO.insertar(pizza);
            JsonUtil.escribir(response, HttpServletResponse.SC_CREATED, creada);
        } catch (SQLException e) {
            fallo(e, response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!estaAutenticado(request)) {
            JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión");
            return;
        }

        Integer id = leerIdDeLaRuta(request);
        if (id == null) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "Falta el id de la pizza en la URL");
            return;
        }

        Pizza pizza;
        try {
            pizza = JsonUtil.leer(request, Pizza.class);
        } catch (JsonSyntaxException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El JSON enviado no es válido");
            return;
        }

        String errorValidacion = validar(pizza);
        if (errorValidacion != null) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        // El id siempre se toma de la URL, no del cuerpo: así nadie puede
        // enviar PUT /api/pizzas/3 con {"id": 9, ...} y modificar otra fila.
        pizza.setId(id);

        try {
            if (pizzaDAO.buscarPorId(id) == null) {
                JsonUtil.error(response, HttpServletResponse.SC_NOT_FOUND, "No existe una pizza con ese id");
                return;
            }
            pizzaDAO.actualizar(pizza);
            JsonUtil.escribir(response, HttpServletResponse.SC_OK, pizza);
        } catch (SQLException e) {
            fallo(e, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!estaAutenticado(request)) {
            JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión");
            return;
        }

        Integer id = leerIdDeLaRuta(request);
        if (id == null) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "Falta el id de la pizza en la URL");
            return;
        }

        try {
            if (pizzaDAO.buscarPorId(id) == null) {
                JsonUtil.error(response, HttpServletResponse.SC_NOT_FOUND, "No existe una pizza con ese id");
                return;
            }
            pizzaDAO.eliminar(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            fallo(e, response);
        }
    }

    /**
     * Una sesión está activa si existe (no se crea una nueva al preguntar,
     * por eso getSession(false)) y además tiene el atributo "usuarioId"
     * que AuthServlet guarda justo cuando el login es correcto.
     */
    private boolean estaAutenticado(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null && sesion.getAttribute("usuarioId") != null;
    }

    /** Extrae el {id} de rutas tipo /api/pizzas/5. Devuelve null si no hay un número válido. */
    private Integer leerIdDeLaRuta(HttpServletRequest request) {
        String pathInfo = request.getPathInfo(); // ej. "/5"
        if (pathInfo == null || pathInfo.length() < 2) {
            return null;
        }
        try {
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Reglas de negocio que debe cumplir toda pizza, sin importar si es creación o edición. */
    private String validar(Pizza pizza) {
        if (pizza == null) {
            return "El cuerpo de la petición está vacío";
        }
        if (pizza.getNombre() == null || pizza.getNombre().trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (pizza.getNombre().length() > 50) {
            return "El nombre no puede tener más de 50 caracteres";
        }
        if (pizza.getPrecio() <= 0) {
            return "El precio debe ser un número mayor que 0";
        }
        if (pizza.getDescripcion() != null && pizza.getDescripcion().length() > 200) {
            return "La descripción no puede tener más de 200 caracteres";
        }
        if (pizza.getImagen() != null && pizza.getImagen().length() > 100) {
            return "El nombre de la imagen no puede tener más de 100 caracteres";
        }
        return null;
    }

    /**
     * Ante un error de base de datos, el cliente solo recibe un mensaje genérico;
     * el detalle (SQLException completa) queda en el log de Tomcat para que lo
     * revise quien administra el servidor. Así nunca se expone información
     * interna (nombres de tablas, consultas, etc.) a quien hace la petición.
     */
    private void fallo(SQLException e, HttpServletResponse response) throws IOException {
        getServletContext().log("Error de base de datos en PizzaServlet", e);
        JsonUtil.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno");
    }
}
