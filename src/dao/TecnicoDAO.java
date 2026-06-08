package dao;

import model.Tecnico;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO interface for Tecnico entity */
public interface TecnicoDAO {
    /** Inserts a new technician using its own connection */
    void insert(Tecnico tecnico) throws SQLException;

    /** Inserts a new technician using an external connection (for transactions) */
    void insert(Tecnico tecnico, Connection conn) throws SQLException;

    /** Returns all technicians with user data via JOIN */
    List<Tecnico> findAll() throws SQLException;

    /** Updates an existing technician */
    void update(Tecnico tecnico) throws SQLException;

    /** Deletes a technician by id */
    void delete(int id) throws SQLException;

    /** Returns a technician by id, or null if not found */
    Tecnico findById(int id) throws SQLException;
}
