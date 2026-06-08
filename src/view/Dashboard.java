package view;

import model.Empleado;
import model.Tecnico;
import model.Usuario;
import javax.swing.*;

import dao.AsignacionDAO;
import dao.AsignacionDAOImpl;
import dao.EmpleadoDAO;
import dao.EmpleadoDAOImpl;
import dao.IncidenciaDAO;
import dao.IncidenciaDAOImpl;
import dao.TecnicoDAO;
import dao.TecnicoDAOImpl;
import dto.AsignacionDTO;
import dto.IncidenciaDTO;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.List;



public class Dashboard extends JFrame {

    public Dashboard(Usuario usuario) {
        setTitle("Helpdesk - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel panelTop = new JPanel(new BorderLayout());
        JLabel lblUsuario = new JLabel("Logged in: " + usuario.getNombre() + " " + usuario.getApellidos());
        JButton btnLogout = new JButton("Logout");
        panelTop.add(lblUsuario,BorderLayout.WEST);
        panelTop.add(btnLogout, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Incidencias", crearTablaIncidencias());
        tabs.addTab("Asignaciones", crearTablaAsignaciones());
        tabs.addTab("Empleados", crearTablaEmpleados());
        tabs.addTab("Tecnicos", crearTablaTecnicos());
        
        add(tabs, BorderLayout.CENTER);
        
        btnLogout.addActionListener(e -> {
            dispose();
            new Login();
        });


        setVisible(true);
    }

    private JScrollPane crearTablaIncidencias() {
        String[] columnas = {"ID", "Empleado", "Titulo", "Prioridad", "Estado", "Fecha"};
        Object[][] datos = {};

        try {
            IncidenciaDAO dao = new IncidenciaDAOImpl();
            List<IncidenciaDTO> lista = dao.findAllWithDetails();

            datos = new Object[lista.size()][columnas.length];
            int i = 0;
            for (IncidenciaDTO inc : lista) {
                datos[i++] = new Object[] {
                    inc.getId(),
                    inc.getNombreEmpleado(),
                    inc.getTitulo(),
                    inc.getPrioridad(),
                    inc.getEstado(),
                    inc.getFechaCreacion()
                };
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading incidencias: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTable tabla = new JTable(datos, columnas);
        return new JScrollPane(tabla);
    }

    private JScrollPane crearTablaEmpleados() {
        String[] columnas = {"ID", "Username", "Nombre", "Apellidos", "Departamento", "Telefono"};
        Object[][] datos = {};

        try {
            EmpleadoDAO dao = new EmpleadoDAOImpl();
            List<Empleado> lista = dao.findAll();

            datos = new Object[lista.size()][columnas.length];
            int i = 0;
            for (Empleado emp : lista) {
                datos[i++] = new Object[]{
                    emp.getId(),
                    emp.getUsername(),
                    emp.getNombre(),
                    emp.getApellidos(),
                    emp.getDepartamento(),
                    emp.getTelefono()
                };
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading empleados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        JTable tabla = new JTable(datos, columnas);
        return new JScrollPane(tabla);
    }

    private JScrollPane crearTablaTecnicos() {
        String[] columnas = {"ID", "Username", "Nombre", "Apellidos", "Especialidad"};
        Object[][] datos = {};

        try {
            TecnicoDAO dao = new TecnicoDAOImpl();
            List<Tecnico> lista = dao.findAll();

            datos = new Object[lista.size()][columnas.length];
            int i = 0;
            for (Tecnico tec : lista) {
                datos[i++] = new Object[]{
                    tec.getId(),
                    tec.getUsername(),
                    tec.getNombre(),
                    tec.getApellidos(),
                    tec.getEspecialidad()
                };
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading tecnicos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTable tabla = new JTable(datos, columnas);
        return new JScrollPane(tabla);
    }

    private JScrollPane crearTablaAsignaciones() {
        String[] columnas = {"ID", "Incidencia", "Tecnico", "Fecha", "Comentario"};
        Object[][] datos = {};

        try {
            AsignacionDAO dao = new AsignacionDAOImpl();
            List<AsignacionDTO> lista = dao.findAllWithDetails();

            datos = new Object[lista.size()][columnas.length];
            int i = 0;
            for (AsignacionDTO as : lista) {
                datos[i++] = new Object[] {
                    as.getId(),
                    as.getIncidenciaTitulo(),
                    as.getTecnicoNombre(),
                    as.getFechaAsignacion(),
                    as.getComentario()
                };
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading asignaciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        JTable tabla = new JTable(datos, columnas);
        return new JScrollPane(tabla);
    }
}