package view;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.*;
import model.Usuario;

/** Login window — entry point of the application */
public class Login extends JFrame {

    private final JLabel lblUsername, lblPassword;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private final JButton btnLogin, btnRegister;

    public Login() {
        setTitle("Helpdesk - Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center on screen
        setResizable(false); // prevent resizing and fullscreen

        lblUsername = new JLabel("Username: ");
        lblPassword = new JLabel("Password: ");
        txtUsername = new JTextField(30);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");

        // GridLayout(rows, cols, hgap, vgap) — 3 rows, 2 cols with spacing
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); // padding
        panel.add(lblUsername);
        panel.add(txtUsername);
        panel.add(lblPassword);
        panel.add(txtPassword);
        panel.add(btnLogin);
        panel.add(btnRegister);

        add(panel, BorderLayout.CENTER);

        // Login button — validates credentials and opens Dashboard
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            // getPassword() returns char[] — convert to String for comparison
            String password = new String(txtPassword.getPassword());

            try {
                UsuarioDAO dao = new UsuarioDAOImpl();
                Usuario usuario = dao.validate(username, password);

                if (usuario == null) {
                    // Wrong credentials
                    JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Correct credentials — close Login and open Dashboard
                    dispose();
                    Dashboard dashboard = new Dashboard(usuario);
                    dashboard.setVisible(true);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Register button — close Login and open Register window
        btnRegister.addActionListener(e -> {
            Register register = new Register();
            register.setVisible(true);
            dispose();
        });

        setVisible(true);
    }
}
