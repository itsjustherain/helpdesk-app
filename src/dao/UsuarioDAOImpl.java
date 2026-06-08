package dao;

import db.ConexionDB;
import model.Usuario;
import model.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Implementation of UsuarioDAO */
public class UsuarioDAOImpl implements UsuarioDAO {

    /** Validates credentials against the database. Returns null if not found */
    @Override
    public Usuario validate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, email, nombre, apellidos, dni, rol FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("dni"),
                        Rol.valueOf(rs.getString("rol"))
                    );
                }
            }
        }
        return null;
    }

    /** Inserts a new user using its own connection */
    @Override
    public void register(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password, email, nombre, apellidos, dni, rol) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPassword());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getNombre());
            ps.setString(5, usuario.getApellidos());
            ps.setString(6, usuario.getDni());
            ps.setString(7, usuario.getRol().name());

            ps.executeUpdate();
        }
    }

    /** Inserts a new user using an external connection. Sets the generated id on the object */
    @Override
    public void register(Usuario usuario, Connection conn) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password, email, nombre, apellidos, dni, rol) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPassword());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getNombre());
            ps.setString(5, usuario.getApellidos());
            ps.setString(6, usuario.getDni());
            ps.setString(7, usuario.getRol().name());
            ps.executeUpdate();

            // Retrieve the auto-generated id and set it on the object
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }
        }
    }

    /** Returns all users */
    @Override
    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT id, username, password, email, nombre, apellidos, dni, rol FROM usuarios";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("dni"),
                    Rol.valueOf(rs.getString("rol"))
                ));
            }
        }
        return usuarios;
    }

    /** Updates an existing user */
    @Override
    public void update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET username=?, password=?, email=?, nombre=?, apellidos=?, dni=?, rol=? WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPassword());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getNombre());
            ps.setString(5, usuario.getApellidos());
            ps.setString(6, usuario.getDni());
            ps.setString(7, usuario.getRol().name());
            ps.setInt(8, usuario.getId());

            ps.executeUpdate();
        }
    }

    /** Deletes a user by id. CASCADE handles child table rows */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Returns a user by id, or null if not found */
    @Override
    public Usuario findById(int id) throws SQLException {
        String sql = "SELECT id, username, password, email, nombre, apellidos, dni, rol FROM usuarios WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("dni"),
                        Rol.valueOf(rs.getString("rol"))
                    );
                }
            }
        }
        return null;
    }
}
