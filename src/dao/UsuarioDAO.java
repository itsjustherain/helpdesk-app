package dao;

import model.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface UsuarioDAO {
    void register(Usuario usuario) throws SQLException;
    Usuario validate(String username, String password) throws SQLException;
    List<Usuario> findAll() throws SQLException;
    void update(Usuario usuario) throws SQLException;
    void delete(int id) throws SQLException;
    Usuario findById(int id) throws SQLException;
}
