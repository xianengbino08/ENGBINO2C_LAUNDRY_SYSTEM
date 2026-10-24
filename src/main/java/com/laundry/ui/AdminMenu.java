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
            System.out.println("Welcome, " + user.fullName() + "!  (Status: " + user.status() + ")");
            System.out.println("1. View all users");
            System.out.println("2. Manage service rates (coming soon)");
            System.out.println("3. View activity logs (coming soon)");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                viewAllUsers();
            } else if (choice.equals("2") || choice.equals("3")) {
                System.out.println("This feature will be built next.");
            } else if (choice.equals("4")) {
                System.out.println("You have been logged out.");
                loggedIn = false;   // back to the main menu
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.findAll();

        System.out.println("\n--- ALL USERS ---");
        System.out.printf("%-5s %-15s %-20s %-10s %-8s%n", "ID", "USERNAME", "FULL NAME", "ROLE", "STATUS");
        for (User u : users) {
            System.out.printf("%-5d %-15s %-20s %-10s %-8s%n",
                    u.userId(), u.username(), u.fullName(), u.role(), u.status());
        }
    }
}
