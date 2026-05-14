package view;

import controllers.AuthController;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

// Registration — 1280x720, same split layout as LoginFrame, form centered with GridBagLayout
public class RegisterDialog extends JDialog {

    private static final String[] COLLEGES = {
        "College of Computer Studies",
        "College of Engineering",
        "College of Science and Mathematics",
        "College of Arts and Social Sciences",
        "College of Economics, Business and Accountancy",
        "College of Health Sciences",
        "College of Education"
    };

    public RegisterDialog(JFrame parent) {
        super(parent, "IITraQ — Student Registration", true);
        setSize(1280, 720);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout());
        JPanel brand = buildBrandPanel();
        brand.setPreferredSize(new Dimension(560, 720));
        root.add(brand,          BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildBrandPanel() {
        JPanel brand = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Path2D wave = new Path2D.Float();
                wave.moveTo(0, h - 80);
                wave.curveTo(w * 0.3, h - 20, w * 0.6, h - 120, w, h - 50);
                wave.lineTo(w, h); wave.lineTo(0, h); wave.closePath();
                g2.setColor(Theme.GOLD); g2.fill(wave); g2.dispose();
            }
        };
        brand.setBackground(Theme.MAROON_DARK);
        brand.setBorder(BorderFactory.createEmptyBorder(48, 44, 100, 44));

        JPanel top = new JPanel(); top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel(AppIcon.get(80, 80)); logo.setAlignmentX(0f);
        JLabel name = new JLabel("IITraQ"); name.setFont(new Font("Segoe UI", Font.BOLD, 36));
        name.setForeground(Color.WHITE); name.setAlignmentX(0f);
        JLabel sub = new JLabel("SMART QUEUE & DOCUMENT TRACKING SYSTEM");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10)); sub.setForeground(new Color(210, 185, 185));
        sub.setAlignmentX(0f);
        JSeparator sep = new JSeparator(); sep.setForeground(Theme.GOLD);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        top.add(logo); top.add(Box.createVerticalStrut(12));
        top.add(name); top.add(Box.createVerticalStrut(4));
        top.add(sub);  top.add(Box.createVerticalStrut(16)); top.add(sep);

        JPanel mid = new JPanel(); mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));
        JLabel h1 = new JLabel("Create Your Account.");
        h1.setFont(new Font("Segoe UI", Font.BOLD, 24)); h1.setForeground(Color.WHITE); h1.setAlignmentX(0f);
        JLabel h2 = new JLabel("<html><font color='#FFD700'>Join IITraQ Today.</font></html>");
        h2.setFont(new Font("Segoe UI", Font.BOLD, 24)); h2.setAlignmentX(0f);
        JLabel desc = new JLabel(
            "<html><body style='width:260px;color:#DDBBBB;font-size:12px;line-height:1.5'>" +
            "Register to start submitting document requests, track their status, " +
            "and manage your academic needs — all in one place.</body></html>");
        desc.setAlignmentX(0f);
        mid.add(h1); mid.add(h2); mid.add(Box.createVerticalStrut(14)); mid.add(desc);

        brand.add(top, BorderLayout.NORTH);
        brand.add(mid, BorderLayout.CENTER);
        return brand;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        // no setPreferredSize — let GridBagLayout size naturally from children

        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0; r.fill = GridBagConstraints.NONE; r.weightx = 0;
        r.anchor = GridBagConstraints.CENTER;

        // header
        JLabel uLogo = new JLabel(AppIcon.get(56, 56), SwingConstants.CENTER);
        uLogo.setPreferredSize(new Dimension(420, 62));
        r.gridy = 0; r.insets = new Insets(0, 0, 4, 0); form.add(uLogo, r);

        JLabel uName = new JLabel("Mindanao State University", SwingConstants.CENTER);
        uName.setFont(new Font("Segoe UI", Font.BOLD, 14)); uName.setForeground(Theme.MAROON);
        uName.setPreferredSize(new Dimension(420, 22));
        r.gridy = 1; r.insets = new Insets(0, 0, 6, 0); form.add(uName, r);

        JPanel accentWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        accentWrap.setBackground(Color.WHITE);
        JPanel accent = new JPanel(); accent.setBackground(Theme.GOLD);
        accent.setPreferredSize(new Dimension(44, 2)); accentWrap.add(accent);
        r.gridy = 2; r.insets = new Insets(0, 0, 14, 0); form.add(accentWrap, r);

        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22)); heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setPreferredSize(new Dimension(420, 30));
        r.gridy = 3; r.insets = new Insets(0, 0, 2, 0); form.add(heading, r);

        JLabel sub2 = new JLabel("Fill in your details to register as a student.");
        sub2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); sub2.setForeground(Theme.TEXT_SECONDARY);
        sub2.setPreferredSize(new Dimension(420, 18));
        r.gridy = 4; r.insets = new Insets(0, 0, 14, 0); form.add(sub2, r);

        // 2-column grid for fields
        JPanel grid = new JPanel(new GridLayout(7, 2, 10, 8));
        grid.setBackground(Color.WHITE);
        grid.setPreferredSize(new Dimension(420, 280));

        JTextField nameF = inp(); JTextField emailF = inp();
        JTextField idF   = inp(); JPasswordField pwF = new JPasswordField(); styleInp(pwF);
        // year level dropdown 1-7
        JComboBox<String> yearBox = new JComboBox<>(new String[]{"1","2","3","4","5","6","7"});
        yearBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField courseF = inp();
        JComboBox<String> collegeBox = new JComboBox<>(COLLEGES);
        collegeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // password row with show toggle
        JPanel pwRow = new JPanel(new BorderLayout(4, 0));
        pwRow.setBackground(Color.WHITE);
        JCheckBox showPw = new JCheckBox("Show");
        showPw.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPw.setForeground(Theme.TEXT_SECONDARY);
        showPw.setBackground(Color.WHITE);
        showPw.setFocusPainted(false);
        showPw.addActionListener(e -> pwF.setEchoChar(showPw.isSelected() ? (char) 0 : '\u2022'));
        pwRow.add(pwF, BorderLayout.CENTER);
        pwRow.add(showPw, BorderLayout.EAST);

        grid.add(lbl("Full Name:"));  grid.add(nameF);
        grid.add(lbl("Email:"));      grid.add(emailF);
        grid.add(lbl("ID Number:"));  grid.add(idF);
        grid.add(lbl("Password:"));   grid.add(pwRow);
        grid.add(lbl("Year Level:")); grid.add(yearBox);
        grid.add(lbl("Course:"));     grid.add(courseF);
        grid.add(lbl("College:"));    grid.add(collegeBox);

        r.gridy = 5; r.insets = new Insets(0, 0, 16, 0); form.add(grid, r);

        // Create Account button
        JButton createBtn = new JButton("  Create Account");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createBtn.setBackground(Theme.MAROON); createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false); createBtn.setBorderPainted(false); createBtn.setOpaque(true);
        createBtn.setPreferredSize(new Dimension(420, 46));
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        r.gridy = 6; r.insets = new Insets(0, 0, 10, 0); form.add(createBtn, r);

        // Back button
        JButton backBtn = new JButton("  Back to Login");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setBackground(Color.WHITE); backBtn.setForeground(Theme.MAROON);
        backBtn.setFocusPainted(false); backBtn.setOpaque(true);
        backBtn.setPreferredSize(new Dimension(420, 46));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.MAROON, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        backBtn.addActionListener(e -> dispose());
        r.gridy = 7; r.insets = new Insets(0, 0, 0, 0); form.add(backBtn, r);

        createBtn.addActionListener(e -> {
            try {
                short year = Short.parseShort((String) yearBox.getSelectedItem());
                boolean ok = AuthController.register(
                    nameF.getText().trim(), new String(pwF.getPassword()),
                    emailF.getText().trim(), idF.getText().trim(),
                    year, courseF.getText().trim(),
                    (String) collegeBox.getSelectedItem());
                if (ok) { JOptionPane.showMessageDialog(this, "Account created successfully!"); dispose(); }
                else      JOptionPane.showMessageDialog(this, "ID Number already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year Level must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        outer.add(form);
        return outer;
    }

    private JTextField inp() { JTextField f = new JTextField(); styleInp(f); return f; }

    private void styleInp(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }
}
