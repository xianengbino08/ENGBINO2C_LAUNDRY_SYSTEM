package com.laundry.dao;

import com.laundry.model.Customer;
import com.laundry.model.LaundryOrder;
import com.laundry.model.ServiceRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LaundryDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public List<Customer> getCustomers() {
        String sql = "SELECT customer_id, full_name FROM customers ORDER BY customer_id";
        List<Customer> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Customer(rs.getInt("customer_id"), rs.getString("full_name")));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return list;
    }

    public List<ServiceRate> getActiveServices() {
        String sql = "SELECT service_id, service_name, rate_per_kg FROM service_rates "
                + "WHERE status = ? ORDER BY service_id";
        List<ServiceRate> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Active");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ServiceRate(rs.getInt("service_id"),
                            rs.getString("service_name"),
                            rs.getDouble("rate_per_kg")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return list;
    }

    public int createOrder(int staffId, Customer customer, ServiceRate service, double weightKg) {
        double total = weightKg * service.ratePerKg();
        String sql = "INSERT INTO laundry_orders (customer_id, staff_id, service_id, weight_kg, total_price, order_status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int orderId;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, customer.customerId());
                ps.setInt(2, staffId);
                ps.setInt(3, service.serviceId());
                ps.setDouble(4, weightKg);
                ps.setDouble(5, total);
                ps.setString(6, "RECEIVED");
                ps.executeUpdate();
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();
                orderId = rs.getInt(1);
            }

            logDao.logAction(staffId, "ORDER_CREATED",
                    "Created Order #" + orderId + " for " + customer.fullName()
                            + " - Total: P" + String.format("%.2f", total));
            return orderId;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return 0;
        }
    }

    public List<LaundryOrder> getAllOrders() {
        String sql = "SELECT o.order_id, c.full_name, s.service_name, o.weight_kg, o.total_price, "
                + "o.order_status, o.payment_status "
                + "FROM laundry_orders o "
                + "JOIN customers c ON o.customer_id = c.customer_id "
                + "JOIN service_rates s ON o.service_id = s.service_id "
                + "ORDER BY o.order_id DESC";
        List<LaundryOrder> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LaundryOrder(rs.getInt("order_id"),
                        rs.getString("full_name"),
                        rs.getString("service_name"),
                        rs.getDouble("weight_kg"),
                        rs.getDouble("total_price"),
                        rs.getString("order_status"),
                        rs.getString("payment_status")));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateOrderStatus(int staffId, int orderId, String newStatus) {
        String selectSql = "SELECT order_status FROM laundry_orders WHERE order_id = ?";
        String updateSql = "UPDATE laundry_orders SET order_status = ?, "
                + "date_claimed = CASE WHEN ? = 'CLAIMED' THEN datetime('now', 'localtime') ELSE date_claimed END "
                + "WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            String oldStatus;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    oldStatus = rs.getString("order_status").toUpperCase();
                }
            }

            boolean done;
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus.toUpperCase());
                ps.setString(2, newStatus.toUpperCase());
                ps.setInt(3, orderId);
                done = ps.executeUpdate() > 0;
            }

            if (done) {
                logDao.logAction(staffId, "ORDER_STATUS_UPDATED",
                        "Updated Order #" + orderId + " status from " + oldStatus
                                + " to " + newStatus.toUpperCase());
            }
            return done;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
}