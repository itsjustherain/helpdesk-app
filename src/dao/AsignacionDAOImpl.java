package dao;

import db.ConexionDB;
import model.Asignacion;
import dto.AsignacionDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Implementation of AsignacionDAO. Handles the asignaciones table (N:M between incidencias and tecnicos) */
public class AsignacionDAOImpl implements AsignacionDAO {

    /** Inserts a new assignment. fecha_asignacion is set automatically by the DB (DEFAULT NOW()) */
    @Override
    public void insert(Asignacion asignacion) throws SQLException {
        String sql = "INSERT INTO asignaciones (incidencia_id, tecnico_id, comentario) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, asignacion.getIncidenciaId());
            ps.setInt(2, asignacion.getTecnicoId());
            ps.setString(3, asignacion.getComentario());
            ps.executeUpdate();
        }
    }

    /** Returns all assignments. Converts the SQL Timestamp to Java LocalDateTime */
    @Override
    public List<Asignacion> findAll() throws SQLException {
        String sql = "SELECT id, incidencia_id, tecnico_id, fecha_asignacion, comentario FROM asignaciones";
        List<Asignacion> asignaciones = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                asignaciones.add(new Asignacion(
                    rs.getInt("id"),
                    rs.getInt("incidencia_id"),
                    rs.getInt("tecnico_id"),
                    // getTimestamp returns a java.sql.Timestamp; toLocalDateTime converts it to java.time.LocalDateTime
                    rs.getTimestamp("fecha_asignacion").toLocalDateTime(),
                    rs.getString("comentario")
                ));
            }
        }
        return asignaciones;
    }

    /**
     * Returns EVERY incident together with its assignment (if any), via LEFT JOIN.
     * Incidents with no technician assigned appear once with a null assignment id and
     * "Sin asignar" as technician, so the Asignaciones tab shows all incidents.
     * An incident assigned to several technicians appears once per technician (N:M).
     */
    @Override
    public List<AsignacionDTO> findAllWithDetails() throws SQLException {
        String sql = "SELECT i.id AS incidenciaId, a.id AS asignacionId, i.titulo AS incidenciaTitulo, " +
                     "CONCAT(u.nombre, ' ', u.apellidos) AS tecnicoNombre, " +
                     "a.fecha_asignacion, a.comentario " +
                     "FROM incidencias i " +
                     "LEFT JOIN asignaciones a ON a.incidencia_id = i.id " +
                     "LEFT JOIN tecnicos t ON a.tecnico_id = t.usuario_id " +
                     "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                     "ORDER BY i.id, a.id";
        List<AsignacionDTO> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AsignacionDTO dto = new AsignacionDTO();
                dto.setIncidenciaId(rs.getInt("incidenciaId"));
                // getInt devuelve 0 cuando la columna es NULL (incidencia sin asignación)
                dto.setId(rs.getInt("asignacionId"));
                dto.setIncidenciaTitulo(rs.getString("incidenciaTitulo"));

                // Si no hay técnico asignado, mostramos "Sin asignar"
                String tecnico = rs.getString("tecnicoNombre");
                dto.setTecnicoNombre(tecnico != null ? tecnico : "Sin asignar");

                // La fecha y el comentario pueden ser NULL si la incidencia no está asignada
                Timestamp ts = rs.getTimestamp("fecha_asignacion");
                dto.setFechaAsignacion(ts != null ? ts.toLocalDateTime() : null);
                dto.setComentario(rs.getString("comentario"));

                lista.add(dto);
            }
        }
        return lista;
    }

    /** Updates incidencia, tecnico and comment of an existing assignment */
    @Override
    public void update(Asignacion asignacion) throws SQLException {
        String sql = "UPDATE asignaciones SET incidencia_id=?, tecnico_id=?, comentario=? WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, asignacion.getIncidenciaId());
            ps.setInt(2, asignacion.getTecnicoId());
            ps.setString(3, asignacion.getComentario());
            ps.setInt(4, asignacion.getId());
            ps.executeUpdate();
        }
    }

    /** Returns a single assignment by id, or null if not found */
    @Override
    public Asignacion findById(int id) throws SQLException {
        String sql = "SELECT id, incidencia_id, tecnico_id, fecha_asignacion, comentario FROM asignaciones WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Asignacion(
                        rs.getInt("id"),
                        rs.getInt("incidencia_id"),
                        rs.getInt("tecnico_id"),
                        rs.getTimestamp("fecha_asignacion").toLocalDateTime(),
                        rs.getString("comentario")
                    );
                }
            }
        }
        return null;
    }

    /** Deletes an assignment by id */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM asignaciones WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
