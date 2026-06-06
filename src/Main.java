import db.ConexionDB;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection()) {
            System.out.println("Connection successful!");
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}