package dao;

import model.Empleado;

import java.sql.SQLException;
import java.util.List;

public interface EmpleadoDAO {
    void insertar(Empleado empleado) throws SQLException;
    List<Empleado> listarTodos() throws SQLException;
    void actualizar(Empleado empleado) throws SQLException;
    void eliminar(int id) throws SQLException;  
    Empleado obtenerPorId(int id) throws SQLException;  
}
