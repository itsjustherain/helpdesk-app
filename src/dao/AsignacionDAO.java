package dao;

import model.Asignacion;

import java.sql.SQLException;
import java.util.List;

public interface AsignacionDAO {
    void insertar(Asignacion asignacion) throws SQLException;
    List<Asignacion> listarTodos() throws SQLException;
    void eliminar(int id) throws SQLException;
}
