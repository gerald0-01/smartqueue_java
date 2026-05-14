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

// Student dashboard — Home, My Requests (with status subtabs), New Request
public class StudentFrame extends JFrame {

    private static final String[] STATUS_TABS = {
        "All", "Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"
    };

    private final Student student;
    private final DefaultTableModel[] tabModels = new DefaultTableModel[STATUS_TABS.length];
    private JTabbedPane reqTabs; // field so showRequestsTab can select a specific tab

    private final JButton navHome     = new JButton("Home");
    private final JButton navRequests = new JButton("My Requests");
    private final JButton navNew      = new JButton("New Request");
    private final JButton navTrack    = new JButton("Track Requests");
    private final JPanel  contentArea = new JPanel(new CardLayout());

    public StudentFrame(Student student) {
        this.student = student;
        setTitle("IITraQ");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(AppIcon.getImage());
        getContentPane().setBackground(Theme.BG_PAGE);

        JButton logoutBtn = new JButton("Sign Out");
        Theme.styleButtonSecondary(logoutBtn);
        logoutBtn.addActionListener(e -> logout());
        JPanel topBar = Theme.makeTopBar(student.getName(), "Student", logoutBtn);

        Theme.styleButtonNavActive(navHome);
        Theme.styleButtonNav(navRequests);
        Theme.styleButtonNav(navNew);
        Theme.styleButtonNav(navTrack);
        navHome.addActionListener(e     -> showCard("home"));
        navRequests.addActionListener(e -> { showCard("requests"); refreshAll(); });
        navNew.addActionListener(e      -> showCard("new"));
        navTrack.addActionListener(e    -> { showCard("track"); refreshTrack(); });
        JPanel sidebar = Theme.makeSidebar("IITraQ", navHome, navRequests, navNew, navTrack);

        contentArea.setBackground(Theme.BG_PAGE);
        contentArea.add(buildHomePage(),      "home");
        contentArea.add(buildRequestsPage(),  "requests");
        contentArea.add(buildNewRequestPage(),"new");
        contentArea.add(buildTrackPage(),     "track");

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_PAGE);
        center.add(sidebar,     BorderLayout.WEST);
        center.add(contentArea, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshAll();
    }

    private void showCard(String name) {
        ((CardLayout) contentArea.getLayout()).show(contentArea, name);
        Theme.styleButtonNav(navHome); Theme.styleButtonNav(navRequests);
        Theme.styleButtonNav(navNew);  Theme.styleButtonNav(navTrack);
        switch (name) {
            case "home"     -> Theme.styleButtonNavActive(navHome);
            case "requests" -> Theme.styleButtonNavActive(navRequests);
            case "new"      -> Theme.styleButtonNavActive(navNew);
            case "track"    -> Theme.styleButtonNavActive(navTrack);
        }
    }

    // navigate to requests page and select a specific status tab
    private void showRequestsTab(String status) {
        showCard("requests");
        refreshAll();
        if (reqTabs == null) return;
        for (int i = 0; i < STATUS_TABS.length; i++) {
            if (STATUS_TABS[i].equals(status)) { reqTabs.setSelectedIndex(i); break; }
        }
    }

    // ── Home page ─────────────────────────────────────────────────────────────
    private JPanel buildHomePage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.MAROON);
        banner.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        banner.setPreferredSize(new Dimension(0, 110));
        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        JLabel wb = new JLabel("Welcome back,");
        wb.setFont(new Font("Segoe UI", Font.PLAIN, 15)); wb.setForeground(new Color(220, 200, 200)); wb.setAlignmentX(0f);
        JLabel nameLabel = new JLabel(student.getName() + "!");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26)); nameLabel.setForeground(Theme.GOLD); nameLabel.setAlignmentX(0f);
        JLabel desc = new JLabel("Request documents, get in queue, and track your request status.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12)); desc.setForeground(new Color(210, 185, 185)); desc.setAlignmentX(0f);
        bannerText.add(wb); bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(nameLabel); bannerText.add(Box.createVerticalStrut(6)); bannerText.add(desc);
        JButton newReqBtn = new JButton("New Request");
        newReqBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        newReqBtn.setBackground(Theme.GOLD); newReqBtn.setForeground(Theme.MAROON);
        newReqBtn.setFocusPainted(false); newReqBtn.setBorderPainted(false); newReqBtn.setOpaque(true);
        newReqBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newReqBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        newReqBtn.addActionListener(e -> showCard("new"));
        JPanel bannerRight = new JPanel(new GridBagLayout());
        bannerRight.setOpaque(false); bannerRight.add(newReqBtn);
        banner.add(bannerText, BorderLayout.CENTER);
        banner.add(bannerRight, BorderLayout.EAST);

        // stat cards
        int total     = countRequests(null);
        int inQueue   = countRequests("Pending") + countRequests("Processing");
        int completed = countRequests("Completed") + (int) DataStore.completed.values().stream()
            .filter(r -> r.getStudentId().equals(student.getId())).count();
        int ready     = countRequests("Ready for Pick-Up");

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("T", String.valueOf(total),     "Total Requests",  "View all requests", new Color(255, 235, 235), () -> showRequestsTab("All")));
        stats.add(Theme.statCard("Q", String.valueOf(inQueue),   "In Queue",        "Being processed",   new Color(255, 248, 220), () -> showRequestsTab("Pending")));
        stats.add(Theme.statCard("D", String.valueOf(completed), "Completed",       "View completed",    new Color(232, 245, 233), () -> showRequestsTab("Completed")));
        stats.add(Theme.statCard("P", String.valueOf(ready),     "Ready for Pickup","Awaiting claim",    new Color(227, 242, 253), () -> showRequestsTab("Ready for Pick-Up")));

        // how it works
        JPanel steps = new JPanel(new GridLayout(1, 4, 12, 0));
        steps.setOpaque(false);
        steps.add(stepCard("1", "Submit Request",  "Fill out the form and submit your document request."));
        steps.add(stepCard("2", "Get in Queue",    "Your request will be added to the queue and processed."));
        steps.add(stepCard("3", "Track Status",    "Monitor your request status in real-time."));
        steps.add(stepCard("4", "Claim Document",  "Once completed, claim your document at the office."));

        JLabel howTitle = new JLabel("How It Works");
        howTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); howTitle.setForeground(Theme.MAROON);

        // center panel: stats + how it works stacked
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        JPanel howPanel = new JPanel(new BorderLayout(0, 8));
        howPanel.setOpaque(false);
        howPanel.add(howTitle, BorderLayout.NORTH);
        howPanel.add(steps,    BorderLayout.CENTER);
        center.add(stats,    BorderLayout.NORTH);
        center.add(howPanel, BorderLayout.CENTER);

        page.add(banner, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private int countRequests(String status) {
        int count = 0;
        for (Request r : RequestController.getFor(student).values()) {
            if (status == null || status.equals(r.getStatus())) count++;
        }
        return count;
    }

    private JPanel stepCard(String num, String title, String desc) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;

        // small circle badge left-aligned
        JLabel numLbl = new JLabel(num, SwingConstants.CENTER);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numLbl.setForeground(Color.WHITE); numLbl.setOpaque(true);
        numLbl.setBackground(Theme.MAROON);
        numLbl.setPreferredSize(new Dimension(28, 28));
        JPanel numRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        numRow.setOpaque(false); numRow.add(numLbl);
        c.gridy = 0; c.insets = new Insets(0, 0, 10, 0); p.add(numRow, c);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEADER); titleLbl.setForeground(Theme.TEXT_PRIMARY);
        c.gridy = 1; c.insets = new Insets(0, 0, 6, 0); p.add(titleLbl, c);

        JLabel descLbl = new JLabel("<html><body style='width:110px'>" + desc + "</body></html>");
        descLbl.setFont(Theme.FONT_SMALL); descLbl.setForeground(Theme.TEXT_SECONDARY);
        c.gridy = 2; c.insets = new Insets(0, 0, 0, 0); p.add(descLbl, c);

        return p;
    }

    // ── Requests page ─────────────────────────────────────────────────────────
    private JPanel buildRequestsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Requests");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        reqTabs = new JTabbedPane();
        reqTabs.setFont(Theme.FONT_HEADER);
        reqTabs.setBackground(Theme.BG_PAGE);

        String[] cols = {"Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"};
        for (int i = 0; i < STATUS_TABS.length; i++) {
            tabModels[i] = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable table = new JTable(tabModels[i]);
            Theme.styleTable(table); Theme.applyStatusRenderer(table);
            reqTabs.addTab(STATUS_TABS[i], Theme.scrollPane(table));
        }

        page.add(title,   BorderLayout.NORTH);
        page.add(reqTabs, BorderLayout.CENTER);
        return page;
    }

    // ── New Request page ──────────────────────────────────────────────────────
    private JPanel buildNewRequestPage() {
        // outer: GridBagLayout centers the card both horizontally and vertically
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.BG_PAGE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(32, 36, 32, 36)));
        // fixed width; height determined by children
        card.setPreferredSize(new Dimension(520, 420));

        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0; r.fill = GridBagConstraints.HORIZONTAL; r.weightx = 1;

        r.gridy = 0; r.insets = new Insets(0, 0, 4, 0);
        JLabel title = new JLabel("Submit a New Request");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        card.add(title, r);

        r.gridy = 1; r.insets = new Insets(0, 0, 20, 0);
        JLabel sub = new JLabel("Fill in the details below to submit your document request.");
        sub.setFont(Theme.FONT_SMALL); sub.setForeground(Theme.TEXT_SECONDARY);
        card.add(sub, r);

        r.gridy = 2; r.insets = new Insets(0, 0, 4, 0);
        card.add(Theme.label("Document Type"), r);

        String[] docTypes = {"Transcript of Records", "Certificate of Enrollment",
                             "Certificate of Graduation", "Good Moral Certificate", "Other"};
        JComboBox<String> docBox = new JComboBox<>(docTypes);
        Theme.styleCombo(docBox);
        r.gridy = 3; r.insets = new Insets(0, 0, 14, 0);
        card.add(docBox, r);

        JLabel otherLbl = Theme.label("Document Name");
        JTextField otherField = new JTextField(); Theme.styleField(otherField);
        otherLbl.setVisible(false); otherField.setVisible(false);
        r.gridy = 4; r.insets = new Insets(0, 0, 4, 0);  card.add(otherLbl, r);
        r.gridy = 5; r.insets = new Insets(0, 0, 14, 0); card.add(otherField, r);

        docBox.addActionListener(e -> {
            boolean other = "Other".equals(docBox.getSelectedItem());
            otherLbl.setVisible(other); otherField.setVisible(other);
            card.revalidate(); card.repaint();
        });

        r.gridy = 6; r.insets = new Insets(0, 0, 4, 0);
        card.add(Theme.label("Reason for Request"), r);

        JTextField reasonField = new JTextField(); Theme.styleField(reasonField);
        r.gridy = 7; r.insets = new Insets(0, 0, 28, 0);
        card.add(reasonField, r);

        JButton submitBtn = new JButton("Submit Request");
        Theme.styleButtonPrimary(submitBtn);
        r.gridy = 8; r.insets = new Insets(0, 0, 0, 0);
        card.add(submitBtn, r);

        submitBtn.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter a reason."); return; }
            String sel = (String) docBox.getSelectedItem();
            String docType = "Other".equals(sel) ? otherField.getText().trim() : sel;
            if (docType.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter the document name."); return; }
            RequestController.submit(student, docType, reason);
            reasonField.setText(""); otherField.setText(""); docBox.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this, "Request submitted successfully!");
            showCard("requests"); refreshAll();
        });

        outer.add(card);
        return outer;
    }

    // ── Track Requests page ───────────────────────────────────────────────────
    // Shows requests submitted 1+ days ago that are still active
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

        String[] cols = {"Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted", "Days Waiting"};
        trackModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(trackModel);
        Theme.styleTable(table);
        Theme.applyStatusRenderer(table);

        page.add(header,                  BorderLayout.NORTH);
        page.add(Theme.scrollPane(table), BorderLayout.CENTER);
        return page;
    }

    private void refreshTrack() {
        if (trackModel == null) return;
        trackModel.setRowCount(0);
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(1);

        // show completed/rejected requests older than 1 day — they are resolved and old
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(student).entrySet()) {
            Request r = entry.getValue();
            if (r.getCreatedAt().isAfter(cutoff)) continue; // less than 1 day old
            if (!"Completed".equals(r.getStatus()) && !"Rejected".equals(r.getStatus())) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(r.getCreatedAt(), java.time.LocalDateTime.now());
            trackModel.addRow(new Object[]{
                r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                r.getCreatedAt().toString(),
                days + (days == 1 ? " day" : " days")
            });
        }
        // also include archived completed requests belonging to this student
        for (Request r : DataStore.completed.values()) {
            if (!r.getStudentId().equals(student.getId())) continue;
            if (r.getCreatedAt().isAfter(cutoff)) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(r.getCreatedAt(), java.time.LocalDateTime.now());
            trackModel.addRow(new Object[]{
                r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                r.getCreatedAt().toString(),
                days + (days == 1 ? " day" : " days")
            });
        }
    }

    private void refreshAll() {
        for (DefaultTableModel m : tabModels) if (m != null) m.setRowCount(0);
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(student).entrySet()) {
            addToTabs(entry.getValue());
        }
        for (Request r : DataStore.completed.values()) {
            if (!r.getStudentId().equals(student.getId())) continue;
            addToTabs(r);
        }
    }

    private void addToTabs(Request r) {
        Object[] row = { r.getDocumentType(), r.getStatus(), r.getReason(),
            r.getMessage()        != null ? r.getMessage()                   : "",
            r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
            r.getCreatedAt().toString() };
        tabModels[0].addRow(row);
        for (int i = 1; i < STATUS_TABS.length; i++) {
            if (STATUS_TABS[i].equals(r.getStatus())) { tabModels[i].addRow(row); break; }
        }
    }

    private void logout() { AuthController.logout(student); dispose(); new LoginFrame().setVisible(true); }
}
