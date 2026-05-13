package view;

import controllers.AuthController;
import java.awt.*;
import javax.swing.*;

// Student self-registration dialog
public class RegisterDialog extends JDialog {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    // the seven colleges offered as choices — stored as a constant array so it's easy to update
    private static final String[] COLLEGES = {
        "College of Computer Sciences",
        "College of Engineering",
        "College of Science and Mathematics",
        "College of Arts and Social Sciences",
        "College of Economics, Business and Administration",
        "College of Health Sciences",
        "College of Education"
    };

    public RegisterDialog(JFrame parent) {
        super(parent, "Register", true);
        setSize(420, 380);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(MAROON);

        // GridLayout(rows, cols, hgap, vgap)
        // 9 rows: name, email, id, password, year, course, college, spacer, button
        JPanel p = new JPanel(new GridLayout(9, 2, 5, 5));
        p.setBackground(MAROON);
        p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // plain text fields
        JTextField    nameF  = addRow(p, "Full Name:");
        JTextField    emailF = addRow(p, "Email:");
        JTextField    idF    = addRow(p, "ID Number:");
        JPasswordField pwF   = new JPasswordField();
        addLabeledField(p, "Password:", pwF);
        JTextField    yearF   = addRow(p, "Year Level:");
        JTextField    courseF = addRow(p, "Course:");

        // college is now a JComboBox instead of a free-text field
        // JComboBox<String>(array) builds a dropdown from the array items
        JComboBox<String> collegeBox = new JComboBox<>(COLLEGES);
        collegeBox.setBackground(Color.WHITE);  // white background so it's readable
        collegeBox.setForeground(Color.BLACK);
        addLabeledField(p, "College:", collegeBox);

        // register button
        JButton btn = new JButton("Register");
        btn.setBackground(GOLD);
        btn.setForeground(MAROON);
        btn.setFocusPainted(false);
        p.add(new JLabel()); // empty label acts as a spacer in the left column
        p.add(btn);

        btn.addActionListener(e -> {
            try {
                short year = Short.parseShort(yearF.getText().trim());

                // getSelectedItem() returns Object, so we cast to String
                String selectedCollege = (String) collegeBox.getSelectedItem();

                boolean ok = AuthController.register(
                    nameF.getText().trim(),
                    new String(pwF.getPassword()),
                    emailF.getText().trim(),
                    idF.getText().trim(),
                    year,
                    courseF.getText().trim(),
                    selectedCollege   // pass the chosen college name
                );
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registered successfully!");
                    dispose(); // close the dialog on success
                } else {
                    JOptionPane.showMessageDialog(this, "ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(p);
    }

    // helper: creates a JTextField, adds the label+field pair to the panel, returns the field
    private JTextField addRow(JPanel p, String label) {
        JTextField f = new JTextField();
        addLabeledField(p, label, f);
        return f;
    }

    // helper: adds a gold label and any JComponent as a row in the grid
    private void addLabeledField(JPanel p, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(GOLD);
        p.add(lbl);
        p.add(field);
    }
}
