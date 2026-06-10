import javax.swing.UIManager;
import view.Login;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // Si FlatLaf no está disponible la app arranca con el L&F por defecto
        }
        Login login = new Login();
        login.setVisible(true);
    }
}
