package dao;

import model.Tecnico;

import java.sql.SQLException;
import java.util.List;

public interface TecnicoDAO {
    void insert(Tecnico tecnico) throws SQLException;
    List<Tecnico> findAll() throws SQLException;
    void update(Tecnico tecnico) throws SQLException;
    void delete(int id) throws SQLException;
    Tecnico findById(int id) throws SQLException;
}
