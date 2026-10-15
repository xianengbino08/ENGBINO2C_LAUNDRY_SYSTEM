import dao.DatabaseConnection;
import ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.createTable();   // makes the users table if it does not exist yet
        new MainMenu().show();
    }
}
