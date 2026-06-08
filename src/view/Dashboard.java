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
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Dashboard extends JFrame {

    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean darkMode = true;

    public Dashboard(Usuario usuario) {
        setTitle("Helpdesk - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel lblUsuario = new JLabel("Logged in: " + usuario.getNombre() + " " + usuario.getApellidos());
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD, 14f));
        JButton btnLogout = new JButton("Logout");
        panelTop.add(lblUsuario, BorderLayout.WEST);
        panelTop.add(btnLogout, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(13f));
        tabs.addTab("Incidencias", crearTablaIncidencias());
        tabs.addTab("Asignaciones", crearTablaAsignaciones());
        tabs.addTab("Empleados", crearTablaEmpleados());
        tabs.addTab("Tecnicos", crearTablaTecnicos());

        add(tabs, BorderLayout.CENTER);

        btnLogout.addActionListener(e -> {
            dispose();
            new Login();
        });

        // Theme toggle button fixed at bottom-right corner
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnTheme = new JButton("🌙");
        btnTheme.setToolTipText("Toggle light/dark mode");
        btnTheme.setFont(btnTheme.getFont().deriveFont(16f));
        panelBottom.add(btnTheme);
        add(panelBottom, BorderLayout.SOUTH);

        btnTheme.addActionListener(e -> {
            try {
                if (darkMode) {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                    btnTheme.setText("🌙");
                } else {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                    btnTheme.setText("☀️");
                }
                darkMode = !darkMode;
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
                datos[i++] = new Object[]{
                    inc.getId(),
                    inc.getNombreEmpleado(),
                    inc.getTitulo(),
                    inc.getPrioridad(),
                    inc.getEstado(),
                    inc.getFechaCreacion().format(FECHA_FORMAT)
                };
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading incidencias: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return buildTable(datos, columnas);
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

        return buildTable(datos, columnas);
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

        return buildTable(datos, columnas);
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
                datos[i++] = new Object[]{
                    as.getId(),
                    as.getIncidenciaTitulo(),
                    as.getTecnicoNombre(),
                    as.getFechaAsignacion().format(FECHA_FORMAT),
                    as.getComentario()
                };
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading asignaciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return buildTable(datos, columnas);
    }

    /** Builds a styled non-editable JTable wrapped in a JScrollPane */
    private JScrollPane buildTable(Object[][] datos, String[] columnas) {
        JTable tabla = new JTable(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        return new JScrollPane(tabla);
    }
}
