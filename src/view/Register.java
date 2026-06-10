package view;

import model.Empleado;
import model.Rol;
import model.Tecnico;
import model.Usuario;

import javax.swing.*;

import dao.EmpleadoDAO;
import dao.EmpleadoDAOImpl;
import dao.TecnicoDAO;
import dao.TecnicoDAOImpl;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import db.ConexionDB;
import util.Validador;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;

/** Registration window — allows creating a new EMPLEADO or TECNICO account */
public class Register extends JFrame {

    private JLabel lblUsername, lblPassword, lblEmail, lblNombre, lblApellidos, lblDni, lblRol;
    private JTextField txtUsername, txtEmail, txtNombre, txtApellidos, txtDni;
    private JPasswordField txtPassword;
    private JComboBox<Rol> cmbRol;
    private JButton btnRegister, btnBack;

    // Dynamic fields — declared as fields because they are accessed from both
    // the ItemListener (to add them to the panel) and the ActionListener (to read their values)
    private JLabel lblDepartamento, lblTelefono, lblEspecialidad;
    private JTextField txtDepartamento, txtTelefono, txtEspecialidad;

    public Register() {
        setTitle("Helpdesk - Register");
        setSize(450, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // prevent resizing and fullscreen

        // ── Labels and fields ─────────────────────────────────────────────────
        lblUsername = new JLabel("Username:");
        lblPassword = new JLabel("Password:");
        lblEmail = new JLabel("Email:");
        lblNombre = new JLabel("Nombre:");
        lblApellidos = new JLabel("Apellidos:");
        lblDni = new JLabel("DNI:");
        lblRol = new JLabel("Rol:");

        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtEmail = new JTextField(20);
        txtNombre = new JTextField(20);
        txtApellidos = new JTextField(20);
        txtDni = new JTextField(20);

        // Rol.values() fills the combo with all enum values (EMPLEADO, TECNICO)
        // setSelectedIndex(-1) so no option is pre-selected by default
        cmbRol = new JComboBox<>(Rol.values());
        cmbRol.setSelectedIndex(-1);

        // Dynamic fields — only shown after a role is selected
        lblDepartamento = new JLabel("Departamento:");
        lblTelefono = new JLabel("Telefono:");
        lblEspecialidad = new JLabel("Especialidad:");
        txtDepartamento = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtEspecialidad = new JTextField(20);

        btnRegister = new JButton("Register");
        btnBack = new JButton("Back");

        // ── Static panel (always visible) ─────────────────────────────────────
        // GridLayout(7, 2) — 7 rows, 2 columns (label + field per row)
        JPanel panelMain = new JPanel(new GridLayout(7, 2, 10, 10));
        panelMain.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        panelMain.add(lblUsername);
        panelMain.add(txtUsername);
        panelMain.add(lblPassword);
        panelMain.add(txtPassword);
        panelMain.add(lblEmail);
        panelMain.add(txtEmail);
        panelMain.add(lblNombre);
        panelMain.add(txtNombre);
        panelMain.add(lblApellidos);
        panelMain.add(txtApellidos);
        panelMain.add(lblDni);
        panelMain.add(txtDni);
        panelMain.add(lblRol);
        panelMain.add(cmbRol);

        // ── Dynamic panel (changes based on selected role) ────────────────────
        JPanel panelDinamico = new JPanel(new GridLayout(2, 2, 10, 10));
        panelDinamico.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // ── Buttons panel ─────────────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        panelBotones.add(btnRegister);
        panelBotones.add(btnBack);

        // ── Role selector listener ────────────────────────────────────────────
        // Fires every time the selected item changes
        // removeAll() clears the dynamic panel before adding the new fields
        // revalidate() + repaint() force Swing to re-render the panel
        cmbRol.addItemListener(e -> {
            panelDinamico.removeAll();
            Rol rolSeleccionado = (Rol) cmbRol.getSelectedItem();

            if (rolSeleccionado == Rol.EMPLEADO) {
                panelDinamico.setLayout(new GridLayout(2, 2, 10, 10)); // 2 fields
                setSize(450, 440); // taller window for 2 extra fields
                panelDinamico.add(lblDepartamento);
                panelDinamico.add(txtDepartamento);
                panelDinamico.add(lblTelefono);
                panelDinamico.add(txtTelefono);
            } else if (rolSeleccionado == Rol.TECNICO) {
                panelDinamico.setLayout(new GridLayout(1, 2, 10, 10)); // 1 field
                setSize(450, 400); // slightly taller for 1 extra field
                panelDinamico.add(lblEspecialidad);
                panelDinamico.add(txtEspecialidad);
            }

            panelDinamico.revalidate();
            panelDinamico.repaint();
        });

        // ── Layout container ──────────────────────────────────────────────────
        // BorderLayout splits the window into 3 vertical sections
        JPanel container = new JPanel(new BorderLayout());
        container.add(panelMain, BorderLayout.NORTH);
        container.add(panelDinamico, BorderLayout.CENTER);
        container.add(panelBotones, BorderLayout.SOUTH);
        add(container);

        // ── Register button ───────────────────────────────────────────────────
        // Validates all fields, then runs 2-3 inserts inside a single transaction:
        // 1. INSERT into usuarios (gets generated id)
        // 2. INSERT into empleados or tecnicos using that id
        // If any insert fails, rollback undoes everything
        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText();
            // getPassword() returns char[] for security — convert to String for DB
            String password = String.valueOf(txtPassword.getPassword());
            String email = txtEmail.getText();
            String nombre = txtNombre.getText();
            String apellidos = txtApellidos.getText();
            String dni = txtDni.getText();
            Rol rol = (Rol) cmbRol.getSelectedItem();

            // Primero hay que haber seleccionado un rol
            if (rol == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un rol", "Datos no válidos", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validación de los campos comunes de usuario (al registrarse la contraseña es obligatoria).
            // Reutiliza las mismas reglas que el resto de formularios (clase Validador).
            String error = Validador.validarUsuario(username, password, email, nombre, apellidos, dni, true);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Datos no válidos", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validación de los campos específicos según el rol
            if (rol == Rol.EMPLEADO) {
                if (!Validador.noVacio(txtDepartamento.getText())) {
                    JOptionPane.showMessageDialog(this, "El departamento es obligatorio.", "Datos no válidos", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Validador.esTelefono(txtTelefono.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "El teléfono debe tener 9 dígitos.", "Datos no válidos", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (rol == Rol.TECNICO && !Validador.noVacio(txtEspecialidad.getText())) {
                JOptionPane.showMessageDialog(this, "La especialidad es obligatoria.", "Datos no válidos", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Use interfaces as types — good DAO pattern practice
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();
            TecnicoDAO tecnicoDAO = new TecnicoDAOImpl();

            try (Connection conn = ConexionDB.getConnection()) {
                // Disable autocommit — we manage the transaction manually
                conn.setAutoCommit(false);

                try {
                    // Step 1: insert into usuarios — DAO sets the generated id on the object
                    Usuario usuario = new Usuario(username, password, email, nombre, apellidos, dni, rol);
                    usuarioDAO.register(usuario, conn);

                    // Step 2: insert into the child table using the generated id
                    if (rol == Rol.EMPLEADO) {
                        Empleado emp = new Empleado(usuario.getId(), username, password, email, nombre, apellidos, dni, rol,
                                                    txtDepartamento.getText(), txtTelefono.getText());
                        empleadoDAO.insert(emp, conn);
                    } else {
                        Tecnico tec = new Tecnico(usuario.getId(), username, password, email, nombre, apellidos, dni, rol,
                                                  txtEspecialidad.getText());
                        tecnicoDAO.insert(tec, conn);
                    }

                    // All inserts successful — commit the transaction
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "User registered successfully!");
                    dispose();
                    Login loginAfterRegister = new Login();
                    loginAfterRegister.setVisible(true);

                } catch (SQLException ex) {
                    // Something failed — rollback undoes all inserts in this transaction
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back button — close this window and return to Login
        btnBack.addActionListener(e -> {
            dispose();
            Login loginBack = new Login();
            loginBack.setVisible(true);
        });

        setVisible(true);
    }
}
