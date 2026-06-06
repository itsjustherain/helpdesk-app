package dao;

import model.Asignacion;
import java.util.List;

public interface AsignacionDAO {
    void insertar(Asignacion asignacion);
    List<Asignacion> listarTodos();
    void eliminar(int id);
}
