package co.edu.sena.cimm.backend.java.datos;

import co.edu.sena.cimm.backend.java.modelo.Pizza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla Pizza.
 *
 * Todas las consultas usan PreparedStatement con "?" en vez de concatenar
 * texto (por ejemplo, nunca se hace "WHERE Id = " + id). Con "?" el valor
 * que llega del usuario SIEMPRE se trata como un dato (nunca como código SQL),
 * así que aunque alguien escriba algo como  1; DROP TABLE Pizza  en un campo,
 * el motor lo guarda o lo busca como texto literal y no como una instrucción
 * nueva. Eso es justamente lo que evita la inyección SQL.
 */
public class PizzaDAO {

    public List<Pizza> listar() throws SQLException {
        String sql = "SELECT Id, Nombre, Precio, Imagen, Descripcion FROM Pizza ORDER BY Id";
        List<Pizza> pizzas = new ArrayList<>();
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                pizzas.add(mapear(resultado));
            }
        }
        return pizzas;
    }

    public Pizza buscarPorId(int id) throws SQLException {
        String sql = "SELECT Id, Nombre, Precio, Imagen, Descripcion FROM Pizza WHERE Id = ?";
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public Pizza insertar(Pizza pizza) throws SQLException {
        String sql = "INSERT INTO Pizza (Nombre, Precio, Imagen, Descripcion) VALUES (?, ?, ?, ?)";
        // Statement.RETURN_GENERATED_KEYS le pide a MySQL que nos devuelva el Id
        // que él mismo generó con AUTO_INCREMENT, para no tener que adivinarlo.
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, pizza.getNombre());
            sentencia.setInt(2, pizza.getPrecio());
            sentencia.setString(3, pizza.getImagen());
            sentencia.setString(4, pizza.getDescripcion());
            sentencia.executeUpdate();

            try (ResultSet generadas = sentencia.getGeneratedKeys()) {
                if (generadas.next()) {
                    pizza.setId(generadas.getInt(1));
                }
            }
        }
        return pizza;
    }

    public boolean actualizar(Pizza pizza) throws SQLException {
        String sql = "UPDATE Pizza SET Nombre = ?, Precio = ?, Imagen = ?, Descripcion = ? WHERE Id = ?";
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, pizza.getNombre());
            sentencia.setInt(2, pizza.getPrecio());
            sentencia.setString(3, pizza.getImagen());
            sentencia.setString(4, pizza.getDescripcion());
            sentencia.setInt(5, pizza.getId());
            // executeUpdate devuelve cuántas filas cambiaron; si es 0, ese Id no existía.
            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Pizza WHERE Id = ?";
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        }
    }

    private Pizza mapear(ResultSet resultado) throws SQLException {
        Pizza pizza = new Pizza();
        pizza.setId(resultado.getInt("Id"));
        pizza.setNombre(resultado.getString("Nombre"));
        pizza.setPrecio(resultado.getInt("Precio"));
        pizza.setImagen(resultado.getString("Imagen"));
        pizza.setDescripcion(resultado.getString("Descripcion"));
        return pizza;
    }
}
