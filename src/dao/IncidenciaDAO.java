package dao;

import model.Incidencia;
import java.util.List;

public interface IncidenciaDAO {
    void insertar(Incidencia incidencia);
    List<Incidencia> listarTodos();
    void actualizar(Incidencia incidencia);
    void eliminar(int id);
    Incidencia obtenerPorId(int id);
}
