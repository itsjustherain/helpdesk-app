package view;

import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.*;
import dto.*;
import db.ConexionDB;
import util.Validador;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Ventana principal tras un inicio de sesión correcto. Muestra una pestaña por entidad.
 *
 * Acceso según el rol:
 *   - EMPLEADO: solo la pestaña Incidencias, donde ve y abre SUS propios tickets.
 *   - TECNICO:  acceso completo a todas las pestañas y operaciones CRUD.
 *
 * El código repetido (selección de fila, confirmaciones, mensajes, montaje de
 * diálogos y formularios) está centralizado en la sección HELPERS del final.
 */
public class Dashboard extends JFrame {

    /** Interfaz funcional para insertar la fila hija (empleados/tecnicos) dentro de una transacción. */
    @FunctionalInterface
    private interface InsertHijo {
        void insertar(Connection conn, int usuarioId) throws SQLException;
    }

    /** Interfaz funcional para una acción sobre la fila seleccionada que puede lanzar SQLException. */
    @FunctionalInterface
    private interface AccionFila {
        void ejecutar(int fila) throws SQLException;
    }

    // Formato legible para las fechas
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Usuario usuario;
    private final boolean esTecnico;

    // DAOs declarados con el tipo de la INTERFAZ (patrón DAO) y reutilizados en toda la ventana
    private final IncidenciaDAO incidenciaDAO = new IncidenciaDAOImpl();
    private final EmpleadoDAO   empleadoDAO   = new EmpleadoDAOImpl();
    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAOImpl();
    private final AsignacionDAO asignacionDAO = new AsignacionDAOImpl();
    private final UsuarioDAO    usuarioDAO    = new UsuarioDAOImpl();

    private boolean darkMode = true;                                  // tema actual (arranca oscuro)
    private final List<JButton> botonesTema = new ArrayList<>();       // un botón de tema por pestaña, sincronizados
    private List<AsignacionDTO> asignacionesCargadas = new ArrayList<>(); // filas actuales de la tabla de asignaciones

    // Modelos de cada tabla, para poder refrescarlas todas. Los no creados (según rol) quedan null.
    private DefaultTableModel modelIncidencias, modelEmpleados, modelTecnicos, modelAsignaciones;

    public Dashboard(Usuario usuario) {
        this.usuario = usuario;
        this.esTecnico = usuario.getRol() == Rol.TECNICO;

        setTitle("Helpdesk - Dashboard");
        setSize(1050, 560); // tamaño medio (no maximizado) para no dejar hueco vacío
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Barra superior: usuario conectado + cerrar sesión ──────────────────
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> { dispose(); Login login = new Login(); login.setVisible(true); });
        JLabel lblUsuario = new JLabel("Logged in: " + usuario.getNombre() + " " + usuario.getApellidos()
            + "  ·  " + (esTecnico ? "Technician" : "Employee"));
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD, 14f));
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelTop.add(lblUsuario, BorderLayout.WEST);
        panelTop.add(btnLogout, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);

        // ── Pestañas (el empleado solo ve Incidencias) ─────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(13f));
        tabs.addTab("Incidencias", panelIncidencias());
        if (esTecnico) {
            tabs.addTab("Asignaciones", panelAsignaciones());
            tabs.addTab("Empleados",    panelEmpleados());
            tabs.addTab("Tecnicos",     panelTecnicos());
        }
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INCIDENCIAS
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel panelIncidencias() {
        modelIncidencias = modelo("ID", "Empleado", "Titulo", "Prioridad", "Estado", "Fecha");
        cargarIncidencias();
        JTable tabla = buildTable(modelIncidencias);

        JButton add  = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del  = new JButton("Delete");

        JButton details = new JButton("Details");

        add.addActionListener(e -> mostrarDialogIncidencia(null));
        edit.addActionListener(e -> conSeleccion(tabla, f ->
            mostrarDialogIncidencia(incidenciaDAO.findById((int) modelIncidencias.getValueAt(f, 0)))));
        del.addActionListener(e -> conSeleccion(tabla, f -> {
            int id = (int) modelIncidencias.getValueAt(f, 0);
            if (confirmar("¿Eliminar incidencia #" + id + "? También se eliminarán sus asignaciones.")) {
                incidenciaDAO.delete(id); cargarIncidencias(); exito("Incidencia eliminada correctamente.");
            }
        }));
        details.addActionListener(e -> conSeleccion(tabla, f -> {
            Incidencia i = incidenciaDAO.findById((int) modelIncidencias.getValueAt(f, 0));
            mostrarDetalles("Incidencia #" + i.getId(),
                "ID", String.valueOf(i.getId()),
                "Empleado", String.valueOf(modelIncidencias.getValueAt(f, 1)),
                "Título", i.getTitulo(),
                "Descripción", i.getDescripcion(),
                "Prioridad", i.getPrioridad().name(),
                "Estado", i.getEstado().name(),
                "Fecha", i.getFechaCreacion().format(FECHA_FORMAT));
        }));

        // El empleado solo puede Añadir y Ver; el técnico tiene CRUD completo
        return panelTabla(tabla, esTecnico ? barraInferior(add, edit, del, details) : barraInferior(add, details));
    }

    /** Carga todas las incidencias (técnico) o solo las del empleado conectado. */
    private void cargarIncidencias() {
        modelIncidencias.setRowCount(0);
        try {
            List<IncidenciaDTO> lista = esTecnico
                ? incidenciaDAO.findAllWithDetails()
                : incidenciaDAO.findByEmpleado(usuario.getId());
            for (IncidenciaDTO i : lista) {
                modelIncidencias.addRow(new Object[]{
                    i.getId(), i.getNombreEmpleado(), i.getTitulo(),
                    i.getPrioridad(), i.getEstado(), i.getFechaCreacion().format(FECHA_FORMAT)
                });
            }
        } catch (SQLException ex) { mostrarError(ex); }
    }

    /**
     * Diálogo Añadir/Editar incidencia (incidencia == null → añadir).
     * El técnico elige empleado y estado; el empleado crea el ticket en su nombre y en estado ABIERTA.
     */
    private void mostrarDialogIncidencia(Incidencia incidencia) {
        boolean nueva = (incidencia == null);
        JTextField titulo = new JTextField(20), descripcion = new JTextField(20);
        JComboBox<Prioridad> prioridad = new JComboBox<>(Prioridad.values());
        JComboBox<Estado> estado = new JComboBox<>(Estado.values());
        JComboBox<String> empleadoCmb = new JComboBox<>();

        // El selector de empleado solo lo usa el técnico
        List<Empleado> empleados = null;
        if (esTecnico) {
            try {
                empleados = empleadoDAO.findAll();
                for (Empleado em : empleados) empleadoCmb.addItem(em.getId() + " - " + em.getNombre() + " " + em.getApellidos());
            } catch (SQLException ex) { mostrarError(ex); return; }
        }

        if (incidencia != null) { // modo edición: rellenar campos
            titulo.setText(incidencia.getTitulo());
            descripcion.setText(incidencia.getDescripcion());
            prioridad.setSelectedItem(incidencia.getPrioridad());
            estado.setSelectedItem(incidencia.getEstado());
            if (empleados != null) seleccionar(empleadoCmb, empleados, Empleado::getId, incidencia.getEmpleadoId());
        }

        JPanel form = formulario();
        form.add(new JLabel("Título:"));      form.add(titulo);
        form.add(new JLabel("Descripción:")); form.add(descripcion);
        form.add(new JLabel("Prioridad:"));   form.add(prioridad);
        if (esTecnico) {
            form.add(new JLabel("Estado:"));   form.add(estado);
            form.add(new JLabel("Empleado:")); form.add(empleadoCmb);
        }

        final List<Empleado> emps = empleados;
        mostrarFormulario(nueva ? "Add Incidencia" : "Edit Incidencia", form, dialog -> {
            String t = titulo.getText().trim(), d = descripcion.getText().trim();
            String error = Validador.validarIncidencia(t, d);
            if (error != null) { datosInvalidos(dialog, error); return; }

            int empId = (emps != null) ? emps.get(empleadoCmb.getSelectedIndex()).getId() : usuario.getId();
            Estado est = esTecnico ? (Estado) estado.getSelectedItem() : Estado.ABIERTA;
            try {
                if (incidencia == null) {
                    incidenciaDAO.insert(new Incidencia(0, empId, t, d, (Prioridad) prioridad.getSelectedItem(), est, null));
                } else {
                    incidencia.setEmpleadoId(empId); // permite reasignar a otro empleado
                    incidencia.setTitulo(t);
                    incidencia.setDescripcion(d);
                    incidencia.setPrioridad((Prioridad) prioridad.getSelectedItem());
                    incidencia.setEstado(est);
                    incidenciaDAO.update(incidencia);
                }
                dialog.dispose();
                exito(nueva ? "Incidencia añadida correctamente." : "Incidencia actualizada correctamente.");
                cargarIncidencias();
            } catch (SQLException ex) { mostrarError(dialog, ex); }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPLEADOS
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel panelEmpleados() {
        modelEmpleados = modelo("ID", "Username", "Nombre", "Apellidos", "Departamento", "Telefono");
        cargarEmpleados();
        JTable tabla = buildTable(modelEmpleados);

        JButton add  = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del  = new JButton("Delete");

        JButton details = new JButton("Details");

        add.addActionListener(e -> mostrarDialogEmpleado(null));
        edit.addActionListener(e -> conSeleccion(tabla, f ->
            mostrarDialogEmpleado(empleadoDAO.findById((int) modelEmpleados.getValueAt(f, 0)))));
        del.addActionListener(e -> conSeleccion(tabla, f -> {
            int id = (int) modelEmpleados.getValueAt(f, 0);
            // delete borra de usuarios; ON DELETE CASCADE elimina la fila de empleados
            if (confirmar("¿Eliminar empleado #" + id + "? También se eliminarán sus incidencias y asignaciones.")) {
                empleadoDAO.delete(id); cargarEmpleados(); exito("Empleado eliminado correctamente.");
            }
        }));
        details.addActionListener(e -> conSeleccion(tabla, f -> {
            Empleado em = empleadoDAO.findById((int) modelEmpleados.getValueAt(f, 0));
            mostrarDetalles("Empleado #" + em.getId(),
                "ID", String.valueOf(em.getId()),
                "Username", em.getUsername(),
                "Email", em.getEmail(),
                "Nombre", em.getNombre(),
                "Apellidos", em.getApellidos(),
                "DNI", em.getDni(),
                "Departamento", em.getDepartamento(),
                "Teléfono", em.getTelefono());
        }));

        return panelTabla(tabla, barraInferior(add, edit, del, details));
    }

    private void cargarEmpleados() {
        modelEmpleados.setRowCount(0);
        try {
            for (Empleado em : empleadoDAO.findAll())
                modelEmpleados.addRow(new Object[]{ em.getId(), em.getUsername(), em.getNombre(),
                    em.getApellidos(), em.getDepartamento(), em.getTelefono() });
        } catch (SQLException ex) { mostrarError(ex); }
    }

    /** Diálogo Añadir/Editar empleado. Al añadir inserta usuarios+empleados en una transacción. */
    private void mostrarDialogEmpleado(Empleado empleado) {
        boolean nuevo = (empleado == null);
        JTextField username = new JTextField(20), email = new JTextField(20), nombre = new JTextField(20),
                   apellidos = new JTextField(20), dni = new JTextField(20),
                   depto = new JTextField(20), telefono = new JTextField(20);
        JPasswordField password = new JPasswordField(20);

        if (empleado != null) {
            username.setText(empleado.getUsername()); email.setText(empleado.getEmail());
            nombre.setText(empleado.getNombre());     apellidos.setText(empleado.getApellidos());
            dni.setText(empleado.getDni());           depto.setText(empleado.getDepartamento());
            telefono.setText(empleado.getTelefono());
        }

        JPanel form = formularioUsuario(username, password, email, nombre, apellidos, dni);
        form.add(new JLabel("Departamento:")); form.add(depto);
        form.add(new JLabel("Teléfono:"));     form.add(telefono);

        mostrarFormulario(nuevo ? "Add Empleado" : "Edit Empleado", form, dialog -> {
            String u = username.getText().trim(), p = new String(password.getPassword()), e = email.getText().trim(),
                   n = nombre.getText().trim(), a = apellidos.getText().trim(), dn = dni.getText().trim(),
                   dp = depto.getText().trim(), tel = telefono.getText().trim();

            String error = Validador.validarUsuario(u, p, e, n, a, dn, nuevo);
            if (error == null && !Validador.noVacio(dp)) error = "El departamento es obligatorio.";
            if (error == null && !Validador.esTelefono(tel)) error = "El teléfono debe tener 9 dígitos.";
            if (error != null) { datosInvalidos(dialog, error); return; }

            try {
                if (empleado == null) {
                    registrarConHijo(new Usuario(u, p, e, n, a, dn, Rol.EMPLEADO),
                        (conn, id) -> empleadoDAO.insert(new Empleado(id, u, p, e, n, a, dn, Rol.EMPLEADO, dp, tel), conn));
                } else {
                    String pwd = p.isEmpty() ? empleado.getPassword() : p; // mantiene la contraseña si no se cambia
                    empleadoDAO.update(new Empleado(empleado.getId(), u, pwd, e, n, a, dn, Rol.EMPLEADO, dp, tel));
                }
                dialog.dispose();
                exito(nuevo ? "Empleado añadido correctamente." : "Empleado actualizado correctamente.");
                cargarEmpleados();
            } catch (SQLException ex) { mostrarError(dialog, ex); }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TECNICOS
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel panelTecnicos() {
        modelTecnicos = modelo("ID", "Username", "Nombre", "Apellidos", "Especialidad");
        cargarTecnicos();
        JTable tabla = buildTable(modelTecnicos);

        JButton add  = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del  = new JButton("Delete");

        JButton details = new JButton("Details");

        add.addActionListener(e -> mostrarDialogTecnico(null));
        edit.addActionListener(e -> conSeleccion(tabla, f ->
            mostrarDialogTecnico(tecnicoDAO.findById((int) modelTecnicos.getValueAt(f, 0)))));
        del.addActionListener(e -> conSeleccion(tabla, f -> {
            int id = (int) modelTecnicos.getValueAt(f, 0);
            if (confirmar("¿Eliminar técnico #" + id + "? También se eliminarán sus asignaciones.")) {
                tecnicoDAO.delete(id); cargarTecnicos(); exito("Técnico eliminado correctamente.");
            }
        }));
        details.addActionListener(e -> conSeleccion(tabla, f -> {
            Tecnico t = tecnicoDAO.findById((int) modelTecnicos.getValueAt(f, 0));
            mostrarDetalles("Técnico #" + t.getId(),
                "ID", String.valueOf(t.getId()),
                "Username", t.getUsername(),
                "Email", t.getEmail(),
                "Nombre", t.getNombre(),
                "Apellidos", t.getApellidos(),
                "DNI", t.getDni(),
                "Especialidad", t.getEspecialidad());
        }));

        return panelTabla(tabla, barraInferior(add, edit, del, details));
    }

    private void cargarTecnicos() {
        modelTecnicos.setRowCount(0);
        try {
            for (Tecnico t : tecnicoDAO.findAll())
                modelTecnicos.addRow(new Object[]{ t.getId(), t.getUsername(), t.getNombre(),
                    t.getApellidos(), t.getEspecialidad() });
        } catch (SQLException ex) { mostrarError(ex); }
    }

    /** Diálogo Añadir/Editar técnico. Mismo patrón transaccional que el empleado. */
    private void mostrarDialogTecnico(Tecnico tecnico) {
        boolean nuevo = (tecnico == null);
        JTextField username = new JTextField(20), email = new JTextField(20), nombre = new JTextField(20),
                   apellidos = new JTextField(20), dni = new JTextField(20), especialidad = new JTextField(20);
        JPasswordField password = new JPasswordField(20);

        if (tecnico != null) {
            username.setText(tecnico.getUsername()); email.setText(tecnico.getEmail());
            nombre.setText(tecnico.getNombre());     apellidos.setText(tecnico.getApellidos());
            dni.setText(tecnico.getDni());           especialidad.setText(tecnico.getEspecialidad());
        }

        JPanel form = formularioUsuario(username, password, email, nombre, apellidos, dni);
        form.add(new JLabel("Especialidad:")); form.add(especialidad);

        mostrarFormulario(nuevo ? "Add Técnico" : "Edit Técnico", form, dialog -> {
            String u = username.getText().trim(), p = new String(password.getPassword()), e = email.getText().trim(),
                   n = nombre.getText().trim(), a = apellidos.getText().trim(), dn = dni.getText().trim(),
                   esp = especialidad.getText().trim();

            String error = Validador.validarUsuario(u, p, e, n, a, dn, nuevo);
            if (error == null && !Validador.noVacio(esp)) error = "La especialidad es obligatoria.";
            if (error != null) { datosInvalidos(dialog, error); return; }

            try {
                if (tecnico == null) {
                    registrarConHijo(new Usuario(u, p, e, n, a, dn, Rol.TECNICO),
                        (conn, id) -> tecnicoDAO.insert(new Tecnico(id, u, p, e, n, a, dn, Rol.TECNICO, esp), conn));
                } else {
                    String pwd = p.isEmpty() ? tecnico.getPassword() : p;
                    tecnicoDAO.update(new Tecnico(tecnico.getId(), u, pwd, e, n, a, dn, Rol.TECNICO, esp));
                }
                dialog.dispose();
                exito(nuevo ? "Técnico añadido correctamente." : "Técnico actualizado correctamente.");
                cargarTecnicos();
            } catch (SQLException ex) { mostrarError(dialog, ex); }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ASIGNACIONES
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel panelAsignaciones() {
        modelAsignaciones = modelo("ID", "Incidencia", "Tecnico", "Fecha", "Comentario");
        cargarAsignaciones();
        JTable tabla = buildTable(modelAsignaciones);

        JButton add  = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del  = new JButton("Delete");

        JButton details = new JButton("Details");

        add.addActionListener(e -> mostrarDialogAsignacion(null, 0));
        edit.addActionListener(e -> conSeleccion(tabla, f -> {
            AsignacionDTO dto = asignacionesCargadas.get(f);
            // Incidencia "Sin asignar" → abrir Añadir con esa incidencia preseleccionada
            if (dto.getId() == 0) mostrarDialogAsignacion(null, dto.getIncidenciaId());
            else mostrarDialogAsignacion(asignacionDAO.findById(dto.getId()), 0);
        }));
        del.addActionListener(e -> conSeleccion(tabla, f -> {
            AsignacionDTO dto = asignacionesCargadas.get(f);
            if (dto.getId() == 0) { aviso("Esta incidencia no tiene ninguna asignación que eliminar."); return; }
            if (confirmar("¿Eliminar asignación #" + dto.getId() + "?")) {
                asignacionDAO.delete(dto.getId()); cargarAsignaciones(); exito("Asignación eliminada correctamente.");
            }
        }));
        details.addActionListener(e -> conSeleccion(tabla, f -> {
            AsignacionDTO a = asignacionesCargadas.get(f);
            mostrarDetalles("Asignación",
                "ID", a.getId() == 0 ? "—" : String.valueOf(a.getId()),
                "Incidencia", a.getIncidenciaTitulo(),
                "Técnico", a.getTecnicoNombre(),
                "Fecha", a.getFechaAsignacion() != null ? a.getFechaAsignacion().format(FECHA_FORMAT) : "—",
                "Comentario", a.getComentario() != null ? a.getComentario() : "");
        }));

        return panelTabla(tabla, barraInferior(add, edit, del, details));
    }

    /** Muestra TODAS las incidencias; las que no tienen técnico aparecen como "Sin asignar". */
    private void cargarAsignaciones() {
        modelAsignaciones.setRowCount(0);
        try {
            asignacionesCargadas = asignacionDAO.findAllWithDetails();
            for (AsignacionDTO a : asignacionesCargadas) {
                modelAsignaciones.addRow(new Object[]{
                    a.getId() == 0 ? "—" : a.getId(),
                    a.getIncidenciaTitulo(),
                    a.getTecnicoNombre(),
                    a.getFechaAsignacion() != null ? a.getFechaAsignacion().format(FECHA_FORMAT) : "—",
                    a.getComentario() != null ? a.getComentario() : ""
                });
            }
        } catch (SQLException ex) { mostrarError(ex); }
    }

    /**
     * Diálogo Añadir/Editar asignación. incidenciaPre (≠ 0) preselecciona una incidencia
     * al añadir (se usa al editar una incidencia que aún está "Sin asignar").
     */
    private void mostrarDialogAsignacion(Asignacion asignacion, int incidenciaPre) {
        boolean nueva = (asignacion == null);
        JComboBox<String> incidenciaCmb = new JComboBox<>(), tecnicoCmb = new JComboBox<>();
        JTextField comentario = new JTextField(20);

        List<Incidencia> incidencias;
        List<Tecnico> tecnicos;
        try {
            incidencias = incidenciaDAO.findAll();
            for (Incidencia i : incidencias) incidenciaCmb.addItem(i.getId() + " - " + i.getTitulo());
            tecnicos = tecnicoDAO.findAll();
            for (Tecnico t : tecnicos) tecnicoCmb.addItem(t.getId() + " - " + t.getNombre() + " " + t.getApellidos());
        } catch (SQLException ex) { mostrarError(ex); return; }

        if (asignacion != null) {
            comentario.setText(asignacion.getComentario());
            seleccionar(incidenciaCmb, incidencias, Incidencia::getId, asignacion.getIncidenciaId());
            seleccionar(tecnicoCmb, tecnicos, Tecnico::getId, asignacion.getTecnicoId());
        } else if (incidenciaPre != 0) {
            seleccionar(incidenciaCmb, incidencias, Incidencia::getId, incidenciaPre);
        }

        JPanel form = formulario();
        form.add(new JLabel("Incidencia:")); form.add(incidenciaCmb);
        form.add(new JLabel("Técnico:"));    form.add(tecnicoCmb);
        form.add(new JLabel("Comentario:")); form.add(comentario);

        final List<Incidencia> incs = incidencias;
        final List<Tecnico> tecs = tecnicos;
        mostrarFormulario(nueva ? "Add Asignación" : "Edit Asignación", form, dialog -> {
            if (incidenciaCmb.getSelectedIndex() == -1 || tecnicoCmb.getSelectedIndex() == -1) {
                datosInvalidos(dialog, "Selecciona una incidencia y un técnico."); return;
            }
            int incId = incs.get(incidenciaCmb.getSelectedIndex()).getId();
            int tecId = tecs.get(tecnicoCmb.getSelectedIndex()).getId();
            String com = comentario.getText().trim();
            try {
                if (asignacion == null) {
                    asignacionDAO.insert(new Asignacion(0, incId, tecId, null, com));
                } else {
                    asignacion.setIncidenciaId(incId);
                    asignacion.setTecnicoId(tecId);
                    asignacion.setComentario(com);
                    asignacionDAO.update(asignacion);
                }
                dialog.dispose();
                exito(nueva ? "Asignación añadida correctamente." : "Asignación actualizada correctamente.");
                cargarAsignaciones();
            } catch (SQLException ex) { mostrarError(dialog, ex); }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS  (reutilizados por todas las pestañas)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Crea un DefaultTableModel no editable con las columnas dadas. */
    private DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /** Da estilo a una JTable de selección única. */
    private JTable buildTable(DefaultTableModel model) {
        JTable tabla = new JTable(model);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        return tabla;
    }

    /** Monta el panel de una pestaña: la tabla en el centro y la barra de botones abajo. */
    private JPanel panelTabla(JTable tabla, JPanel barra) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(barra, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Barra inferior de una pestaña: a la izquierda los botones CRUD recibidos y a la
     * derecha los botones globales de refrescar y de cambiar tema.
     */
    private JPanel barraInferior(JButton... crud) {
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        for (JButton b : crud) izquierda.add(b);

        JButton btnRefresh = iconButton("🔄", "Refresh all tables");
        btnRefresh.addActionListener(e -> refrescarTablas());
        JButton btnTema = iconButton(darkMode ? "🌙" : "☀️", "Toggle light/dark mode");
        btnTema.addActionListener(e -> cambiarTema());
        botonesTema.add(btnTema); // se guarda para sincronizar su icono al cambiar el tema

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        derecha.add(btnRefresh);
        derecha.add(btnTema);

        JPanel barra = new JPanel(new BorderLayout());
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    /** Crea un botón de icono grande con tooltip. */
    private JButton iconButton(String icono, String tooltip) {
        JButton b = new JButton(icono);
        b.setToolTipText(tooltip);
        b.setFont(b.getFont().deriveFont(16f));
        return b;
    }

    /** Alterna tema claro/oscuro y sincroniza el icono de todos los botones de tema. */
    private void cambiarTema() {
        try {
            String icono;
            if (darkMode) { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf"); icono = "🌙"; }
            else          { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");  icono = "☀️"; }
            darkMode = !darkMode;
            for (JButton b : botonesTema) b.setText(icono);
            SwingUtilities.updateComponentTreeUI(this); // aplica el tema sin reiniciar la ventana
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cambiar el tema: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Recarga el contenido de todas las tablas existentes (las no creadas según el rol se ignoran). */
    private void refrescarTablas() {
        if (modelIncidencias  != null) cargarIncidencias();
        if (modelAsignaciones != null) cargarAsignaciones();
        if (modelEmpleados    != null) cargarEmpleados();
        if (modelTecnicos     != null) cargarTecnicos();
    }

    /** Panel de formulario vacío en rejilla de 2 columnas (etiqueta + campo por fila). */
    private JPanel formulario() {
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        return form;
    }

    /** Formulario con los 6 campos comunes a todo usuario (lo amplían empleado/técnico). */
    private JPanel formularioUsuario(JTextField username, JPasswordField password, JTextField email,
                                     JTextField nombre, JTextField apellidos, JTextField dni) {
        JPanel form = formulario();
        form.add(new JLabel("Username:"));  form.add(username);
        form.add(new JLabel("Password:"));  form.add(password);
        form.add(new JLabel("Email:"));     form.add(email);
        form.add(new JLabel("Nombre:"));    form.add(nombre);
        form.add(new JLabel("Apellidos:")); form.add(apellidos);
        form.add(new JLabel("DNI:"));       form.add(dni);
        return form;
    }

    /**
     * Inserta un usuario y su fila hija (empleado/técnico) dentro de una transacción.
     * Si algo falla hace rollback de ambos inserts.
     */
    private void registrarConHijo(Usuario usuario, InsertHijo insertarHijo) throws SQLException {
        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                usuarioDAO.register(usuario, conn);   // inserta en usuarios y obtiene el id generado
                insertarHijo.insertar(conn, usuario.getId());
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    /** Selecciona en el desplegable el elemento de la lista cuyo id coincide con el dado. */
    private <T> void seleccionar(JComboBox<String> combo, List<T> lista, ToIntFunction<T> getId, int id) {
        for (int i = 0; i < lista.size(); i++) {
            if (getId.applyAsInt(lista.get(i)) == id) { combo.setSelectedIndex(i); return; }
        }
    }

    /**
     * Monta y muestra un diálogo modal de formulario con botones Save/Cancel.
     * onGuardar recibe el propio diálogo (para cerrarlo al guardar o mostrar errores sobre él).
     */
    private void mostrarFormulario(String titulo, JPanel form, Consumer<JDialog> onGuardar) {
        JDialog dialog = new JDialog(this, titulo, true);
        JButton save = new JButton("Save"), cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> onGuardar.accept(dialog));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(save);
        botones.add(cancel);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(botones, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Muestra un diálogo de solo lectura con la información completa de un registro.
     * Recibe pares (etiqueta, valor): "Campo1", valor1, "Campo2", valor2, ...
     */
    private void mostrarDetalles(String titulo, String... pares) {
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        for (int i = 0; i + 1 < pares.length; i += 2) {
            JLabel etiqueta = new JLabel(pares[i] + ":");
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
            form.add(etiqueta);
            String valor = (pares[i + 1] == null) ? "" : pares[i + 1];
            // HTML para que los textos largos (descripción, comentario) se ajusten en varias líneas
            form.add(new JLabel("<html><body style='width:260px'>" + valor.replace("<", "&lt;") + "</body></html>"));
        }
        JButton cerrar = new JButton("Close");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(cerrar);

        JDialog dialog = new JDialog(this, titulo, true);
        cerrar.addActionListener(e -> dialog.dispose());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(botones, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Ejecuta una acción sobre la fila seleccionada de la tabla. Si no hay fila avisa;
     * si la acción lanza un error de BD lo muestra. Evita repetir este patrón en cada botón.
     */
    private void conSeleccion(JTable tabla, AccionFila accion) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { aviso("Selecciona una fila de la tabla."); return; }
        try { accion.ejecutar(fila); }
        catch (SQLException ex) { mostrarError(ex); }
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar eliminación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void exito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void datosInvalidos(JDialog dialog, String mensaje) {
        JOptionPane.showMessageDialog(dialog, mensaje, "Datos no válidos", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarError(SQLException ex) {
        mostrarError(this, ex);
    }

    private void mostrarError(Component padre, SQLException ex) {
        JOptionPane.showMessageDialog(padre, "Error de base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
