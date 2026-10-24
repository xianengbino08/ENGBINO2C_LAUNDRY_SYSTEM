package com.laundry.dao;

import com.laundry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// DAO = Data Access Object. All SQL for users (and the customer profile) is here.
public class UserDao {

    // CREATE: save a new user. A CUSTOMER also gets a row in the customers table.
    public boolean register(User user) {
        String insertUser = "INSERT INTO users (username, password, full_name, role, status) "
                          + "VALUES (?, ?, ?, ?, ?)";
        String insertCustomer = "INSERT INTO customers (user_id, full_name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);   // both inserts succeed together or not at all
            try {
                int newId = 0;

                try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                    ps.setString(1, user.username());
                    ps.setString(2, user.password());
                    ps.setString(3, user.fullName());
                    ps.setString(4, user.role());
                    ps.setString(5, user.status());
                    ps.executeUpdate();
                }

                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    rs.next();
                    newId = rs.getInt(1);
                }

                if (user.role().equals("CUSTOMER")) {
                    try (PreparedStatement ps = conn.prepareStatement(insertCustomer)) {
                        ps.setInt(1, newId);
                        ps.setString(2, user.fullName());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // READ: is this username already used?
    public boolean usernameExists(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";

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
        String sql = "SELECT user_id, username, password, full_name, role, status "
                   + "FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toUser(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }

    // READ: every user (used by the Admin menu)
    public List<User> findAll() {
        String sql = "SELECT user_id, username, password, full_name, role, status "
                   + "FROM users ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(toUser(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return users;
    }

    // turns one row into a User record
    private User toUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("status"));
    }
}
