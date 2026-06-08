package dao;

import model.Empleado;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO interface for Empleado entity */
public interface EmpleadoDAO {
    /** Inserts a new employee using its own connection */
    void insert(Empleado empleado) throws SQLException;

    /** Inserts a new employee using an external connection (for transactions) */
    void insert(Empleado empleado, Connection conn) throws SQLException;

    /** Returns all employees with user data via JOIN */
    List<Empleado> findAll() throws SQLException;

    /** Updates an existing employee */
    void update(Empleado empleado) throws SQLException;

    /** Deletes an employee by id */
    void delete(int id) throws SQLException;

    /** Returns an employee by id, or null if not found */
    Empleado findById(int id) throws SQLException;
}
