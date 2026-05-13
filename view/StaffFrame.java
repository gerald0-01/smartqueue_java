package view;

import controllers.AuthController;
import controllers.RequestController;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.*;

// Staff dashboard: view all requests and update their status
public class StaffFrame extends JFrame {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    private final Staff staff;
    private DefaultTableModel tableModel;
    private List<UUID> rowIds = new ArrayList<>(); // tracks which UUID each row maps to

    public StaffFrame(Staff staff) {
        this.staff = staff;
        setTitle("SmartQueue - Staff: " + staff.getName());
        setSize(850, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MAROON);

        // top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAROON);
        JLabel welcome = new JLabel("Staff: " + staff.getName() + " | " + staff.getDepartment(), SwingConstants.LEFT);
        welcome.setForeground(GOLD);
        welcome.setFont(new Font("Arial", Font.BOLD, 14));
        welcome.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // request table
        String[] cols = {"#", "Document Type", "Status", "Reason", "Student ID", "Submitted"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setBackground(GOLD);
        table.getTableHeader().setForeground(MAROON);
        JScrollPane scroll = new JScrollPane(table);

        // edit panel
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.setBackground(MAROON);
        String[] statuses = {"Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        JTextField msgField   = new JTextField(18);
        JTextField pickUpField = new JTextField(16); // format: yyyy-MM-ddTHH:mm
        JButton updateBtn = new JButton("Update Selected");
        styleButton(updateBtn);

        form.add(label("Status:"));
        form.add(statusBox);
        form.add(label("Message:"));
        form.add(msgField);
        form.add(label("Pick-Up (yyyy-MM-ddTHH:mm):"));
        form.add(pickUpField);
        form.add(updateBtn);

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
            UUID id = rowIds.get(row);
            LocalDateTime pickUp = null;
            String pickUpText = pickUpField.getText().trim();
            if (!pickUpText.isEmpty()) {
                try { pickUp = LocalDateTime.parse(pickUpText); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-ddTHH:mm");
                    return;
                }
            }
            RequestController.update(staff, id, (String) statusBox.getSelectedItem(),
                                     msgField.getText().trim(), pickUp);
            msgField.setText("");
            pickUpField.setText("");
            refreshTable();
        });

        setLayout(new BorderLayout(5, 5));
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        rowIds.clear();
        int i = 1;
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(staff).entrySet()) {
            Request r = entry.getValue();
            tableModel.addRow(new Object[]{
                i++, r.getDocumentType(), r.getStatus(),
                r.getReason(), r.getStudentId().toString().substring(0, 8),
                r.getCreatedAt().toString()
            });
            rowIds.add(entry.getKey());
        }
    }

    private void logout() {
        AuthController.logout(staff);
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
}
