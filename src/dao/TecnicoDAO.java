package dao;

import model.Tecnico;
import java.util.List;

public interface TecnicoDAO {
    void insertar(Tecnico tecnico);
    List<Tecnico> listarTodos();
    void actualizar(Tecnico tecnico);
    void eliminar(int id);
    Tecnico obtenerPorId(int id);
}

