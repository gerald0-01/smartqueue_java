package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Design system — matches IITraQ reference screenshot
public class Theme {

    // --- Colors ---
    public static final Color MAROON       = new Color(128,  0,   0);
    public static final Color MAROON_DARK  = new Color( 90,  0,   0);
    public static final Color MAROON_LIGHT = new Color(180, 40,  40);
    public static final Color GOLD         = new Color(255, 193,   7);  // amber-gold matching reference
    public static final Color GOLD_MUTED   = new Color(200, 160,  0);

    public static final Color BG_PAGE    = new Color(245, 245, 248);
    public static final Color BG_CARD    = Color.WHITE;
    public static final Color BG_ROW_ALT = new Color(250, 248, 252);
    public static final Color BG_SELECTED= new Color(128,   0,   0);

    public static final Color TEXT_PRIMARY   = new Color( 30,  30,  40);
    public static final Color TEXT_SECONDARY = new Color(100, 100, 115);
    public static final Color TEXT_ON_DARK   = Color.WHITE;

    public static final Color BORDER       = new Color(220, 220, 228);
    public static final Color BORDER_FOCUS = MAROON;

    public static final Color STATUS_PENDING    = new Color(255, 193,   7);
    public static final Color STATUS_PROCESSING = new Color( 33, 150, 243);
    public static final Color STATUS_READY      = new Color( 76, 175,  80);
    public static final Color STATUS_COMPLETED  = new Color(158, 158, 158);
    public static final Color STATUS_REJECTED   = new Color(244,  67,  54);

    // --- Fonts ---
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE    = new Font("Segoe UI", Font.BOLD,  11);

    // --- Dimensions ---
    public static final int ROW_HEIGHT    = 36;
    public static final int SIDEBAR_WIDTH = 210;
    public static final int TOPBAR_HEIGHT = 60;

    // ── Buttons ──────────────────────────────────────────────────────────────

    public static void styleButtonPrimary(JButton btn) {
        btn.setBackground(MAROON);
        btn.setForeground(TEXT_ON_DARK);
        btn.setFont(FONT_HEADER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }

    public static void styleButtonSecondary(JButton btn) {
        btn.setBackground(BG_CARD);
        btn.setForeground(MAROON);
        btn.setFont(FONT_HEADER);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAROON, 1),
            BorderFactory.createEmptyBorder(7, 19, 7, 19)));
    }

    // inactive sidebar nav button — centered
    public static void styleButtonNav(JButton btn) {
        btn.setBackground(MAROON_DARK);
        btn.setForeground(new Color(210, 185, 185));
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
    }

    // active sidebar nav button — gold text, maroon bg, gold left accent
    public static void styleButtonNavActive(JButton btn) {
        btn.setBackground(MAROON);
        btn.setForeground(GOLD);
        btn.setFont(FONT_HEADER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, GOLD),
            BorderFactory.createEmptyBorder(12, 4, 12, 8)));
    }

    public static void styleButton(JButton btn) { styleButtonPrimary(btn); }

    // ── Inputs ───────────────────────────────────────────────────────────────

    public static void styleField(JTextField f) {
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_CARD);
        f.setCaretColor(MAROON);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static void styleCombo(JComboBox<?> box) {
        box.setFont(FONT_BODY);
        box.setBackground(BG_CARD);
        box.setForeground(TEXT_PRIMARY);
        box.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    // ── Tables ───────────────────────────────────────────────────────────────

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(BG_SELECTED);
        table.setSelectionForeground(TEXT_ON_DARK);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) { setBackground(row % 2 == 0 ? BG_CARD : BG_ROW_ALT); setForeground(TEXT_PRIMARY); }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(BG_PAGE);
        header.setForeground(TEXT_SECONDARY);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(BG_PAGE); setForeground(TEXT_SECONDARY); setFont(FONT_HEADER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getViewport().setBackground(BG_CARD);
        sp.setBackground(BG_CARD);
        return sp;
    }

    // ── Layout builders ──────────────────────────────────────────────────────

    // Top bar — white bg, logo+name left, user avatar right, all vertically centered
    public static JPanel makeTopBar(String userName, String role, JComponent... right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setPreferredSize(new Dimension(0, TOPBAR_HEIGHT));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(0, 16, 0, 16)));

        // left: logo + app name, vertically centered
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(BG_CARD);
        GridBagConstraints lc = new GridBagConstraints();
        lc.insets = new Insets(0, 0, 0, 8); lc.anchor = GridBagConstraints.CENTER;
        left.add(new JLabel(AppIcon.get(32, 32)), lc);
        lc.insets = new Insets(0, 0, 0, 0);
        JLabel appLbl = new JLabel("IITraQ");
        appLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appLbl.setForeground(MAROON);
        left.add(appLbl, lc);

        // right: extra buttons + user avatar chip, vertically centered
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(BG_CARD);
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(0, 8, 0, 0); rc.anchor = GridBagConstraints.CENTER;
        for (JComponent c : right) rightPanel.add(c, rc);

        // user avatar circle + name + role
        JPanel userChip = new JPanel(new GridBagLayout());
        userChip.setBackground(BG_CARD);
        GridBagConstraints uc = new GridBagConstraints();
        uc.insets = new Insets(0, 8, 0, 0); uc.anchor = GridBagConstraints.CENTER;

        JLabel avatar = new JLabel(initials(userName), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAROON);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));
        userChip.add(avatar, uc);

        JPanel nameStack = new JPanel();
        nameStack.setOpaque(false);
        nameStack.setLayout(new BoxLayout(nameStack, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(userName);
        nameLbl.setFont(FONT_HEADER); nameLbl.setForeground(TEXT_PRIMARY);
        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(FONT_SMALL); roleLbl.setForeground(TEXT_SECONDARY);
        nameStack.add(nameLbl); nameStack.add(roleLbl);
        userChip.add(nameStack, uc);

        rightPanel.add(userChip, rc);

        bar.add(left,       BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    // Sidebar — maroon dark, logo+name header, centered nav items, university footer
    public static JPanel makeSidebar(String appName, JButton... navButtons) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(MAROON_DARK);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setMaximumSize(new Dimension(SIDEBAR_WIDTH, Integer.MAX_VALUE));

        // header: logo + app name centered
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 14));
        header.setBackground(MAROON_DARK);
        header.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 72));
        header.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 72));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel logoLbl = new JLabel(AppIcon.get(30, 30));
        JLabel appLbl  = new JLabel(appName);
        appLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appLbl.setForeground(GOLD);
        header.add(logoLbl); header.add(appLbl);
        sidebar.add(header);

        // divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(110, 30, 30));
        sep.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(6));

        // nav buttons — full width, centered
        for (JButton btn : navButtons) {
            btn.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 46));
            btn.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 46));
            btn.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 46));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(btn);
        }

        sidebar.add(Box.createVerticalGlue());

        // university footer
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(110, 30, 30));
        sep2.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 1));
        sep2.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(sep2);

        JPanel footer = new JPanel();
        footer.setBackground(MAROON_DARK);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
        footer.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 90));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uni1 = new JLabel("Mindanao State University");
        uni1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        uni1.setForeground(new Color(210, 185, 185));
        uni1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel uni2 = new JLabel("Iligan Institute of Technology");
        uni2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        uni2.setForeground(new Color(180, 155, 155));
        uni2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel uni3 = new JLabel("Excellence in Education.");
        uni3.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        uni3.setForeground(new Color(160, 135, 135));
        uni3.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(uni1); footer.add(Box.createVerticalStrut(2));
        footer.add(uni2); footer.add(Box.createVerticalStrut(2));
        footer.add(uni3);
        sidebar.add(footer);

        return sidebar;
    }

    // Stat card — white card with colored label box, count, and subtitle
    // onClick: called when the card is clicked (e.g. navigate to requests page)
    public static JPanel statCard(String label, String count, String title, String sub, Color iconBg) {
        return statCard(label, count, title, sub, iconBg, null);
    }

    public static JPanel statCard(String label, String count, String title, String sub, Color iconBg, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        if (onClick != null) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(new Color(250, 248, 252)); }
                @Override public void mouseExited(java.awt.event.MouseEvent e)  { card.setBackground(BG_CARD); }
            });
        }

        // fixed-size colored box — wrap in a panel so BorderLayout.WEST doesn't stretch it
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        JLabel iconLbl = new JLabel(label.substring(0, 1), SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        iconLbl.setForeground(MAROON);
        iconLbl.setOpaque(true);
        iconLbl.setBackground(iconBg);
        iconLbl.setPreferredSize(new Dimension(44, 44));
        iconWrap.add(iconLbl);

        // text stack
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_HEADER); titleLbl.setForeground(TEXT_PRIMARY);
        JLabel countLbl = new JLabel(count);
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 24)); countLbl.setForeground(TEXT_PRIMARY);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(FONT_SMALL); subLbl.setForeground(TEXT_SECONDARY);
        text.add(titleLbl); text.add(countLbl); text.add(subLbl);

        card.add(iconWrap, BorderLayout.WEST);
        card.add(text,     BorderLayout.CENTER);
        return card;
    }

    // card panel — white bg with border and padding
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY); l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel labelOnDark(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY); l.setForeground(GOLD);
        return l;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JPanel formBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(BG_PAGE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        return p;
    }

    // ── Status badges ─────────────────────────────────────────────────────────

    public static JLabel statusBadge(String status) {
        JLabel badge = new JLabel(status, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_BADGE);
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        switch (status) {
            case "Pending"           -> badge.setBackground(STATUS_PENDING);
            case "Processing"        -> badge.setBackground(STATUS_PROCESSING);
            case "Ready for Pick-Up" -> badge.setBackground(STATUS_READY);
            case "Completed"         -> badge.setBackground(STATUS_COMPLETED);
            case "Rejected"          -> badge.setBackground(STATUS_REJECTED);
            default                  -> badge.setBackground(TEXT_SECONDARY);
        }
        return badge;
    }

    public static TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                String status = value != null ? value.toString() : "";
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                wrapper.setBackground(sel ? BG_SELECTED : (row % 2 == 0 ? BG_CARD : BG_ROW_ALT));
                wrapper.add(statusBadge(status));
                return wrapper;
            }
        };
    }

    public static void applyStatusRenderer(JTable table) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if ("Status".equals(table.getColumnName(i))) {
                table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer());
                break;
            }
        }
    }

    // returns first two initials of a name for the avatar circle
    private static String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }
}
