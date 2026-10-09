package co.edu.sena.cimm.backend.java.modelo;

/**
 * Representa una fila de la tabla Pizza.
 * Los atributos están en camelCase porque Gson los convierte directo a JSON
 * con el mismo nombre; así el frontend recibe {id, nombre, precio, imagen, descripcion}.
 */
public class Pizza {

    private int id;
    private String nombre;
    private int precio;
    private String imagen;
    private String descripcion;

    public Pizza() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
