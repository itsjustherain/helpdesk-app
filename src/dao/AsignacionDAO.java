package dao;

import model.Asignacion;

import java.sql.SQLException;
import java.util.List;

public interface AsignacionDAO {
    void insert(Asignacion asignacion) throws SQLException;
    List<Asignacion> findAll() throws SQLException;
    void delete(int id) throws SQLException;
}
