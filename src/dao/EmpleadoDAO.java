package dao;

import model.Empleado;

import java.sql.SQLException;
import java.util.List;

public interface EmpleadoDAO {
    void insert(Empleado empleado) throws SQLException;
    List<Empleado> findAll() throws SQLException;
    void update(Empleado empleado) throws SQLException;
    void delete(int id) throws SQLException;
    Empleado findById(int id) throws SQLException;
}
