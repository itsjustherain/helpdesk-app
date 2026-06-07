package dao;

import db.ConexionDB;
import model.Tecnico;
import model.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TecnicoDAOImpl implements TecnicoDAO {

    @Override
    public void insert(Tecnico tecnico) throws SQLException {
        String sql = "INSERT INTO tecnicos (usuario_id, especialidad) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, tecnico.getId());
                ps.setString(2, tecnico.getEspecialidad());

                ps.executeUpdate();
        }
    }

    @Override
    public List<Tecnico> findAll() throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.email, u.nombre, u.apellidos, u.dni, u.rol, t.especialidad FROM usuarios u JOIN tecnicos t ON u.id = t.usuario_id";
        List<Tecnico> tecnicos = new ArrayList<>();
        
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tecnicos.add(new Tecnico(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("dni"),
                        Rol.valueOf(rs.getString("rol")),
                        rs.getString("especialidad")
                    ));
                }
        } 
        return tecnicos;
    }

    @Override
    public void update(Tecnico tecnico) throws SQLException {
        String sql = "UPDATE usuarios u JOIN tecnicos t ON u.id = t.usuario_id SET u.username=?, u.password=?, u.email=?, u.nombre=?, u.apellidos=?, u.dni=?, t.especialidad=? WHERE u.id=?";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tecnico.getUsername());
                ps.setString(2, tecnico.getPassword());
                ps.setString(3, tecnico.getEmail());
                ps.setString(4, tecnico.getNombre());
                ps.setString(5, tecnico.getApellidos());
                ps.setString(6, tecnico.getDni());
                ps.setString(7, tecnico.getEspecialidad());
                ps.setInt(8, tecnico.getId());

                ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
        }
    }

    @Override
    public Tecnico findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.email, u.nombre, u.apellidos, u.dni, u.rol, t.especialidad FROM usuarios u JOIN tecnicos t ON u.id = t.usuario_id WHERE u.id=?";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Tecnico(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("dni"),
                            Rol.valueOf(rs.getString("rol")),
                            rs.getString("especialidad")
                        );
                    }
                }
        }
        return null;
    }
}