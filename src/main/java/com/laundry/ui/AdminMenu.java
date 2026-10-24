package com.laundry.ui;

import com.laundry.dao.UserDao;
import com.laundry.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final User user;
    private final UserDao userDao;
    private final Scanner scanner;

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
            System.out.println("4. Manage service rates (coming soon)");
            System.out.println("5. View activity logs (coming soon)");
            System.out.println("6. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                viewAllUsers();
            } else if (choice.equals("2")) {
                viewPendingUsers();
            } else if (choice.equals("3")) {
                approveUser();
            } else if (choice.equals("4") || choice.equals("5")) {
                System.out.println("This feature will be built next.");
            } else if (choice.equals("6")) {
                System.out.println("You have been logged out.");
                loggedIn = false;   // back to the main menu
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    // ---------- menu actions ----------

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

            // make sure the ID is really in the pending list
            User target = findById(pending, id);
            if (target == null) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(id)) {
                System.out.println("SUCCESS: " + target.username() + " (" + target.role()
                        + ") has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    // ---------- helpers ----------

    private User findById(List<User> users, int id) {
        for (User u : users) {
            if (u.userId() == id) {
                return u;
            }
        }
        return null;
    }

    // Prints any list of users as an aligned table with borders.
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
}