package view;

import controllers.AuthController;
import controllers.RequestController;
import models.*;
import store.DataStore;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Staff dashboard — Home, Requests (status subtabs), PDF export
public class StaffFrame extends JFrame {

    private static final BaseColor PDF_MAROON = new BaseColor(128, 0, 0);
    private static final BaseColor PDF_WHITE  = BaseColor.WHITE;

    private static final String[] STATUS_TABS = {
        "All", "Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"
    };

    private final Staff staff;
    private final DefaultTableModel[] tabModels = new DefaultTableModel[STATUS_TABS.length];
    private final List<List<UUID>>    tabIds    = new ArrayList<>();
    private JTabbedPane reqTabs; // promoted so home page can select a specific tab

    private final JButton navHome     = new JButton("Home");
    private final JButton navRequests = new JButton("Requests");
    private final JButton navTrack    = new JButton("Track Requests");
    private final JPanel  contentArea = new JPanel(new CardLayout());

    public StaffFrame(Staff staff) {
        this.staff = staff;
        setTitle("IITraQ");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(AppIcon.getImage());
        getContentPane().setBackground(Theme.BG_PAGE);

        for (int i = 0; i < STATUS_TABS.length; i++) tabIds.add(new ArrayList<>());

        JButton pdfBtn    = new JButton("Export PDF"); Theme.styleButtonSecondary(pdfBtn);
        JButton logoutBtn = new JButton("Sign Out");   Theme.styleButtonSecondary(logoutBtn);
        pdfBtn.addActionListener(e -> generatePdf());
        logoutBtn.addActionListener(e -> logout());

        JPanel topBar = Theme.makeTopBar(staff.getName(), "Staff  ·  " + staff.getDepartment(), pdfBtn, logoutBtn);

        Theme.styleButtonNavActive(navHome);
        Theme.styleButtonNav(navRequests);
        Theme.styleButtonNav(navTrack);
        navHome.addActionListener(e     -> showCard("home"));
        navRequests.addActionListener(e -> { showCard("requests"); refreshAll(); });
        navTrack.addActionListener(e    -> { showCard("track"); refreshTrack(); });
        JPanel sidebar = Theme.makeSidebar("IITraQ", navHome, navRequests, navTrack);

        contentArea.setBackground(Theme.BG_PAGE);
        contentArea.add(buildHomePage(),     "home");
        contentArea.add(buildRequestsPage(), "requests");
        contentArea.add(buildTrackPage(),    "track");

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_PAGE);
        center.add(sidebar,     BorderLayout.WEST);
        center.add(contentArea, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshAll();
        new javax.swing.Timer(60_000, e -> { archiveOldCompleted(); refreshAll(); }).start();
    }

    private void showCard(String name) {
        ((CardLayout) contentArea.getLayout()).show(contentArea, name);
        Theme.styleButtonNav(navHome); Theme.styleButtonNav(navRequests); Theme.styleButtonNav(navTrack);
        if ("home".equals(name))          Theme.styleButtonNavActive(navHome);
        else if ("requests".equals(name)) Theme.styleButtonNavActive(navRequests);
        else                              Theme.styleButtonNavActive(navTrack);
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
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG_PAGE);

        // welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.MAROON);
        banner.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        banner.setPreferredSize(new Dimension(0, 110));
        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        JLabel wb = new JLabel("Welcome back,");
        wb.setFont(new Font("Segoe UI", Font.PLAIN, 15)); wb.setForeground(new Color(220, 200, 200)); wb.setAlignmentX(0f);
        JLabel nameLbl = new JLabel(staff.getName() + " — " + staff.getDepartment());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 26)); nameLbl.setForeground(Theme.GOLD); nameLbl.setAlignmentX(0f);
        JLabel desc = new JLabel("Manage document requests, update statuses, and export reports.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12)); desc.setForeground(new Color(210, 185, 185)); desc.setAlignmentX(0f);
        bannerText.add(wb); bannerText.add(Box.createVerticalStrut(4)); bannerText.add(nameLbl);
        bannerText.add(Box.createVerticalStrut(6)); bannerText.add(desc);
        banner.add(bannerText, BorderLayout.CENTER);

        // stat cards
        long total     = DataStore.requests.size() + DataStore.completed.size();
        long inQueue   = DataStore.requests.values().stream().filter(r -> "Pending".equals(r.getStatus()) || "Processing".equals(r.getStatus())).count();
        long completed = DataStore.completed.size();
        long ready     = DataStore.requests.values().stream().filter(r -> "Ready for Pick-Up".equals(r.getStatus())).count();

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("T", String.valueOf(total),     "Total Requests",  "All requests",    new Color(255, 235, 235), () -> showRequestsTab("All")));
        stats.add(Theme.statCard("Q", String.valueOf(inQueue),   "In Queue",        "Being processed", new Color(255, 248, 220), () -> showRequestsTab("Pending")));
        stats.add(Theme.statCard("D", String.valueOf(completed), "Completed",       "View completed",  new Color(232, 245, 233), () -> showRequestsTab("Completed")));
        stats.add(Theme.statCard("P", String.valueOf(ready),     "Ready for Pickup","Awaiting claim",  new Color(227, 242, 253), () -> showRequestsTab("Ready for Pick-Up")));

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        north.add(banner, BorderLayout.NORTH);
        north.add(stats,  BorderLayout.CENTER);

        page.add(north, BorderLayout.NORTH);
        return page;
    }

    // ── Requests page ─────────────────────────────────────────────────────────
    private JPanel buildRequestsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(Theme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JLabel title = new JLabel("Requests");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        reqTabs = new JTabbedPane();
        reqTabs.setFont(Theme.FONT_HEADER);
        reqTabs.setBackground(Theme.BG_PAGE);

        String[] cols = {"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Student ID", "Submitted"};
        for (int i = 0; i < STATUS_TABS.length; i++) {
            tabModels[i] = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable table = new JTable(tabModels[i]);
            Theme.styleTable(table); Theme.applyStatusRenderer(table);
            reqTabs.addTab(STATUS_TABS[i], Theme.scrollPane(table));
        }

        // update form
        JPanel form = Theme.formBar();
        form.setPreferredSize(new Dimension(0, 90));
        String[] statuses = {"Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"};
        JComboBox<String> statusBox = new JComboBox<>(statuses); Theme.styleCombo(statusBox);
        JTextField msgField    = new JTextField(18); Theme.styleField(msgField);
        JTextField pickUpField = new JTextField(16); Theme.styleField(pickUpField);
        JButton updateBtn = new JButton("Update Selected"); Theme.styleButtonPrimary(updateBtn);

        form.add(Theme.label("Status:"));                       form.add(statusBox);
        form.add(Theme.label("Message:"));                      form.add(msgField);
        form.add(Theme.label("Pick-Up (yyyy-MM-ddTHH:mm):")); form.add(pickUpField);
        form.add(updateBtn);

        updateBtn.addActionListener(e -> {
            int tabIdx = reqTabs.getSelectedIndex();
            JTable activeTable = (JTable) ((JScrollPane) reqTabs.getSelectedComponent()).getViewport().getView();
            int row = activeTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
            UUID id = tabIds.get(tabIdx).get(row);
            LocalDateTime pickUp = null;
            String pt = pickUpField.getText().trim();
            if (!pt.isEmpty()) {
                try { pickUp = LocalDateTime.parse(pt); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid date. Use yyyy-MM-ddTHH:mm"); return; }
            }
            RequestController.update(staff, id, (String) statusBox.getSelectedItem(), msgField.getText().trim(), pickUp);
            msgField.setText(""); pickUpField.setText("");
            archiveOldCompleted(); refreshAll();
        });

        page.add(title,   BorderLayout.NORTH);
        page.add(reqTabs, BorderLayout.CENTER);
        page.add(form,    BorderLayout.SOUTH);
        return page;
    }

    private void refreshAll() {
        for (int i = 0; i < STATUS_TABS.length; i++) { tabModels[i].setRowCount(0); tabIds.get(i).clear(); }
        List<Map.Entry<UUID, Request>> all = new ArrayList<>();
        all.addAll(DataStore.requests.entrySet());
        all.addAll(DataStore.completed.entrySet());
        int rowNum = 1;
        for (Map.Entry<UUID, Request> entry : all) {
            Request r = entry.getValue();
            Object[] row = buildRow(rowNum++, r);
            tabModels[0].addRow(row); tabIds.get(0).add(entry.getKey());
            for (int i = 1; i < STATUS_TABS.length; i++) {
                if (STATUS_TABS[i].equals(r.getStatus())) {
                    tabModels[i].addRow(row); tabIds.get(i).add(entry.getKey()); break;
                }
            }
        }
    }

    private Object[] buildRow(int i, Request r) {
        return new Object[]{ i, r.getDocumentType(), r.getStatus(), r.getReason(),
            r.getMessage() != null ? r.getMessage() : "",
            r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
            resolveStudentId(r.getStudentId()), r.getCreatedAt().toString() };
    }

    private void archiveOldCompleted() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        Iterator<Map.Entry<UUID, Request>> it = DataStore.requests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Request> entry = it.next();
            Request r = entry.getValue();
            if ("Completed".equals(r.getStatus()) && r.getCompletedAt() != null && r.getCompletedAt().isBefore(cutoff)) {
                DataStore.completed.put(entry.getKey(), r); it.remove();
            }
        }
    }

    private String resolveStudentId(UUID uuid) {
        for (User u : DataStore.users.values()) if (u.getId().equals(uuid)) return u.getIdNumber();
        return uuid.toString().substring(0, 8) + "…";
    }

    private void generatePdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("requests_" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new java.io.File(file.getAbsolutePath() + ".pdf");
        Document doc = new Document(com.itextpdf.text.PageSize.A4);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(file)); doc.open();
            com.itextpdf.text.Font tf = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD,   PDF_MAROON);
            com.itextpdf.text.Font sf = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font hf = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,  9, com.itextpdf.text.Font.BOLD,   PDF_WHITE);
            com.itextpdf.text.Font cf = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,  8, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            Paragraph t = new Paragraph("IITraQ — Document Requests Report", tf);
            t.setAlignment(Element.ALIGN_CENTER); doc.add(t); doc.add(new Paragraph("\n"));
            Paragraph inf = new Paragraph("Generated by: " + staff.getName() + " (" + staff.getDepartment() + ")  |  " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), sf);
            inf.setAlignment(Element.ALIGN_CENTER); doc.add(inf);
            Paragraph cnt = new Paragraph("Total Requests: " + tabModels[0].getRowCount(), sf);
            cnt.setAlignment(Element.ALIGN_CENTER); doc.add(cnt); doc.add(new Paragraph("\n"));
            PdfPTable pt2 = new PdfPTable(7); pt2.setWidthPercentage(100);
            pt2.setWidths(new float[]{1f, 3f, 2f, 3f, 3f, 2.5f, 2.5f});
            for (String h : new String[]{"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, hf));
                cell.setBackgroundColor(PDF_MAROON); cell.setHorizontalAlignment(Element.ALIGN_CENTER); cell.setPadding(5); pt2.addCell(cell);
            }
            for (int r = 0; r < tabModels[0].getRowCount(); r++) {
                BaseColor bg = (r % 2 == 0) ? PDF_WHITE : new BaseColor(255, 248, 220);
                for (int col : new int[]{0, 1, 2, 3, 4, 5, 7}) {
                    Object val = tabModels[0].getValueAt(r, col);
                    PdfPCell cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", cf));
                    cell.setBackgroundColor(bg); cell.setPadding(4); pt2.addCell(cell);
                }
            }
            doc.add(pt2); doc.close();
            JOptionPane.showMessageDialog(this, "PDF saved:\n" + file.getAbsolutePath());
        } catch (DocumentException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() { AuthController.logout(staff); dispose(); new LoginFrame().setVisible(true); }

    // ── Track Requests page ───────────────────────────────────────────────────
    // Shows completed/rejected requests that are 1+ days old
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
        // from active requests — completed/rejected older than 1 day
        for (Request r : DataStore.requests.values()) {
            if (r.getCreatedAt().isAfter(cutoff)) continue;
            if (!"Completed".equals(r.getStatus()) && !"Rejected".equals(r.getStatus())) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(r.getCreatedAt(), java.time.LocalDateTime.now());
            trackModel.addRow(new Object[]{ i++, r.getDocumentType(), r.getStatus(), r.getReason(),
                r.getMessage() != null ? r.getMessage() : "",
                resolveStudentId(r.getStudentId()), r.getCreatedAt().toString(),
                days + (days == 1 ? " day" : " days") });
        }
        // from archived completed
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
