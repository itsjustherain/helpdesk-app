package dao;

import model.Tecnico;

import java.sql.SQLException;
import java.util.List;

public interface TecnicoDAO {
    void insertar(Tecnico tecnico) throws SQLException;
    List<Tecnico> listarTodos() throws SQLException;
    void actualizar(Tecnico tecnico) throws SQLException;
    void eliminar(int id) throws SQLException;
    Tecnico obtenerPorId(int id) throws SQLException;
}

