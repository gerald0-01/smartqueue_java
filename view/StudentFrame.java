package view;

import controllers.AuthController;
import controllers.RequestController;
import models.*;
import store.DataStore;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.UUID;

// Student dashboard: submit requests, view active requests, view completed requests
public class StudentFrame extends JFrame {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    private final Student student;
    private DefaultTableModel activeModel;
    private DefaultTableModel completedModel;

    public StudentFrame(Student student) {
        this.student = student;
        setTitle("SmartQueue - Student: " + student.getName());
        setSize(1280, 720);
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

        // tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(MAROON);
        tabs.setForeground(GOLD);
        tabs.addTab("My Requests",  buildActiveTab());
        tabs.addTab("Completed",    buildCompletedTab());

        setLayout(new BorderLayout(5, 5));
        add(top,  BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refreshActive();
        refreshCompleted();
    }

    // active requests tab: submit form + table of non-completed requests
    private JPanel buildActiveTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(MAROON);

        String[] cols = {"Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"};
        activeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(activeModel);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        // submit form
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.setBackground(MAROON);

        String[] docTypes = {
            "Transcript of Records",
            "Certificate of Enrollment",
            "Certificate of Graduation",
            "Good Moral Certificate",
            "Other"
        };
        JComboBox<String> docBox = new JComboBox<>(docTypes);

        // "Other" label and field — hidden until "Other" is selected
        JLabel    otherLabel = label("Document Name:");
        JTextField otherField = new JTextField(15);
        otherLabel.setVisible(false);
        otherField.setVisible(false);

        docBox.addActionListener(e -> {
            boolean isOther = "Other".equals(docBox.getSelectedItem());
            otherLabel.setVisible(isOther);
            otherField.setVisible(isOther);
            form.revalidate();
            form.repaint();
        });

        JTextField reasonField = new JTextField(20);
        JButton submitBtn = new JButton("Submit Request");
        styleButton(submitBtn);

        form.add(label("Document:")); form.add(docBox);
        form.add(otherLabel);         form.add(otherField);
        form.add(label("Reason:"));   form.add(reasonField);
        form.add(submitBtn);

        submitBtn.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a reason.");
                return;
            }
            String selected = (String) docBox.getSelectedItem();
            String docType;
            if ("Other".equals(selected)) {
                docType = otherField.getText().trim();
                if (docType.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the document name.");
                    return;
                }
            } else {
                docType = selected;
            }
            RequestController.submit(student, docType, reason);
            reasonField.setText("");
            otherField.setText("");
            docBox.setSelectedIndex(0);
            refreshActive();
        });

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    // completed tab: read-only view of this student's completed requests from the archive
    private JPanel buildCompletedTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(MAROON);

        String[] cols = {"Document Type", "Reason", "Message", "Pick-Up", "Completed At", "Submitted"};
        completedModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(completedModel);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // fills the active table with this student's non-completed requests
    private void refreshActive() {
        activeModel.setRowCount(0);
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(student).entrySet()) {
            Request r = entry.getValue();
            if ("Completed".equals(r.getStatus())) continue; // completed ones go to the other tab
            activeModel.addRow(new Object[]{
                r.getDocumentType(),
                r.getStatus(),
                r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                r.getCreatedAt().toString()
            });
        }
    }

    // fills the completed table from DataStore.completed filtered to this student
    private void refreshCompleted() {
        completedModel.setRowCount(0);
        for (Request r : DataStore.completed.values()) {
            // only show requests that belong to this student
            if (!r.getStudentId().equals(student.getId())) continue;
            completedModel.addRow(new Object[]{
                r.getDocumentType(),
                r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                r.getCompletedAt()    != null ? r.getCompletedAt().toString()    : "",
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
        JLabel l = new JLabel(text); l.setForeground(GOLD); return l;
    }

    private void styleButton(JButton btn) {
        btn.setBackground(GOLD); btn.setForeground(MAROON); btn.setFocusPainted(false);
    }

    private void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setBackground(GOLD);
        table.getTableHeader().setForeground(MAROON);
    }
}
