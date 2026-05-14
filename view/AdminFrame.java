package view;

import controllers.AdminController;
import controllers.AuthController;
import models.*;
import store.DataStore;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Admin dashboard — Home, Users, Requests (status subtabs), Activity Log
public class AdminFrame extends JFrame {

    private static final String[] STATUS_TABS = {
        "All", "Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"
    };

    private final Admin admin;
    private DefaultTableModel userModel, logModel;
    private final DefaultTableModel[] reqModels = new DefaultTableModel[STATUS_TABS.length];
    private JTabbedPane reqTabs; // promoted so home page can select a specific tab

    private final JButton navHome     = new JButton("Home");
    private final JButton navUsers    = new JButton("Users");
    private final JButton navRequests = new JButton("Requests");
    private final JButton navLog      = new JButton("Activity Log");
    private final JButton navTrack    = new JButton("Track Requests");
    private final JPanel  contentArea = new JPanel(new CardLayout());

    public AdminFrame(Admin admin) {
        this.admin = admin;
        setTitle("IITraQ");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(AppIcon.getImage());
        getContentPane().setBackground(Theme.BG_PAGE);

        JButton logoutBtn = new JButton("Sign Out"); Theme.styleButtonSecondary(logoutBtn);
        logoutBtn.addActionListener(e -> logout());
        JPanel topBar = Theme.makeTopBar(admin.getName(), "Administrator", logoutBtn);

        Theme.styleButtonNavActive(navHome);
        Theme.styleButtonNav(navUsers);
        Theme.styleButtonNav(navRequests);
        Theme.styleButtonNav(navLog);
        Theme.styleButtonNav(navTrack);
        navHome.addActionListener(e     -> showCard("home"));
        navUsers.addActionListener(e    -> { showCard("users");    refreshUsers(); });
        navRequests.addActionListener(e -> { showCard("requests"); refreshRequests(); });
        navLog.addActionListener(e      -> { showCard("log");      refreshLog(); });
        navTrack.addActionListener(e    -> { showCard("track");    refreshTrack(); });
        JPanel sidebar = Theme.makeSidebar("IITraQ", navHome, navUsers, navRequests, navLog, navTrack);

        contentArea.setBackground(Theme.BG_PAGE);
        contentArea.add(buildHomePage(),      "home");
        contentArea.add(buildUsersPanel(),    "users");
        contentArea.add(buildRequestsPage(),  "requests");
        contentArea.add(buildLogPanel(),      "log");
        contentArea.add(buildTrackPage(),     "track");

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_PAGE);
        center.add(sidebar,     BorderLayout.WEST);
        center.add(contentArea, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshUsers(); refreshRequests(); refreshLog();
    }

    private void showCard(String name) {
        ((CardLayout) contentArea.getLayout()).show(contentArea, name);
        JButton[] all  = {navHome, navUsers, navRequests, navLog, navTrack};
        String[]  keys = {"home",  "users",  "requests",  "log",  "track"};
        for (int i = 0; i < all.length; i++) {
            if (keys[i].equals(name)) Theme.styleButtonNavActive(all[i]);
            else                      Theme.styleButtonNav(all[i]);
        }
    }

    // ── Home page ─────────────────────────────────────────────────────────────
    private JPanel buildHomePage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG_PAGE);

        // welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.MAROON);
        banner.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));
        banner.setPreferredSize(new Dimension(0, 110));

        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        JLabel wb = new JLabel("Welcome back,");
        wb.setFont(new Font("Segoe UI", Font.PLAIN, 15)); wb.setForeground(new Color(220, 200, 200)); wb.setAlignmentX(0f);
        JLabel nameLbl = new JLabel("Administrator - SYSTEM ADMIN ");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 26)); nameLbl.setForeground(Theme.GOLD); nameLbl.setAlignmentX(0f);
        JLabel desc = new JLabel("Manage users, monitor requests, and oversee the entire IITraQ system.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12)); desc.setForeground(new Color(210, 185, 185)); desc.setAlignmentX(0f);
        bannerText.add(wb); bannerText.add(Box.createVerticalStrut(4)); bannerText.add(nameLbl);
        bannerText.add(Box.createVerticalStrut(6)); bannerText.add(desc);
        banner.add(bannerText, BorderLayout.CENTER);

        // stat cards — plain text labels, no emoji
        long totalUsers = DataStore.users.size();
        long totalReqs  = DataStore.requests.size() + DataStore.completed.size();
        long pending    = DataStore.requests.values().stream().filter(r -> "Pending".equals(r.getStatus())).count();
        long completed  = DataStore.completed.size();

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("U", String.valueOf(totalUsers), "Total Users",    "Manage users",       new Color(227, 242, 253), () -> showCard("users")));
        stats.add(Theme.statCard("R", String.valueOf(totalReqs),  "Total Requests", "View all requests",  new Color(255, 235, 235), () -> showRequestsTab("All")));
        stats.add(Theme.statCard("P", String.valueOf(pending),    "Pending",        "Needs attention",    new Color(255, 248, 220), () -> showRequestsTab("Pending")));
        stats.add(Theme.statCard("D", String.valueOf(completed),  "Completed",      "View completed",     new Color(232, 245, 233), () -> showRequestsTab("Completed")));

        // north panel: banner + stats stacked, fixed height
        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        north.add(banner, BorderLayout.NORTH);
        north.add(stats,  BorderLayout.CENTER);

        page.add(north, BorderLayout.NORTH);
        return page;
    }

    // navigate to requests page and select a specific status tab
    private void showRequestsTab(String status) {
        showCard("requests");
        refreshRequests();
        if (reqTabs == null) return;
        for (int i = 0; i < STATUS_TABS.length; i++) {
            if (STATUS_TABS[i].equals(status)) { reqTabs.setSelectedIndex(i); break; }
        }
    }
    private JPanel buildUsersPanel() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JLabel title = new JLabel("User Management");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        String[] cols = {"ID Number", "Name", "Role", "Email"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(userModel);
        Theme.styleTable(table);

        JPanel actions = new JPanel(new GridLayout(3, 1, 4, 4));
        actions.setBackground(Theme.BG_PAGE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        actions.add(buildCreateStaffRow());
        actions.add(buildDeleteRow());
        actions.add(buildResetRow());

        page.add(title,                   BorderLayout.NORTH);
        page.add(Theme.scrollPane(table), BorderLayout.CENTER);
        page.add(actions,                 BorderLayout.SOUTH);
        return page;
    }

    // ── Requests page ─────────────────────────────────────────────────────────
    private JPanel buildRequestsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Requests");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        reqTabs = new JTabbedPane();
        reqTabs.setFont(Theme.FONT_HEADER);
        reqTabs.setBackground(Theme.BG_PAGE);

        String[] cols = {"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Student ID", "Submitted"};
        for (int i = 0; i < STATUS_TABS.length; i++) {
            reqModels[i] = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable table = new JTable(reqModels[i]);
            Theme.styleTable(table); Theme.applyStatusRenderer(table);
            reqTabs.addTab(STATUS_TABS[i], Theme.scrollPane(table));
        }

        page.add(title, BorderLayout.NORTH);
        page.add(reqTabs,  BorderLayout.CENTER);
        return page;
    }

    // ── Log panel ─────────────────────────────────────────────────────────────
    private JPanel buildLogPanel() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Activity Log");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        String[] cols = {"Timestamp", "Actor", "Role", "Action", "Detail"};
        logModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(logModel);
        Theme.styleTable(table);
        page.add(title, BorderLayout.NORTH); page.add(Theme.scrollPane(table), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildCreateStaffRow() {
        JPanel row = Theme.formBar();
        JTextField nameF = f(10), emailF = f(10), idF = f(8), deptF = f(10);
        JPasswordField pwF = new JPasswordField(8); Theme.styleField(pwF);
        JButton btn = new JButton("Create Staff Account"); Theme.styleButtonPrimary(btn);
        row.add(Theme.label("Name:"));  row.add(nameF);
        row.add(Theme.label("Email:")); row.add(emailF);
        row.add(Theme.label("ID:"));    row.add(idF);
        row.add(Theme.label("Dept:"));  row.add(deptF);
        row.add(Theme.label("Pass:"));  row.add(pwF);
        row.add(btn);
        btn.addActionListener(e -> {
            AdminController.createStaff(admin, nameF.getText().trim(), new String(pwF.getPassword()),
                emailF.getText().trim(), idF.getText().trim(), deptF.getText().trim());
            nameF.setText(""); emailF.setText(""); idF.setText(""); deptF.setText(""); pwF.setText("");
            refreshUsers(); refreshLog();
        });
        return row;
    }

    private JPanel buildDeleteRow() {
        JPanel row = Theme.formBar();
        JTextField delIdF = f(14);
        JButton btn = new JButton("Delete User"); Theme.styleButtonPrimary(btn);
        btn.setBackground(Theme.STATUS_REJECTED);
        row.add(Theme.label("ID to delete:")); row.add(delIdF); row.add(btn);
        btn.addActionListener(e -> {
            boolean ok = AdminController.deleteUser(admin, delIdF.getText().trim());
            JOptionPane.showMessageDialog(this, ok ? "User deleted." : "User not found.");
            delIdF.setText(""); refreshUsers(); refreshLog();
        });
        return row;
    }

    private JPanel buildResetRow() {
        JPanel row = Theme.formBar();
        JTextField resetIdF = f(12);
        JPasswordField newPwF = new JPasswordField(10); Theme.styleField(newPwF);
        JButton btn = new JButton("Reset Password"); Theme.styleButtonSecondary(btn);
        row.add(Theme.label("ID:")); row.add(resetIdF);
        row.add(Theme.label("New Password:")); row.add(newPwF); row.add(btn);
        btn.addActionListener(e -> {
            boolean ok = AdminController.resetPassword(admin, resetIdF.getText().trim(), new String(newPwF.getPassword()));
            JOptionPane.showMessageDialog(this, ok ? "Password reset." : "User not found.");
            resetIdF.setText(""); newPwF.setText(""); refreshLog();
        });
        return row;
    }

    private void refreshUsers() {
        userModel.setRowCount(0);
        for (User u : DataStore.users.values())
            userModel.addRow(new Object[]{ u.getIdNumber(), u.getName(), u.getClass().getSimpleName(), u.getEmail() });
    }

    private void refreshRequests() {
        for (DefaultTableModel m : reqModels) m.setRowCount(0);
        List<Map.Entry<UUID, Request>> all = new ArrayList<>();
        all.addAll(DataStore.requests.entrySet());
        all.addAll(DataStore.completed.entrySet());
        int rowNum = 1;
        for (Map.Entry<UUID, Request> entry : all) {
            Request r = entry.getValue();
            Object[] row = { rowNum++, r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage() != null ? r.getMessage() : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                resolveStudentId(r.getStudentId()), r.getCreatedAt().toString() };
            reqModels[0].addRow(row);
            for (int i = 1; i < STATUS_TABS.length; i++) {
                if (STATUS_TABS[i].equals(r.getStatus())) { reqModels[i].addRow(row); break; }
            }
        }
    }

    private void refreshLog() {
        logModel.setRowCount(0);
        for (ActivityLog log : DataStore.logs)
            logModel.addRow(new Object[]{ log.getFormattedTimestamp(), log.getActorName(),
                log.getActorRole(), log.getAction().name(), log.getDetail() });
    }

    private String resolveStudentId(UUID uuid) {
        for (User u : DataStore.users.values()) if (u.getId().equals(uuid)) return u.getIdNumber();
        return uuid.toString().substring(0, 8) + "…";
    }

    private void logout() { AuthController.logout(admin); dispose(); new LoginFrame().setVisible(true); }

    private JTextField f(int cols) { JTextField t = new JTextField(cols); Theme.styleField(t); return t; }

    // ── Track Requests page ───────────────────────────────────────────────────
    private DefaultTableModel trackModel;

    private JPanel buildTrackPage() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Track Requests");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel sub = new JLabel("Completed or rejected requests that are 1 or more days old.");
        sub.setFont(Theme.FONT_SMALL); sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(sub,   BorderLayout.SOUTH);

        String[] cols = {"#", "Document Type", "Status", "Reason", "Message", "Student ID", "Submitted", "Days Old"};
        trackModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(trackModel);
        Theme.styleTable(table); Theme.applyStatusRenderer(table);

        page.add(header,                  BorderLayout.NORTH);
        page.add(Theme.scrollPane(table), BorderLayout.CENTER);
        return page;
    }

    private void refreshTrack() {
        if (trackModel == null) return;
        trackModel.setRowCount(0);
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(1);
        int i = 1;
        for (Request r : DataStore.requests.values()) {
            if (r.getCreatedAt().isAfter(cutoff)) continue;
            if (!"Completed".equals(r.getStatus()) && !"Rejected".equals(r.getStatus())) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(r.getCreatedAt(), java.time.LocalDateTime.now());
            trackModel.addRow(new Object[]{ i++, r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage() != null ? r.getMessage() : "",
                resolveStudentId(r.getStudentId()), r.getCreatedAt().toString(),
                days + (days == 1 ? " day" : " days") });
        }
        for (Request r : DataStore.completed.values()) {
            if (r.getCreatedAt().isAfter(cutoff)) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(r.getCreatedAt(), java.time.LocalDateTime.now());
            trackModel.addRow(new Object[]{ i++, r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage() != null ? r.getMessage() : "",
                resolveStudentId(r.getStudentId()), r.getCreatedAt().toString(),
                days + (days == 1 ? " day" : " days") });
        }
    }
}
