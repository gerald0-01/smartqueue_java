package view;

import controllers.AuthController;
import javax.swing.*;
import java.awt.*;

// Student self-registration dialog
public class RegisterDialog extends JDialog {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    public RegisterDialog(JFrame parent) {
        super(parent, "Register", true);
        setSize(400, 380);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(MAROON);

        JPanel p = new JPanel(new GridLayout(9, 2, 5, 5));
        p.setBackground(MAROON);
        p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // fields
        JTextField nameF    = addRow(p, "Full Name:");
        JTextField emailF   = addRow(p, "Email:");
        JTextField idF      = addRow(p, "ID Number:");
        JPasswordField pwF  = new JPasswordField();
        addLabeledField(p, "Password:", pwF);
        JTextField yearF    = addRow(p, "Year Level:");
        JTextField courseF  = addRow(p, "Course:");
        JTextField collegeF = addRow(p, "College:");

        // register button
        JButton btn = new JButton("Register");
        btn.setBackground(GOLD);
        btn.setForeground(MAROON);
        btn.setFocusPainted(false);
        p.add(new JLabel()); // spacer
        p.add(btn);

        btn.addActionListener(e -> {
            try {
                short year = Short.parseShort(yearF.getText().trim());
                boolean ok = AuthController.register(
                    nameF.getText().trim(),
                    new String(pwF.getPassword()),
                    emailF.getText().trim(),
                    idF.getText().trim(),
                    year,
                    courseF.getText().trim(),
                    collegeF.getText().trim()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registered successfully!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(p);
    }

    // helper to add a labeled text field row
    private JTextField addRow(JPanel p, String label) {
        JTextField f = new JTextField();
        addLabeledField(p, label, f);
        return f;
    }

    private void addLabeledField(JPanel p, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(GOLD);
        p.add(lbl);
        p.add(field);
    }
}
