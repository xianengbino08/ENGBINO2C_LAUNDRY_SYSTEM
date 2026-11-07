package com.laundry.ui;

import com.laundry.dao.ActivityLogDao;
import com.laundry.dao.UserDao;
import com.laundry.model.ActivityLog;
import com.laundry.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final User user;
    private final UserDao userDao;
    private final Scanner scanner;
    private final ActivityLogDao logDao = new ActivityLogDao();

    public AdminMenu(User user, UserDao userDao, Scanner scanner) {
        this.user = user;
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("Welcome, " + user.fullName() + "!");
            System.out.println("1. View all system users");
            System.out.println("2. View pending registrations");
            System.out.println("3. Approve user account");
            System.out.println("4. Update user role and status");
            System.out.println("5. Manage service rates (coming soon)");
            System.out.println("6. View system audit logs");
            System.out.println("7. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                viewAllUsers();
            } else if (choice.equals("2")) {
                viewPendingUsers();
            } else if (choice.equals("3")) {
                approveUser();
            } else if (choice.equals("4")) {
                updateUser();
            } else if (choice.equals("5")) {
                System.out.println("This feature will be built next.");
            } else if (choice.equals("6")) {
                viewAuditLogs();
            } else if (choice.equals("7")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        printUserTable(userDao.getPendingUsers());
    }

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();

        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        if (pending.isEmpty()) {
            System.out.println("No pending registrations to approve.");
            return;
        }
        printUserTable(pending);

        while (true) {
            System.out.print("Enter the User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();

            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
                continue;
            }

            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = findById(pending, id);
            if (target == null) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(user.userId(), id)) {
                System.out.println("SUCCESS: " + target.username() + " (" + target.role()
                        + ") has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    private void updateUser() {
        System.out.println("\n--- UPDATE USER ROLE AND STATUS ---");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);

        User target;
        while (true) {
            int targetUserId = readIntInput("Enter the User ID to update (0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (targetUserId == user.userId()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                return;
            }
            target = findById(users, targetUserId);
            if (target == null) {
                System.out.println("User ID " + targetUserId + " does not exist. Try again.");
                continue;
            }
            break;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(user.userId(), target.userId(), newRole, newStatus)) {
            System.out.println("SUCCESS: " + target.username() + " is now " + newRole
                    + " | " + newStatus + ".");
        } else {
            System.out.println("Update failed. Please try again.");
        }
    }

    private void viewAuditLogs() {
        while (true) {
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("1. View all system logs");
            System.out.println("2. Filter logs by User ID");
            System.out.println("3. Back");
            int choice = readIntInput("Choose: ");

            if (choice == 1) {
                printLogTable(logDao.getAllLogs());
            } else if (choice == 2) {
                int id = readIntInput("Enter the User ID to filter (0 to cancel): ");
                if (id != 0) {
                    printLogTable(logDao.getLogsByUserId(id));
                }
            } else if (choice == 3) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private int readIntInput(String prompt) {
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

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (ADMIN / STAFF / CUSTOMER, 0 to cancel): ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return null;
            }
            if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("CUSTOMER")) {
                return role;
            }
            System.out.println("Invalid role. Choose ADMIN, STAFF, or CUSTOMER.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED / PENDING, 0 to cancel): ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status. Choose APPROVED or PENDING.");
        }
    }

    private User findById(List<User> users, int id) {
        for (User u : users) {
            if (u.userId() == id) {
                return u;
            }
        }
        return null;
    }

    private String cut(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    private void printUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        String border = "+-------+-----------------+--------------+------------+";
        String row = "| %-5s | %-15s | %-12s | %-10s |%n";

        System.out.println(border);
        System.out.printf(row, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(row, u.userId(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
        System.out.println("Total: " + users.size());
    }

    private void printLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No logs found.");
            return;
        }

        String border = "+" + "-".repeat(7) + "+" + "-".repeat(10) + "+" + "-".repeat(22)
                + "+" + "-".repeat(37) + "+" + "-".repeat(22) + "+";
        String row = "| %-5s | %-8s | %-20s | %-35s | %-20s |%n";

        System.out.println(border);
        System.out.printf(row, "ID", "USER ID", "ACTION", "DESCRIPTION", "TIMESTAMP");
        System.out.println(border);
        for (ActivityLog log : logs) {
            System.out.printf(row, log.id(), log.userId(), log.actionType(),
                    cut(log.description(), 35), log.timestamp());
        }
        System.out.println(border);
        System.out.println("Total: " + logs.size());
    }
}