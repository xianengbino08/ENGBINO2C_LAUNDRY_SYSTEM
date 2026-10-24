package com.laundry.ui;

import com.laundry.dao.UserDao;
import com.laundry.model.User;

import java.util.Scanner;

public class StaffMenu {

    private final User user;
    private final UserDao userDao;   // shared; will be used when staff features are added
    private final Scanner scanner;

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
            System.out.println("1. Create laundry order (coming soon)");
            System.out.println("2. View and update orders (coming soon)");
            System.out.println("3. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1") || choice.equals("2")) {
                System.out.println("This feature will be built next.");
            } else if (choice.equals("3")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
