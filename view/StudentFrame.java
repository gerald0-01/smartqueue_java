package view;

import controllers.AuthController;
import controllers.RequestController;
import models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.UUID;

// Student dashboard: submit requests and view own request history
public class StudentFrame extends JFrame {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    private final Student student;
    private DefaultTableModel tableModel;

    public StudentFrame(Student student) {
        this.student = student;
        setTitle("SmartQueue - Student: " + student.getName());
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MAROON);

        // top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAROON);
        JLabel welcome = new JLabel("Welcome, " + student.getName(), SwingConstants.LEFT);
        welcome.setForeground(GOLD);
        welcome.setFont(new Font("Arial", Font.BOLD, 14));
        welcome.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // request table
        String[] cols = {"Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setBackground(GOLD);
        table.getTableHeader().setForeground(MAROON);
        JScrollPane scroll = new JScrollPane(table);

        // submit panel
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.setBackground(MAROON);
        String[] docTypes = {"Transcript of Records", "Certificate of Enrollment",
                             "Certificate of Graduation", "Good Moral Certificate", "Other"};
        JComboBox<String> docBox = new JComboBox<>(docTypes);
        JTextField reasonField = new JTextField(20);
        JButton submitBtn = new JButton("Submit Request");
        styleButton(submitBtn);

        form.add(label("Document:"));
        form.add(docBox);
        form.add(label("Reason:"));
        form.add(reasonField);
        form.add(submitBtn);

        submitBtn.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a reason.");
                return;
            }
            RequestController.submit(student, (String) docBox.getSelectedItem(), reason);
            reasonField.setText("");
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
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(student).entrySet()) {
            Request r = entry.getValue();
            tableModel.addRow(new Object[]{
                r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage() != null ? r.getMessage() : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                r.getCreatedAt().toString()
            });
        }
    }

    private void logout() {
        AuthController.logout(student);
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
