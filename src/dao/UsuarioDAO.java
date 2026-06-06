package dao;

import model.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface UsuarioDAO {
    void registrar(Usuario usuario) throws SQLException;
    Usuario validar(String username, String password) throws SQLException;
    List<Usuario> listarTodos() throws SQLException;
    void actualizar(Usuario usuario) throws SQLException;
    void eliminar(int id) throws SQLException;
    Usuario obtenerPorId(int id) throws SQLException;
}
