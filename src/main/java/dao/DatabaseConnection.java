package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // The database file "app.db" is created automatically in the project folder.
    private static final String URL = "jdbc:sqlite:app.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Creates the users table the first time the program runs.
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "username TEXT NOT NULL UNIQUE, "
                   + "password TEXT NOT NULL, "
                   + "roles TEXT NOT NULL, "
                   + "status TEXT NOT NULL DEFAULT 'PENDING')";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Could not create table: " + e.getMessage());
        }
    }
}
