package model;

/**
 * Extends Usuario — maps to the tecnicos table (Joined Table Inheritance).
 * Adds the especialidad field specific to technicians.
 */
public class Tecnico extends Usuario {
    private String especialidad;

    public Tecnico() {
    }

    public Tecnico(String especialidad) {
        this.especialidad = especialidad;
    }

    /** Constructor with id — used when reading from the DB or inserting the child row after register() */
    public Tecnico(int id, String username, String password, String email, String nombre, String apellidos, String dni,
            Rol rol, String especialidad) {
        super(id, username, password, email, nombre, apellidos, dni, rol);
        this.especialidad = especialidad;
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
