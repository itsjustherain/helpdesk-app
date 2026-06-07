package dao;

import model.Incidencia;
import dto.IncidenciaDTO;

import java.sql.SQLException;
import java.util.List;

public interface IncidenciaDAO {
    void insert(Incidencia incidencia) throws SQLException;
    List<Incidencia> findAll() throws SQLException;
    List<IncidenciaDTO> findAllWithDetails() throws SQLException;
    void update(Incidencia incidencia) throws SQLException;
    void delete(int id) throws SQLException;
    Incidencia findById(int id) throws SQLException;
}
