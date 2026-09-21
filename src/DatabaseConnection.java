import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/pims_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "re4L01geek@"; // Update to your actual PostgreSQL password!

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }
}