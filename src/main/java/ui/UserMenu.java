package ui;

import model.User;

import java.util.Scanner;

// Menu shown after a successful login.
public class UserMenu {

    private final User user;
    private final Scanner sc;

    public UserMenu(User user, Scanner sc) {
        this.user = user;
        this.sc = sc;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== USER MENU =====");
            System.out.println("Welcome, " + user.username() + "!");
            System.out.println("Role:   " + user.roles());
            System.out.println("Status: " + user.status());
            System.out.println("1. Logout (back to main menu)");
            System.out.println("2. Exit app");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.println("You have been logged out.");
                loggedIn = false;          // returns to the main menu
            } else if (choice.equals("2")) {
                System.out.println("Goodbye!");
                System.exit(0);            // closes the whole program
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
