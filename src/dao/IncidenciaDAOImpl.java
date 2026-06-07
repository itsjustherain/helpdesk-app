package dao;

import db.ConexionDB;
import model.Incidencia;
import model.Prioridad;
import model.Estado;
import dto.IncidenciaDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidenciaDAOImpl implements IncidenciaDAO {

    @Override
    public void insert(Incidencia incidencia) throws SQLException {
        String sql = "INSERT INTO incidencias (empleado_id, titulo, descripcion, prioridad, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, incidencia.getEmpleadoId());
                ps.setString(2, incidencia.getTitulo());
                ps.setString(3, incidencia.getDescripcion());
                ps.setString(4, incidencia.getPrioridad().name());
                ps.setString(5, incidencia.getEstado().name());

                ps.executeUpdate();
        }
    }

    @Override
    public List<Incidencia> findAll() throws SQLException {
        String sql = "SELECT id, empleado_id, titulo, descripcion, prioridad, estado, fecha_creacion FROM incidencias";
        List<Incidencia> incidencias = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidencias.add(new Incidencia(
                        rs.getInt("id"),
                        rs.getInt("empleado_id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        Prioridad.valueOf(rs.getString("prioridad")),
                        Estado.valueOf(rs.getString("estado")),
                        rs.getTimestamp("fecha_creacion").toLocalDateTime()
                    ));
                }
        }
        return incidencias;
    }

    @Override
    public List<IncidenciaDTO> findAllWithDetails() throws SQLException {
        String sql = "SELECT i.id, CONCAT(u.nombre, ' ', u.apellidos) AS nombreEmpleado, i.titulo, i.descripcion, i.prioridad, i.estado, i.fecha_creacion " +
                     "FROM incidencias i JOIN empleados e ON i.empleado_id = e.usuario_id JOIN usuarios u ON e.usuario_id = u.id";
        List<IncidenciaDTO> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new IncidenciaDTO(
                    rs.getInt("id"),
                    rs.getString("nombreEmpleado"),
                    rs.getString("titulo"),
                    rs.getString("descripcion"),
                    Prioridad.valueOf(rs.getString("prioridad")),
                    Estado.valueOf(rs.getString("estado")),
                    rs.getTimestamp("fecha_creacion").toLocalDateTime()
                ));
            }
        }
        return lista;
    }

    @Override
    public void update(Incidencia incidencia) throws SQLException {
        String sql = "UPDATE incidencias SET titulo=?, descripcion=?, prioridad=?, estado=? WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, incidencia.getTitulo());
                ps.setString(2, incidencia.getDescripcion());
                ps.setString(3, incidencia.getPrioridad().name());
                ps.setString(4, incidencia.getEstado().name());
                ps.setInt(5, incidencia.getId());
                
                ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM incidencias WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
        }
    }

    @Override
    public Incidencia findById(int id) throws SQLException {
        String sql = "SELECT id, empleado_id, titulo, descripcion, prioridad, estado, fecha_creacion FROM incidencias WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Incidencia(
                            rs.getInt("id"),
                            rs.getInt("empleado_id"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            Prioridad.valueOf(rs.getString("prioridad")),
                            Estado.valueOf(rs.getString("estado")),
                            rs.getTimestamp("fecha_creacion").toLocalDateTime()
                        );
                    }
                }
        }
        return null;

    }
}