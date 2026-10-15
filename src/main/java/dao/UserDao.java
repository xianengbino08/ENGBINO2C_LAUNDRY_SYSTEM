package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// DAO = Data Access Object. All the SQL for the users table is here.
public class UserDao {

    // CREATE: save a new user
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, user.password());
            ps.setString(3, user.roles());
            ps.setString(4, user.status());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // READ: check if a username is already used
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // READ: check username and password. Returns the User, or null if wrong.
    public User login(String username, String password) {
        String sql = "SELECT id, username, password, roles, status FROM users "
                   + "WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"),
                                    rs.getString("username"),
                                    rs.getString("password"),
                                    rs.getString("roles"),
                                    rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }
}
