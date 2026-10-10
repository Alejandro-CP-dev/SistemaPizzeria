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

        try {
            // En teoría un driver JDBC 4 (como mysql-connector-j) se auto-registra
            // solo, sin este paso. En la práctica, dentro de Tomcat eso depende de
            // qué classloader "tocó" primero la clase DriverManager en todo el
            // servidor, y con varias apps corriendo (o con despliegue "exploded",
            // como el que usa NetBeans) ese auto-registro a veces no llega a pasar
            // para esta app en particular, y DriverManager.getConnection termina
            // lanzando "No suitable driver found" aunque el .jar del driver sí
            // esté en WEB-INF/lib. Cargar la clase a mano con Class.forName fuerza
            // su registro, sin depender de ese mecanismo automático.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el driver de MySQL en el classpath", e);
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
