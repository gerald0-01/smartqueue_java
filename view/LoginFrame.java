package view;

import controllers.AuthController;
import models.*;
import javax.swing.*;
import java.awt.*;

// Login screen with maroon and gold theme
public class LoginFrame extends JFrame {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    private JTextField idField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("SmartQueue - Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MAROON);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(MAROON);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // title
        JLabel title = new JLabel("SmartQueue Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(GOLD);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        // id number
        c.gridwidth = 1; c.gridy = 1;
        JLabel idLabel = new JLabel("ID Number:");
        idLabel.setForeground(GOLD);
        panel.add(idLabel, c);
        c.gridx = 1;
        idField = new JTextField(15);
        panel.add(idField, c);

        // password
        c.gridx = 0; c.gridy = 2;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(GOLD);
        panel.add(passLabel, c);
        c.gridx = 1;
        passField = new JPasswordField(15);
        panel.add(passField, c);

        // login button
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(GOLD);
        loginBtn.setForeground(MAROON);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn, c);

        // register button
        c.gridy = 4;
        JButton regBtn = new JButton("Register (Students)");
        regBtn.setBackground(GOLD);
        regBtn.setForeground(MAROON);
        regBtn.setFocusPainted(false);
        regBtn.addActionListener(e -> openRegister());
        panel.add(regBtn, c);

        add(panel);
    }

    private void handleLogin() {
        String id = idField.getText().trim();
        String pw = new String(passField.getPassword());
        User u = AuthController.login(id, pw);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
        if (u instanceof Admin) {
            new AdminFrame((Admin) u).setVisible(true);
        } else if (u instanceof Staff) {
            new StaffFrame((Staff) u).setVisible(true);
        } else if (u instanceof Student) {
            new StudentFrame((Student) u).setVisible(true);
        }
    }

    private void openRegister() {
        new RegisterDialog(this).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
