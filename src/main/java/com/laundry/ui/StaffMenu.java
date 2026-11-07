package com.laundry.ui;

import com.laundry.dao.LaundryDao;
import com.laundry.dao.UserDao;
import com.laundry.model.Customer;
import com.laundry.model.LaundryOrder;
import com.laundry.model.ServiceRate;
import com.laundry.model.User;

import java.util.List;
import java.util.Scanner;

public class StaffMenu {

    private static final String[] STATUSES = {"RECEIVED", "WASHING", "READY", "CLAIMED"};

    private final User user;
    private final UserDao userDao;
    private final Scanner scanner;
    private final LaundryDao laundryDao = new LaundryDao();

    public StaffMenu(User user, UserDao userDao, Scanner scanner) {
        this.user = user;
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== STAFF MENU =====");
            System.out.println("Welcome, " + user.fullName() + "!  (Status: " + user.status() + ")");
            System.out.println("1. Create laundry order");
            System.out.println("2. View all orders");
            System.out.println("3. Update order status");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                createOrder();
            } else if (choice.equals("2")) {
                printOrderTable(laundryDao.getAllOrders());
            } else if (choice.equals("3")) {
                updateOrderStatus();
            } else if (choice.equals("4")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void createOrder() {
        System.out.println("\n--- CREATE LAUNDRY ORDER ---");

        List<Customer> customers = laundryDao.getCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered yet.");
            return;
        }
        printCustomerTable(customers);

        Customer customer;
        while (true) {
            int id = readInt("Enter the Customer ID (0 to cancel): ");
            if (id == 0) {
                System.out.println("Order cancelled.");
                return;
            }
            customer = findCustomer(customers, id);
            if (customer != null) {
                break;
            }
            System.out.println("Customer ID " + id + " does not exist. Try again.");
        }

        List<ServiceRate> services = laundryDao.getActiveServices();
        if (services.isEmpty()) {
            System.out.println("No services available.");
            return;
        }
        printServiceTable(services);

        ServiceRate service;
        while (true) {
            int id = readInt("Enter the Service ID (0 to cancel): ");
            if (id == 0) {
                System.out.println("Order cancelled.");
                return;
            }
            service = findService(services, id);
            if (service != null) {
                break;
            }
            System.out.println("Service ID " + id + " does not exist. Try again.");
        }

        double weight = readWeight();
        if (weight == 0) {
            System.out.println("Order cancelled.");
            return;
        }

        int orderId = laundryDao.createOrder(user.userId(), customer, service, weight);
        if (orderId > 0) {
            System.out.println("SUCCESS: Order #" + orderId + " created for " + customer.fullName()
                    + ". Total: P" + String.format("%.2f", weight * service.ratePerKg()));
        } else {
            System.out.println("Could not create the order. Please try again.");
        }
    }

    private void updateOrderStatus() {
        System.out.println("\n--- UPDATE ORDER STATUS ---");

        List<LaundryOrder> orders = laundryDao.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        printOrderTable(orders);

        LaundryOrder order;
        while (true) {
            int id = readInt("Enter the Order ID (0 to cancel): ");
            if (id == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            order = findOrder(orders, id);
            if (order != null) {
                break;
            }
            System.out.println("Order ID " + id + " does not exist. Try again.");
        }

        String newStatus;
        while (true) {
            System.out.println("Choose the new status:");
            for (int i = 0; i < STATUSES.length; i++) {
                System.out.println((i + 1) + ". " + STATUSES[i]);
            }
            int pick = readInt("Status number (0 to cancel): ");
            if (pick == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (pick >= 1 && pick <= STATUSES.length) {
                newStatus = STATUSES[pick - 1];
                break;
            }
            System.out.println("Invalid status number. Try again.");
        }

        if (newStatus.equalsIgnoreCase(order.orderStatus())) {
            System.out.println("Order #" + order.orderId() + " is already " + newStatus + ".");
            return;
        }

        if (laundryDao.updateOrderStatus(user.userId(), order.orderId(), newStatus)) {
            System.out.println("SUCCESS: Order #" + order.orderId() + " is now " + newStatus + ".");
        } else {
            System.out.println("Update failed. Please try again.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private double readWeight() {
        while (true) {
            System.out.print("Enter weight in kg (0 to cancel): ");
            String input = scanner.nextLine().trim();
            try {
                double weight = Double.parseDouble(input);
                if (weight >= 0) {
                    return weight;
                }
                System.out.println("Weight cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private Customer findCustomer(List<Customer> list, int id) {
        for (Customer c : list) {
            if (c.customerId() == id) {
                return c;
            }
        }
        return null;
    }

    private ServiceRate findService(List<ServiceRate> list, int id) {
        for (ServiceRate s : list) {
            if (s.serviceId() == id) {
                return s;
            }
        }
        return null;
    }

    private LaundryOrder findOrder(List<LaundryOrder> list, int id) {
        for (LaundryOrder o : list) {
            if (o.orderId() == id) {
                return o;
            }
        }
        return null;
    }

    private void printCustomerTable(List<Customer> customers) {
        String border = "+-------+----------------------+";
        String row = "| %-5s | %-20s |%n";

        System.out.println(border);
        System.out.printf(row, "ID", "CUSTOMER NAME");
        System.out.println(border);
        for (Customer c : customers) {
            System.out.printf(row, c.customerId(), c.fullName());
        }
        System.out.println(border);
    }

    private void printServiceTable(List<ServiceRate> services) {
        String border = "+-------+----------------------+------------+";
        String row = "| %-5s | %-20s | %-10s |%n";

        System.out.println(border);
        System.out.printf(row, "ID", "SERVICE", "RATE/KG");
        System.out.println(border);
        for (ServiceRate s : services) {
            System.out.printf(row, s.serviceId(), s.serviceName(), String.format("%.2f", s.ratePerKg()));
        }
        System.out.println(border);
    }

    private void printOrderTable(List<LaundryOrder> orders) {
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }

        String border = "+-------+----------------------+----------------------+--------+------------+------------+";
        String row = "| %-5s | %-20s | %-20s | %-6s | %-10s | %-10s |%n";

        System.out.println(border);
        System.out.printf(row, "ID", "CUSTOMER", "SERVICE", "KG", "TOTAL", "STATUS");
        System.out.println(border);
        for (LaundryOrder o : orders) {
            System.out.printf(row, o.orderId(), o.customerName(), o.serviceName(),
                    String.format("%.1f", o.weightKg()),
                    String.format("%.2f", o.totalPrice()), o.orderStatus());
        }
        System.out.println(border);
        System.out.println("Total: " + orders.size());
    }
}