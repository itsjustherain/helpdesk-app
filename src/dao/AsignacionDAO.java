package dao;

import model.Asignacion;
import dto.AsignacionDTO;

import java.sql.SQLException;
import java.util.List;

/** DAO interface for Asignacion entity (N:M between Incidencia and Tecnico) */
public interface AsignacionDAO {
    /** Inserts a new assignment */
    void insert(Asignacion asignacion) throws SQLException;

    /** Returns all assignments */
    List<Asignacion> findAll() throws SQLException;

    /** Returns all assignments with incident title and technician name via JOIN */
    List<AsignacionDTO> findAllWithDetails() throws SQLException;

    /** Deletes an assignment by id */
    void delete(int id) throws SQLException;
}
