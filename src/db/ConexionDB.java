package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class that provides a JDBC connection to the helpdesk database.
 * Rename config.properties.example to config.properties and fill in your
 * credentials before running the application.
 */
public class ConexionDB {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;


    // Load database credentials from config.properties at class initialization
    static {
        Properties props = new Properties();
        try (InputStream is = ConexionDB.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    /** Returns a new JDBC connection to the database */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
