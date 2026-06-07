package dao;

import model.Asignacion;
import dto.AsignacionDTO;

import java.sql.SQLException;
import java.util.List;

public interface AsignacionDAO {
    void insert(Asignacion asignacion) throws SQLException;
    List<Asignacion> findAll() throws SQLException;
    List<AsignacionDTO> findAllWithDetails() throws SQLException;
    void delete(int id) throws SQLException;
}
