package view;

import controllers.AdminController;
import controllers.AuthController;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.*;
import store.DataStore;

// Admin dashboard: manage users and view activity log
public class AdminFrame extends JFrame {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    private final Admin admin;
    private DefaultTableModel userModel;
    private DefaultTableModel logModel;

    public AdminFrame(Admin admin) {
        this.admin = admin;
        setTitle("SmartQueue - Admin: " + admin.getName());
        setSize(900, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MAROON);

        // top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAROON);
        JLabel welcome = new JLabel("Admin: " + admin.getName(), SwingConstants.LEFT);
        welcome.setForeground(GOLD);
        welcome.setFont(new Font("Arial", Font.BOLD, 14));
        welcome.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(MAROON);
        tabs.setForeground(GOLD);
        tabs.addTab("Users", buildUsersPanel());
        tabs.addTab("Activity Log", buildLogPanel());

        setLayout(new BorderLayout(5, 5));
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refreshUsers();
        refreshLog();
    }

    private JPanel buildUsersPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(MAROON);

        // user table
        String[] cols = {"ID Number", "Name", "Role", "Email"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(userModel);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        // action panel
        JPanel actions = new JPanel(new GridLayout(3, 1, 5, 5));
        actions.setBackground(MAROON);
        actions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create staff
        JPanel createRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        createRow.setBackground(MAROON);
        JTextField nameF = new JTextField(10); JTextField emailF = new JTextField(10);
        JTextField idF   = new JTextField(8);  JTextField deptF  = new JTextField(10);
        JPasswordField pwF = new JPasswordField(8);
        JButton createBtn = new JButton("Create Staff");
        styleButton(createBtn);
        createRow.add(label("Name:")); createRow.add(nameF);
        createRow.add(label("Email:")); createRow.add(emailF);
        createRow.add(label("ID:")); createRow.add(idF);
        createRow.add(label("Dept:")); createRow.add(deptF);
        createRow.add(label("Pass:")); createRow.add(pwF);
        createRow.add(createBtn);
        createBtn.addActionListener(e -> {
            AdminController.createStaff(admin, nameF.getText().trim(),
                new String(pwF.getPassword()), emailF.getText().trim(),
                idF.getText().trim(), deptF.getText().trim());
            nameF.setText(""); emailF.setText(""); idF.setText("");
            deptF.setText(""); pwF.setText("");
            refreshUsers(); refreshLog();
        });

        // delete user
        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        deleteRow.setBackground(MAROON);
        JTextField delIdF = new JTextField(12);
        JButton delBtn = new JButton("Delete User");
        styleButton(delBtn);
        deleteRow.add(label("ID to delete:")); deleteRow.add(delIdF); deleteRow.add(delBtn);
        delBtn.addActionListener(e -> {
            boolean ok = AdminController.deleteUser(admin, delIdF.getText().trim());
            JOptionPane.showMessageDialog(this, ok ? "User deleted." : "User not found.");
            delIdF.setText("");
            refreshUsers(); refreshLog();
        });

        // reset password
        JPanel resetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        resetRow.setBackground(MAROON);
        JTextField resetIdF = new JTextField(12);
        JPasswordField newPwF = new JPasswordField(10);
        JButton resetBtn = new JButton("Reset Password");
        styleButton(resetBtn);
        resetRow.add(label("ID:")); resetRow.add(resetIdF);
        resetRow.add(label("New Password:")); resetRow.add(newPwF); resetRow.add(resetBtn);
        resetBtn.addActionListener(e -> {
            boolean ok = AdminController.resetPassword(admin, resetIdF.getText().trim(),
                                                       new String(newPwF.getPassword()));
            JOptionPane.showMessageDialog(this, ok ? "Password reset." : "User not found.");
            resetIdF.setText(""); newPwF.setText("");
            refreshLog();
        });

        actions.add(createRow);
        actions.add(deleteRow);
        actions.add(resetRow);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildLogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(MAROON);
        String[] cols = {"Timestamp", "Actor", "Role", "Action", "Detail"};
        logModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(logModel);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshUsers() {
        userModel.setRowCount(0);
        for (User u : DataStore.users.values()) {
            userModel.addRow(new Object[]{
                u.getIdNumber(), u.getName(), u.getClass().getSimpleName(), u.getEmail()
            });
        }
    }

    private void refreshLog() {
        logModel.setRowCount(0);
        for (ActivityLog log : DataStore.logs) {
            logModel.addRow(new Object[]{
                log.getFormattedTimestamp(), log.getActorName(),
                log.getActorRole(), log.getAction().name(), log.getDetail()
            });
        }
    }

    private void logout() {
        AuthController.logout(admin);
        dispose();
        new LoginFrame().setVisible(true);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(GOLD);
        return l;
    }

    private void styleButton(JButton btn) {
        btn.setBackground(GOLD);
        btn.setForeground(MAROON);
        btn.setFocusPainted(false);
    }

    private void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setBackground(GOLD);
        table.getTableHeader().setForeground(MAROON);
    }
}
