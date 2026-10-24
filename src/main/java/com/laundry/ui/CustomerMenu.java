package com.laundry.ui;

import com.laundry.dao.UserDao;
import com.laundry.model.User;

import java.util.Scanner;

public class CustomerMenu {

    private final User user;
    private final UserDao userDao;   // shared; will be used when customer features are added
    private final Scanner scanner;

    public CustomerMenu(User user, UserDao userDao, Scanner scanner) {
        this.user = user;
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("Welcome, " + user.fullName() + "!  (Status: " + user.status() + ")");
            System.out.println("1. View my orders (coming soon)");
            System.out.println("2. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.println("This feature will be built next.");
            } else if (choice.equals("2")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
