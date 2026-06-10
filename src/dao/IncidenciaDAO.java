package dao;

import model.Incidencia;
import dto.IncidenciaDTO;

import java.sql.SQLException;
import java.util.List;

/** DAO interface for Incidencia entity */
public interface IncidenciaDAO {
    /** Inserts a new incident */
    void insert(Incidencia incidencia) throws SQLException;

    /** Returns all incidents */
    List<Incidencia> findAll() throws SQLException;

    /** Returns all incidents with employee name via JOIN */
    List<IncidenciaDTO> findAllWithDetails() throws SQLException;

    /** Returns only the incidents of a given employee (with employee name) via JOIN */
    List<IncidenciaDTO> findByEmpleado(int empleadoId) throws SQLException;

    /** Updates an existing incident */
    void update(Incidencia incidencia) throws SQLException;

    /** Deletes an incident by id */
    void delete(int id) throws SQLException;

    /** Returns an incident by id, or null if not found */
    Incidencia findById(int id) throws SQLException;
}
