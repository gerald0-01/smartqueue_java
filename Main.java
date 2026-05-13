import models.Admin;
import store.DataStore;
import view.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // seed a default admin account so the app is usable on first run
        Admin defaultAdmin = new Admin("System Admin", "admin123", "admin@school.edu", "ADMIN001", "System");
        DataStore.users.put("ADMIN001", defaultAdmin);

        // launch the login screen on the Swing event thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
