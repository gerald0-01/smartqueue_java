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

        // request table
        String[] cols = {"Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"};
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

        // submit form
        // FlowLayout lines up components left-to-right and wraps when the row is full
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.setBackground(MAROON);

        // document type dropdown — "Other" is the last option
        String[] docTypes = {
            "Transcript of Records",
            "Certificate of Enrollment",
            "Certificate of Graduation",
            "Good Moral Certificate",
            "Other"
        };
        JComboBox<String> docBox = new JComboBox<>(docTypes);

        // the "Other" label and text field start hidden
        // setVisible(false) removes them from the layout visually but they still exist in memory
        JLabel otherLabel = label("Document Name:");
        JTextField otherField = new JTextField(15);
        otherLabel.setVisible(false);
        otherField.setVisible(false);

        // ActionListener fires every time the user picks a different item in the dropdown
        docBox.addActionListener(e -> {
            // getSelectedItem() returns the currently chosen string
            boolean isOther = "Other".equals(docBox.getSelectedItem());

            // show or hide the extra field depending on whether "Other" is selected
            otherLabel.setVisible(isOther);
            otherField.setVisible(isOther);

            // revalidate() tells the layout manager to recalculate positions
            // repaint() tells Swing to redraw the panel — both are needed after visibility changes
            form.revalidate();
            form.repaint();
        });

        JTextField reasonField = new JTextField(20);
        JButton submitBtn = new JButton("Submit Request");
        styleButton(submitBtn);

        form.add(label("Document:"));
        form.add(docBox);
        form.add(otherLabel);   // hidden until "Other" is selected
        form.add(otherField);   // hidden until "Other" is selected
        form.add(label("Reason:"));
        form.add(reasonField);
        form.add(submitBtn);

        submitBtn.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a reason.");
                return;
            }

            // decide the final document type name
            String selected = (String) docBox.getSelectedItem();
            String docType;
            if ("Other".equals(selected)) {
                // use whatever the user typed in the custom field
                docType = otherField.getText().trim();
                if (docType.isEmpty()) {
                    // don't allow submitting "Other" with no name
                    JOptionPane.showMessageDialog(this, "Please enter the document name.");
                    return;
                }
            } else {
                // use the dropdown selection directly
                docType = selected;
            }

            RequestController.submit(student, docType, reason);
            reasonField.setText("");
            otherField.setText("");

            // reset dropdown back to first item after submit
            docBox.setSelectedIndex(0);

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
                r.getDocumentType(),
                r.getStatus(),
                r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
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
