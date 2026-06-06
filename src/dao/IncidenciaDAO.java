package dao;

import model.Incidencia;

import java.sql.SQLException;
import java.util.List;

public interface IncidenciaDAO {
    void insertar(Incidencia incidencia) throws SQLException;
    List<Incidencia> listarTodos() throws SQLException;
    void actualizar(Incidencia incidencia) throws SQLException;
    void eliminar(int id) throws SQLException;
    Incidencia obtenerPorId(int id) throws SQLException;
}
