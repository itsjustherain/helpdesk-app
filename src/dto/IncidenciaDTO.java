package dto;

import java.time.LocalDateTime;
import model.Prioridad;
import model.Estado;

/**
 * DTO (Data Transfer Object) for Incidencia.
 * Used when a JOIN is needed to include the employee's full name.
 * Unlike Incidencia (which stores empleadoId as a FK), this carries
 * nombreEmpleado as a readable string — suitable for displaying in tables.
 */
public class IncidenciaDTO {
    private int id;
    private String nombreEmpleado; // full name from the JOIN with usuarios
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private Estado estado;
    private LocalDateTime fechaCreacion;

    /** No-arg constructor required for manual field-by-field population. */
    public IncidenciaDTO() {
    }

    /** Full constructor — used by IncidenciaDAOImpl when mapping a ResultSet row. */
    public IncidenciaDTO(int id, String nombreEmpleado, String titulo, String descripcion, Prioridad prioridad,
            Estado estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombreEmpleado = nombreEmpleado;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}