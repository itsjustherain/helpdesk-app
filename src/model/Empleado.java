package model;

public class Empleado extends Usuario {
    private String departamento, telefono;

    public Empleado() {
    }

    public Empleado(String departamento, String telefono) {
        this.departamento = departamento;
        this.telefono = telefono;
    }

    /** Constructor with id — used when reading from the DB or inserting the child row after register() */
    public Empleado(int id, String username, String password, String email, String nombre, String apellidos, String dni,
            Rol rol, String departamento, String telefono) {
        super(id, username, password, email, nombre, apellidos, dni, rol);
        this.departamento = departamento;
        this.telefono = telefono;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
}
