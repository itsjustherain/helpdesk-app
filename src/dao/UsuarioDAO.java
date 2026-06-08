package dao;

import model.Usuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO interface for Usuario entity */
public interface UsuarioDAO {
    /** Registers a new user using its own connection */
    void register(Usuario usuario) throws SQLException;

    /** Registers a new user using an external connection (for transactions) */
    void register(Usuario usuario, Connection conn) throws SQLException;

    /** Validates credentials and returns the user, or null if not found */
    Usuario validate(String username, String password) throws SQLException;

    /** Returns all users */
    List<Usuario> findAll() throws SQLException;

    /** Updates an existing user */
    void update(Usuario usuario) throws SQLException;

    /** Deletes a user by id */
    void delete(int id) throws SQLException;

    /** Returns a user by id, or null if not found */
    Usuario findById(int id) throws SQLException;
}
