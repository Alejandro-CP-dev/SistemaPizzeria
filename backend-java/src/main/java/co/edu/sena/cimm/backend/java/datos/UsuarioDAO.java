package co.edu.sena.cimm.backend.java.datos;

import co.edu.sena.cimm.backend.java.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos de la tabla Usuario.
 * Igual que en PizzaDAO, se usa PreparedStatement con "?" para que el correo
 * y la clave que escribe quien inicia sesión nunca se interpreten como SQL.
 */
public class UsuarioDAO {

    /**
     * Si correo y clave coinciden con una fila de la BD, devuelve ese Usuario.
     * Si no coinciden (correo inexistente o clave incorrecta), devuelve null:
     * desde aquí no se distingue cuál de los dos falló, esa decisión de
     * seguridad se toma en el servlet.
     */
    public Usuario validarLogin(String correo, String clave) throws SQLException {
        String sql = "SELECT Id, Nombre, Apellido, Correo FROM Usuario WHERE Correo = ? AND Clave = ?";
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, correo);
            sentencia.setString(2, clave);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    /**
     * Se usa en GET /api/sesion: en la sesión HTTP solo se guarda el id,
     * así que para devolver el usuario completo (nombre, apellido, correo)
     * hay que volver a consultarlo en la BD con ese id.
     */
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT Id, Nombre, Apellido, Correo FROM Usuario WHERE Id = ?";
        try (Connection conexion = Conexion.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        // Nota: no se selecciona ni se lee la columna Clave aquí.
        // Usuario ni siquiera tiene ese atributo (ver modelo.Usuario),
        // así que es imposible devolverla por accidente.
        Usuario usuario = new Usuario();
        usuario.setId(resultado.getInt("Id"));
        usuario.setNombre(resultado.getString("Nombre"));
        usuario.setApellido(resultado.getString("Apellido"));
        usuario.setCorreo(resultado.getString("Correo"));
        return usuario;
    }
}
