package com.laundry.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                user_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                username     TEXT NOT NULL UNIQUE,
                password     TEXT NOT NULL,
                full_name    TEXT NOT NULL,
                role         TEXT NOT NULL,
                status       TEXT NOT NULL DEFAULT 'PENDING',
                date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String CREATE_CUSTOMERS = """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id         INTEGER UNIQUE,
                full_name       TEXT NOT NULL,
                contact_number  TEXT,
                address         TEXT,
                date_registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
            """;

    private static final String CREATE_SERVICE_RATES = """
            CREATE TABLE IF NOT EXISTS service_rates (
                service_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                service_name TEXT NOT NULL,
                rate_per_kg  REAL NOT NULL,
                status       TEXT DEFAULT 'Active',
                date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String CREATE_LAUNDRY_ORDERS = """
            CREATE TABLE IF NOT EXISTS laundry_orders (
                order_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id    INTEGER NOT NULL,
                staff_id       INTEGER NOT NULL,
                service_id     INTEGER NOT NULL,
                weight_kg      REAL NOT NULL,
                total_price    REAL NOT NULL,
                payment_status TEXT DEFAULT 'Unpaid',
                order_status   TEXT DEFAULT 'Received',
                date_received  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                date_claimed   TIMESTAMP,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                FOREIGN KEY (staff_id)    REFERENCES users(user_id),
                FOREIGN KEY (service_id)  REFERENCES service_rates(service_id)
            )
            """;

    private static final String CREATE_ACTIVITY_LOGS = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                description TEXT,
                timestamp   TIMESTAMP DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """;

    private static void seedServices(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM service_rates")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }

        String sql = "INSERT INTO service_rates (service_name, rate_per_kg) VALUES (?, ?)";
        String[] names = {"Wash Only", "Wash and Dry", "Wash Dry and Fold"};
        double[] rates = {30.0, 50.0, 70.0};

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < names.length; i++) {
                ps.setString(1, names[i]);
                ps.setDouble(2, rates[i]);
                ps.executeUpdate();
            }
        }
    }

    public static void initialize() {
        System.out.println("Initializing database...");

        String[] statements = {
                CREATE_USERS,
                CREATE_CUSTOMERS,
                CREATE_SERVICE_RATES,
                CREATE_LAUNDRY_ORDERS,
                CREATE_ACTIVITY_LOGS
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            for (String sql : statements) {
                st.execute(sql);
            }
            seedServices(conn);
            System.out.println("Database ready: all 5 tables exist.");
        } catch (SQLException e) {
            System.out.println("Could not initialize the database: " + e.getMessage());
            System.exit(1);
        }
    }
}