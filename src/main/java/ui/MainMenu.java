package ui;

import dao.UserDao;
import model.User;

import java.util.Scanner;

public class MainMenu {

    // The roles a user can pick when registering. Change these words if needed.
    private static final String[] ROLES = {"ADMIN", "STAFF", "CUSTOMER"};

    private final Scanner sc = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void show() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

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
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required.");
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
            pick = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            pick = 0;
        }
        if (pick < 1 || pick > ROLES.length) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        // new accounts always start as PENDING
        User user = new User(0, username, password, ROLES[pick - 1], "PENDING");

        if (userDao.register(user)) {
            System.out.println("Registered successfully! Status: PENDING");
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("\n*** LOGIN FAILED: Wrong username or password. ***");
            System.out.print("Press Enter to go back to the main menu...");
            sc.nextLine();   // waits so you can read the message
        } else {
            new UserMenu(user, sc).show();
        }
    }
}