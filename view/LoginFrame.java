package view;

import controllers.AuthController;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

// Login — 1280x720. Left panel fixed 560px, right panel fills the rest.
public class LoginFrame extends JFrame {

    private JTextField     idField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("IITraQ");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(AppIcon.getImage());

        // use BorderLayout on the content pane directly
        JPanel root = new JPanel(new BorderLayout());

        JPanel brand = buildBrandPanel();
        brand.setPreferredSize(new Dimension(560, 720)); // fixed left width

        JPanel form = buildFormPanel(); // fills remaining space

        root.add(brand, BorderLayout.WEST);
        root.add(form,  BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Left: maroon brand panel with gold wave ───────────────────────────────
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

        // logo + name side by side
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoRow.setOpaque(false);
        logoRow.add(new JLabel(AppIcon.get(72, 72)));
        JPanel nameStack = new JPanel();
        nameStack.setOpaque(false);
        nameStack.setLayout(new BoxLayout(nameStack, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("IITraQ");
        name.setFont(new Font("Segoe UI", Font.BOLD, 34));
        name.setForeground(Color.WHITE);
        JLabel sub = new JLabel("SMART QUEUE & DOCUMENT TRACKING SYSTEM");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        sub.setForeground(new Color(210, 185, 185));
        nameStack.add(Box.createVerticalStrut(14)); // vertically center within logo height
        nameStack.add(name);
        nameStack.add(Box.createVerticalStrut(4));
        nameStack.add(sub);
        logoRow.add(nameStack);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(logoRow);
        top.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.GOLD);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        top.add(sep);

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));
        JLabel h1 = new JLabel("Request. Queue. Track.");
        h1.setFont(new Font("Segoe UI", Font.BOLD, 24)); h1.setForeground(Color.WHITE); h1.setAlignmentX(0f);
        JLabel h2 = new JLabel("<html><font color='#FFD700'>All in One Smart System.</font></html>");
        h2.setFont(new Font("Segoe UI", Font.BOLD, 24)); h2.setAlignmentX(0f);
        JLabel desc = new JLabel(
            "<html><body style='width:280px;color:#DDBBBB;font-size:12px;line-height:1.5'>" +
            "IITraQ helps students request documents, get in queue, " +
            "and track the status of their requests in real-time.</body></html>");
        desc.setAlignmentX(0f);
        mid.add(h1); mid.add(h2); mid.add(Box.createVerticalStrut(14)); mid.add(desc);

        JPanel feats = new JPanel(new GridLayout(1, 3, 12, 0));
        feats.setOpaque(false);
        feats.add(feat("📄", "Request", "Submit document requests anytime, anywhere."));
        feats.add(feat("👥", "Queue",   "Get in line virtually and stay updated."));
        feats.add(feat("🔍", "Track",   "Monitor your request status in real-time."));

        brand.add(top,   BorderLayout.NORTH);
        brand.add(mid,   BorderLayout.CENTER);
        brand.add(feats, BorderLayout.SOUTH);
        return brand;
    }

    private JPanel feat(String icon, String title, String desc) {
        JPanel p = new JPanel(); p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel il = new JLabel(icon); il.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22)); il.setAlignmentX(0f);
        JLabel tl = new JLabel(title); tl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tl.setForeground(Theme.GOLD); tl.setAlignmentX(0f);
        JLabel dl = new JLabel("<html><body style='width:90px;color:#DDBBBB;font-size:10px'>" + desc + "</body></html>");
        dl.setAlignmentX(0f);
        p.add(il); p.add(Box.createVerticalStrut(4)); p.add(tl); p.add(Box.createVerticalStrut(2)); p.add(dl);
        return p;
    }

    // ── Right: white panel, form centered with GridBagLayout ─────────────────
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        // inner card — all rows use NONE fill so they keep their preferred sizes
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints r = rowConstraint();

        r.gridy = 0; r.insets = ins(0, 0, 4, 0);
        JLabel uLogo = new JLabel(AppIcon.get(64, 64), SwingConstants.CENTER);
        uLogo.setPreferredSize(new Dimension(370, 70));
        form.add(uLogo, r);

        r.gridy = 1; r.insets = ins(0, 0, 2, 0);
        JLabel uName = new JLabel("Mindanao State University", SwingConstants.CENTER);
        uName.setFont(new Font("Segoe UI", Font.BOLD, 15)); uName.setForeground(Theme.MAROON);
        uName.setPreferredSize(new Dimension(370, 22));
        form.add(uName, r);

        r.gridy = 2; r.insets = ins(0, 0, 6, 0);
        JLabel uSub = new JLabel("Iligan Institute of Technology", SwingConstants.CENTER);
        uSub.setFont(new Font("Segoe UI", Font.PLAIN, 11)); uSub.setForeground(Theme.TEXT_SECONDARY);
        uSub.setPreferredSize(new Dimension(370, 18));
        form.add(uSub, r);

        r.gridy = 3; r.insets = ins(0, 0, 4, 0);
        JPanel accentWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        accentWrap.setBackground(Color.WHITE);
        accentWrap.setPreferredSize(new Dimension(370, 6));
        JPanel accent = new JPanel(); accent.setBackground(Theme.GOLD);
        accent.setPreferredSize(new Dimension(44, 2)); accentWrap.add(accent);
        form.add(accentWrap, r);

        r.gridy = 4; r.insets = ins(0, 0, 18, 0);
        JLabel motto = new JLabel("Excellence in Education. Service to the Nation.", SwingConstants.CENTER);
        motto.setFont(new Font("Segoe UI", Font.ITALIC, 10)); motto.setForeground(Theme.TEXT_SECONDARY);
        motto.setPreferredSize(new Dimension(370, 16));
        form.add(motto, r);

        r.gridy = 5; r.insets = ins(0, 0, 2, 0);
        JLabel welcome = new JLabel("Welcome back!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24)); welcome.setForeground(Theme.TEXT_PRIMARY);
        welcome.setPreferredSize(new Dimension(370, 32));
        form.add(welcome, r);

        r.gridy = 6; r.insets = ins(0, 0, 14, 0);
        JLabel prompt = new JLabel("Please log in to continue to IITraQ.");
        prompt.setFont(new Font("Segoe UI", Font.PLAIN, 12)); prompt.setForeground(Theme.TEXT_SECONDARY);
        prompt.setPreferredSize(new Dimension(370, 18));
        form.add(prompt, r);

        r.gridy = 7; r.insets = ins(0, 0, 4, 0);
        form.add(fldLabel("ID Number"), r);

        r.gridy = 8; r.insets = ins(0, 0, 12, 0);
        idField = new JTextField(); styleInput(idField);
        form.add(idField, r);

        r.gridy = 9; r.insets = ins(0, 0, 4, 0);
        form.add(fldLabel("Password"), r);

        r.gridy = 10; r.insets = ins(0, 0, 20, 0);
        passField = new JPasswordField(); styleInput(passField);
        form.add(passField, r);

        r.gridy = 11; r.insets = ins(0, 0, 10, 0);
        JButton loginBtn = new JButton("  Log In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(Theme.MAROON); loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false); loginBtn.setBorderPainted(false); loginBtn.setOpaque(true);
        loginBtn.setPreferredSize(new Dimension(370, 46));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(loginBtn);
        form.add(loginBtn, r);

        r.gridy = 12; r.insets = ins(0, 0, 10, 0);
        form.add(orDivider(), r);

        r.gridy = 13; r.insets = ins(0, 0, 16, 0);
        JButton regBtn = new JButton("  Register as Student");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        regBtn.setBackground(Color.WHITE); regBtn.setForeground(Theme.MAROON);
        regBtn.setFocusPainted(false); regBtn.setOpaque(true);
        regBtn.setPreferredSize(new Dimension(370, 46));
        regBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.MAROON, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        regBtn.addActionListener(e -> openRegister());
        form.add(regBtn, r);

        r.gridy = 14; r.insets = ins(0, 0, 0, 0);
        JLabel secure = new JLabel("🔒  Secure and trusted system for MSU-IIT students.", SwingConstants.CENTER);
        secure.setFont(new Font("Segoe UI", Font.PLAIN, 10)); secure.setForeground(new Color(160, 160, 170));
        secure.setPreferredSize(new Dimension(370, 16));
        form.add(secure, r);

        outer.add(form);
        return outer;
    }

    // reusable constraint: no fill, centered, no weight
    private GridBagConstraints rowConstraint() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.NONE;
        c.weightx = 0; c.anchor = GridBagConstraints.CENTER;
        return c;
    }

    private Insets ins(int t, int l, int b, int ri) { return new Insets(t, l, b, ri); }

    private JLabel fldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); l.setForeground(Theme.TEXT_PRIMARY);
        l.setPreferredSize(new Dimension(370, 18));
        return l;
    }

    private void styleInput(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(370, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private JPanel orDivider() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(Color.WHITE);
        row.setPreferredSize(new Dimension(370, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        row.add(new JSeparator(), gc);
        gc.weightx = 0; gc.insets = new Insets(0, 8, 0, 8);
        JLabel or = new JLabel("OR"); or.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        or.setForeground(Theme.TEXT_SECONDARY); row.add(or, gc);
        gc.weightx = 1; gc.insets = new Insets(0, 0, 0, 0);
        row.add(new JSeparator(), gc);
        return row;
    }

    private void handleLogin() {
        String id = idField.getText().trim();
        String pw = new String(passField.getPassword());
        User u = AuthController.login(id, pw);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Invalid ID or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
        if      (u instanceof Admin   a) new AdminFrame(a).setVisible(true);
        else if (u instanceof Staff   s) new StaffFrame(s).setVisible(true);
        else if (u instanceof Student s) new StudentFrame(s).setVisible(true);
    }

    private void openRegister() { new RegisterDialog(this).setVisible(true); }
}
