package co.edu.sena.cimm.backend.java.datos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lee db.properties una sola vez (al cargar la clase) y a partir de ahí
 * entrega una Connection nueva cada vez que alguien la pide.
 * No se usa un pool de conexiones (como HikariCP) porque un proyecto
 * académico con pocos usuarios a la vez no lo necesita; así el código
 * se mantiene simple y fácil de explicar.
 */
public class Conexion {

    private static final Properties PROPIEDADES = new Properties();

    static {
        // getResourceAsStream busca el archivo dentro del classpath
        // (en target/classes, donde Maven copia todo lo de src/main/resources).
        try (InputStream entrada = Conexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró db.properties. Copia db.properties.example y complétalo.");
            }
            PROPIEDADES.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo db.properties", e);
        }
    }

    private Conexion() {
        // Clase de solo métodos estáticos: no tiene sentido crear instancias.
    }

    public static Connection obtener() throws SQLException {
        String url = PROPIEDADES.getProperty("db.url");
        String usuario = PROPIEDADES.getProperty("db.usuario");
        String clave = PROPIEDADES.getProperty("db.clave");
        return DriverManager.getConnection(url, usuario, clave);
    }
}
