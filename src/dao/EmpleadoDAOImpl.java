package dao;

import db.ConexionDB;
import model.Empleado;
import model.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Implementation of EmpleadoDAO. Handles the empleados table (child of usuarios) */
public class EmpleadoDAOImpl implements EmpleadoDAO {

    /** Inserts a new employee row using its own connection */
    @Override
    public void insert(Empleado empleado) throws SQLException {
        String sql = "INSERT INTO empleados (usuario_id, departamento, telefono) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empleado.getId());
            ps.setString(2, empleado.getDepartamento());
            ps.setString(3, empleado.getTelefono());
            ps.executeUpdate();
        }
    }

    /**
     * Inserts a new employee row using an external connection.
     * The connection is NOT closed here — the caller manages the transaction.
     */
    @Override
    public void insert(Empleado empleado, Connection conn) throws SQLException {
        String sql = "INSERT INTO empleados (usuario_id, departamento, telefono) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empleado.getId());
            ps.setString(2, empleado.getDepartamento());
            ps.setString(3, empleado.getTelefono());
            ps.executeUpdate();
        }
    }

    /** Returns all employees with their user data using a JOIN between usuarios and empleados */
    @Override
    public List<Empleado> findAll() throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.email, u.nombre, u.apellidos, u.dni, u.rol, " +
                     "e.departamento, e.telefono FROM usuarios u JOIN empleados e ON u.id = e.usuario_id";
        List<Empleado> empleados = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(new Empleado(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("dni"),
                    Rol.valueOf(rs.getString("rol")),
                    rs.getString("departamento"),
                    rs.getString("telefono")
                ));
            }
        }
        return empleados;
    }

    /** Updates both usuarios and empleados tables in a single JOIN UPDATE */
    @Override
    public void update(Empleado empleado) throws SQLException {
        String sql = "UPDATE usuarios u JOIN empleados e ON u.id = e.usuario_id " +
                     "SET u.username=?, u.password=?, u.email=?, u.nombre=?, u.apellidos=?, u.dni=?, " +
                     "e.departamento=?, e.telefono=? WHERE u.id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empleado.getUsername());
            ps.setString(2, empleado.getPassword());
            ps.setString(3, empleado.getEmail());
            ps.setString(4, empleado.getNombre());
            ps.setString(5, empleado.getApellidos());
            ps.setString(6, empleado.getDni());
            ps.setString(7, empleado.getDepartamento());
            ps.setString(8, empleado.getTelefono());
            ps.setInt(9, empleado.getId());
            ps.executeUpdate();
        }
    }

    /** Deletes the user row. ON DELETE CASCADE removes the empleados row automatically */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Returns a single employee by id with user data via JOIN, or null if not found */
    @Override
    public Empleado findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.email, u.nombre, u.apellidos, u.dni, u.rol, " +
                     "e.departamento, e.telefono FROM usuarios u JOIN empleados e ON u.id = e.usuario_id WHERE u.id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Empleado(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("dni"),
                        Rol.valueOf(rs.getString("rol")),
                        rs.getString("departamento"),
                        rs.getString("telefono")
                    );
                }
            }
        }
        return null;
    }
}
