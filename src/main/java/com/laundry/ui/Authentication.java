package com.laundry.ui;

import com.laundry.dao.ActivityLogDao;
import com.laundry.dao.UserDao;
import com.laundry.model.User;

import java.util.Scanner;

public class Authentication {

    private static final String[] ROLES = {"ADMIN", "STAFF", "CUSTOMER"};

    private final UserDao userDao;
    private final Scanner scanner;
    private final ActivityLogDao logDao = new ActivityLogDao();

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== LAUNDRY SHOP - MAIN MENU =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                register();
            } else if (choice.equals("2")) {
                login();
            } else if (choice.equals("3")) {
                System.out.println("Goodbye!");
                running = false;
            } else {
                System.out.println("Invalid choice. Please type 1, 2, or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Full name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required. Registration cancelled.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("That username is already taken.");
            return;
        }

        System.out.println("Choose a role:");
        for (int i = 0; i < ROLES.length; i++) {
            System.out.println((i + 1) + ". " + ROLES[i]);
        }
        System.out.print("Role number: ");

        int pick;
        try {
            pick = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            pick = 0;
        }
        if (pick < 1 || pick > ROLES.length) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        User newUser = new User(0, username, password, fullName, ROLES[pick - 1], "PENDING");

        if (userDao.register(newUser)) {
            System.out.println("Registered successfully! Role: " + ROLES[pick - 1] + " | Status: PENDING");
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("\n*** LOGIN FAILED: Wrong username or password. ***");
            System.out.print("Press Enter to go back to the main menu...");
            scanner.nextLine();
        } else {
            logDao.logAction(user.userId(), "USER_LOGIN",
                    "User " + user.username() + " (ID: " + user.userId() + ") logged in.");
            routeToMenu(user);
            logDao.logAction(user.userId(), "USER_LOGOUT",
                    "User " + user.username() + " (ID: " + user.userId() + ") logged out.");
        }
    }

    private void routeToMenu(User user) {
        switch (user.role()) {
            case "ADMIN"    -> new AdminMenu(user, userDao, scanner).show();
            case "STAFF"    -> new StaffMenu(user, userDao, scanner).show();
            case "CUSTOMER" -> new CustomerMenu(user, userDao, scanner).show();
            default         -> System.out.println("Unknown role: " + user.role());
        }
    }
}