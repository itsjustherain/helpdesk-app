package dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) for Asignacion.
 * Used when a JOIN is needed to include the incident title and technician's full name.
 * Unlike Asignacion, this carries readable names instead of raw foreign key ids.
 */
public class AsignacionDTO {
    private int id;             // id de la asignación (0 si la incidencia no tiene ninguna)
    private int incidenciaId;   // id de la incidencia (siempre presente)
    private String incidenciaTitulo;
    private String tecnicoNombre;
    private LocalDateTime fechaAsignacion;
    private String comentario;

    public AsignacionDTO() {
    }

    public AsignacionDTO(int id, String incidenciaTitulo, String tecnicoNombre, LocalDateTime fechaAsignacion, String comentario) {
        this.id = id;
        this.incidenciaTitulo = incidenciaTitulo;
        this.tecnicoNombre = tecnicoNombre;
        this.fechaAsignacion = fechaAsignacion;
        this.comentario = comentario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIncidenciaId() {
        return incidenciaId;
    }

    public void setIncidenciaId(int incidenciaId) {
        this.incidenciaId = incidenciaId;
    }

    public String getIncidenciaTitulo() {
        return incidenciaTitulo;
    }

    public void setIncidenciaTitulo(String incidenciaTitulo) {
        this.incidenciaTitulo = incidenciaTitulo;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public void setTecnicoNombre(String tecnicoNombre) {
        this.tecnicoNombre = tecnicoNombre;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
