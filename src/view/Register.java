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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;

public class Register extends JFrame {

    private JLabel lblUsername, lblPassword, lblEmail, lblNombre, lblApellidos, lblDni, lblRol;
    private JTextField txtUsername, txtEmail, txtNombre, txtApellidos, txtDni;
    private JPasswordField txtPassword;
    private JComboBox<Rol> cmbRol;
    private JButton btnRegister, btnBack;

    // Dynamic fields — shown depending on the selected role
    private JLabel lblDepartamento, lblTelefono, lblEspecialidad;
    private JTextField txtDepartamento, txtTelefono, txtEspecialidad;

    public Register() {
        setTitle("Helpdesk - Register");
        setSize(450, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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
        cmbRol = new JComboBox<>(Rol.values());
        cmbRol.setSelectedIndex(-1);

        lblDepartamento = new JLabel("Departamento:");
        lblTelefono = new JLabel("Telefono:");
        lblEspecialidad = new JLabel("Especialidad:");
        txtDepartamento = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtEspecialidad = new JTextField(20);

        btnRegister = new JButton("Register");
        btnBack = new JButton("Back");

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

        JPanel panelDinamico = new JPanel(new GridLayout(2, 2, 10, 10));
        panelDinamico.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        panelBotones.add(btnRegister);
        panelBotones.add(btnBack);

        // Show/hide dynamic fields depending on the selected role
        cmbRol.addItemListener(e -> {
            panelDinamico.removeAll();
            Rol rolSeleccionado = (Rol) cmbRol.getSelectedItem();

            if (rolSeleccionado == Rol.EMPLEADO) {
                panelDinamico.setLayout(new GridLayout(2, 2, 10, 10));
                setSize(450, 440);
                panelDinamico.add(lblDepartamento);
                panelDinamico.add(txtDepartamento);
                panelDinamico.add(lblTelefono);
                panelDinamico.add(txtTelefono);
            } else if (rolSeleccionado == Rol.TECNICO) {
                panelDinamico.setLayout(new GridLayout(1, 2, 10, 10));
                setSize(450, 400);
                panelDinamico.add(lblEspecialidad);
                panelDinamico.add(txtEspecialidad);
            }

            panelDinamico.revalidate();
            panelDinamico.repaint();
        });

        JPanel container = new JPanel(new BorderLayout());
        container.add(panelMain, BorderLayout.NORTH);
        container.add(panelDinamico, BorderLayout.CENTER);
        container.add(panelBotones, BorderLayout.SOUTH);

        add(container);

        // Register button — runs the 3 inserts inside a single transaction
        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = String.valueOf(txtPassword.getPassword());
            String email = txtEmail.getText();
            String nombre = txtNombre.getText();
            String apellidos = txtApellidos.getText();
            String dni = txtDni.getText();
            Rol rol = (Rol) cmbRol.getSelectedItem();

            if (rol == null) {
                JOptionPane.showMessageDialog(this, "Please select a role", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Use interfaces as types — good DAO pattern practice
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();
            TecnicoDAO tecnicoDAO = new TecnicoDAOImpl();

            try (Connection conn = ConexionDB.getConnection()) {
                // Disable autocommit to manage the transaction manually
                conn.setAutoCommit(false);

                try {
                    // Insert into usuarios — the DAO sets the generated id on the object
                    Usuario usuario = new Usuario(username, password, email, nombre, apellidos, dni, rol);
                    usuarioDAO.register(usuario, conn);

                    // Insert into the child table depending on the role
                    if (rol == Rol.EMPLEADO) {
                        Empleado emp = new Empleado(usuario.getId(), username, password, email, nombre, apellidos, dni, rol,txtDepartamento.getText(), txtTelefono.getText());
                        empleadoDAO.insert(emp, conn);
                    } else {
                        Tecnico tec = new Tecnico(usuario.getId(), username, password, email, nombre, apellidos, dni, rol, txtEspecialidad.getText());
                        tecnicoDAO.insert(tec, conn);
                    }

                    // All inserts successful — commit the transaction
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "User registered successfully!");
                    dispose();
                    new Login();

                } catch (SQLException ex) {
                    // Something failed — undo all changes
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back button — close this window and reopen Login
        btnBack.addActionListener(e -> {
            dispose();
            new Login();
        });

        setVisible(true);
    }
}
