package dao;

import model.Usuario;
import java.util.List;

public interface UsuarioDAO {
    void registrar(Usuario usuario);
    Usuario validar(String username, String password);
    List<Usuario> listarTodos();
    void actualizar(Usuario usuario);
    void eliminar(int id);
    Usuario obtenerPorId(int id);
}
