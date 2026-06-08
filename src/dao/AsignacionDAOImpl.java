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
     * Returns all assignments with the incident title and technician's full name via JOIN.
     * Joins 4 tables: asignaciones → incidencias, asignaciones → tecnicos → usuarios.
     * Returns AsignacionDTO objects to carry the extra joined data.
     */
    @Override
    public List<AsignacionDTO> findAllWithDetails() throws SQLException {
        String sql = "SELECT a.id, i.titulo AS incidenciaTitulo, " +
                     "CONCAT(u.nombre, ' ', u.apellidos) AS tecnicoNombre, " +
                     "a.fecha_asignacion, a.comentario " +
                     "FROM asignaciones a " +
                     "JOIN incidencias i ON a.incidencia_id = i.id " +
                     "JOIN tecnicos t ON a.tecnico_id = t.usuario_id " +
                     "JOIN usuarios u ON t.usuario_id = u.id";
        List<AsignacionDTO> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new AsignacionDTO(
                    rs.getInt("id"),
                    rs.getString("incidenciaTitulo"),
                    rs.getString("tecnicoNombre"),
                    rs.getTimestamp("fecha_asignacion").toLocalDateTime(),
                    rs.getString("comentario")
                ));
            }
        }
        return lista;
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
