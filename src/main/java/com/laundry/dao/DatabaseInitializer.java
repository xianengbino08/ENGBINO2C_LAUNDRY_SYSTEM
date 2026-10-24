package com.laundry.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates every table from the approved ERD if it does not exist yet.
 * Runs once at startup, before any menu is shown.
 */
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
                log_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER,
                action       TEXT NOT NULL,
                description  TEXT,
                log_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
            """;

    public static void initialize() {
        System.out.println("Initializing database...");

        // order matters: tables that are referenced come first
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
            System.out.println("Database ready: all 5 tables exist.");
        } catch (SQLException e) {
            System.out.println("Could not initialize the database: " + e.getMessage());
            System.exit(1);   // no point running the app without a database
        }
    }
}
