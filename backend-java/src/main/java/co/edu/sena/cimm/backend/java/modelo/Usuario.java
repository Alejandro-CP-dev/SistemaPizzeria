package co.edu.sena.cimm.backend.java.modelo;

/**
 * Representa un administrador (tabla Usuario).
 * A propósito esta clase NO tiene el campo "clave": así es imposible que la
 * contraseña salga en una respuesta JSON, aunque alguien olvide filtrarla
 * en algún servlet. La clave solo se maneja como texto suelto dentro del DAO.
 */
public class Usuario {

    private int id;
    private String nombre;
    private String apellido;
    private String correo;

    public Usuario() {
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
