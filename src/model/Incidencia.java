package model;

import java.time.LocalDateTime;

/**
 * Entity that represents an IT incident.
 * Maps to the incidencias table.
 * Linked to an Empleado via empleadoId (FK).
 */
public class Incidencia {
    private int id, empleadoId;
    private String titulo, descripcion;
    private Prioridad prioridad;
    private Estado estado;
    private LocalDateTime fechaCreacion; // set automatically by the DB on insert

    public Incidencia() {
    }

    /** Constructor with id — used when reading from the DB */
    public Incidencia(int id, int empleadoId, String titulo, String descripcion, Prioridad prioridad, Estado estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.empleadoId = empleadoId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(int empleadoId) { this.empleadoId = empleadoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
