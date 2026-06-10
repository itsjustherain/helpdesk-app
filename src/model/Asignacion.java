package model;

import java.time.LocalDateTime;

/**
 * Entity that represents an assignment of a Tecnico to an Incidencia.
 * Maps to the asignaciones table (N:M between incidencias and tecnicos).
 */
public class Asignacion {
    private int id, incidenciaId, tecnicoId;
    private LocalDateTime fechaAsignacion; // set automatically by the DB on insert
    private String comentario;

    public Asignacion() {
    }

    /** Constructor with id — used when reading from the DB */
    public Asignacion(int id, int incidenciaId, int tecnicoId, LocalDateTime fechaAsignacion, String comentario) {
        this.id = id;
        this.incidenciaId = incidenciaId;
        this.tecnicoId = tecnicoId;
        this.fechaAsignacion = fechaAsignacion;
        this.comentario = comentario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIncidenciaId() { return incidenciaId; }
    public void setIncidenciaId(int incidenciaId) { this.incidenciaId = incidenciaId; }

    public int getTecnicoId() { return tecnicoId; }
    public void setTecnicoId(int tecnicoId) { this.tecnicoId = tecnicoId; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
