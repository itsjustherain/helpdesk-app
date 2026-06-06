package dao;

import model.Empleado;
import java.util.List;

public interface EmpleadoDAO {
    void insertar(Empleado empleado);
    List<Empleado> listarTodos();
    void actualizar(Empleado empleado);
    void eliminar(int id);  
    Empleado obtenerPorId(int id);  
}
