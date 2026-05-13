package view;

import controllers.AuthController;
import controllers.RequestController;
import models.*;
import store.DataStore;

// iText 5 — fully qualified where needed to avoid clash with java.awt.Font
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

// Staff dashboard: active requests tab + completed tab, PDF export, 24-hour auto-archive
public class StaffFrame extends JFrame {

    private static final Color     MAROON     = new Color(128, 0, 0);
    private static final Color     GOLD       = new Color(255, 215, 0);
    private static final BaseColor PDF_MAROON = new BaseColor(128, 0, 0);
    private static final BaseColor PDF_WHITE  = BaseColor.WHITE;

    private final Staff staff;

    // active requests table
    private DefaultTableModel activeModel;
    private List<UUID>        activeIds = new ArrayList<>();

    // completed requests table
    private DefaultTableModel completedModel;

    public StaffFrame(Staff staff) {
        this.staff = staff;
        setTitle("SmartQueue - Staff: " + staff.getName());
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MAROON);

        // top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAROON);
        JLabel welcome = new JLabel("Staff: " + staff.getName() + " | " + staff.getDepartment());
        welcome.setForeground(GOLD);
        welcome.setFont(new Font("Arial", Font.BOLD, 14));
        welcome.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 3));
        topRight.setBackground(MAROON);
        JButton pdfBtn    = new JButton("Download PDF"); styleButton(pdfBtn);
        JButton logoutBtn = new JButton("Logout");       styleButton(logoutBtn);
        pdfBtn.addActionListener(e -> generatePdf());
        logoutBtn.addActionListener(e -> logout());
        topRight.add(pdfBtn);
        topRight.add(logoutBtn);
        top.add(welcome,  BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        // tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(MAROON);
        tabs.setForeground(GOLD);
        tabs.addTab("Active Requests",    buildActiveTab());
        tabs.addTab("Completed (24h)",    buildCompletedTab());

        setLayout(new BorderLayout(5, 5));
        add(top,  BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refreshActive();
        refreshCompleted();

        // background timer: every 60 seconds, move requests completed 24+ hours ago to the archive
        // javax.swing.Timer fires on the Swing event thread so it's safe to touch the UI
        new javax.swing.Timer(60_000, e -> {
            archiveOldCompleted();
            refreshActive();
            refreshCompleted();
        }).start();
    }

    // builds the Active Requests tab: table + update form
    private JPanel buildActiveTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(MAROON);

        // columns: #, Document Type, Status, Reason, Message, Pick-Up, Student ID, Submitted
        String[] cols = {"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Student ID", "Submitted"};
        activeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(activeModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        // update form at the bottom
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.setBackground(MAROON);
        String[] statuses = {"Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        JTextField msgField    = new JTextField(18);
        JTextField pickUpField = new JTextField(16);
        JButton updateBtn = new JButton("Update Selected"); styleButton(updateBtn);

        form.add(label("Status:"));      form.add(statusBox);
        form.add(label("Message:"));     form.add(msgField);
        form.add(label("Pick-Up (yyyy-MM-ddTHH:mm):")); form.add(pickUpField);
        form.add(updateBtn);

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
            UUID id = activeIds.get(row);
            LocalDateTime pickUp = null;
            String pt = pickUpField.getText().trim();
            if (!pt.isEmpty()) {
                try { pickUp = LocalDateTime.parse(pt); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date. Use yyyy-MM-ddTHH:mm");
                    return;
                }
            }
            RequestController.update(staff, id, (String) statusBox.getSelectedItem(),
                                     msgField.getText().trim(), pickUp);
            msgField.setText(""); pickUpField.setText("");

            // if just marked Completed, immediately check if it should be archived
            archiveOldCompleted();
            refreshActive();
            refreshCompleted();
        });

        p.add(scroll, BorderLayout.CENTER);
        p.add(form,   BorderLayout.SOUTH);
        return p;
    }

    // builds the Completed tab: read-only table of archived requests
    private JPanel buildCompletedTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(MAROON);
        String[] cols = {"#", "Document Type", "Reason", "Message", "Student ID", "Completed At", "Submitted"};
        completedModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(completedModel);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        // info label at the bottom explaining the 24-hour rule
        JLabel info = new JLabel("Requests are moved here when marked Completed. They are removed after 24 hours.");
        info.setForeground(GOLD);
        info.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    // moves any Completed request whose completedAt is older than 24 hours out of active requests
    // and into DataStore.completed — they stay there for display but are gone from active
    private void archiveOldCompleted() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        Iterator<Map.Entry<UUID, Request>> it = DataStore.requests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Request> entry = it.next();
            Request r = entry.getValue();
            // only move if status is Completed AND completedAt is set AND older than 24h
            if ("Completed".equals(r.getStatus())
                    && r.getCompletedAt() != null
                    && r.getCompletedAt().isBefore(cutoff)) {
                DataStore.completed.put(entry.getKey(), r); // archive it
                it.remove();                                // remove from active
            }
        }
    }

    // looks up the student's ID number string from DataStore.users by matching UUID
    // falls back to a short UUID prefix if the student account was deleted
    private String resolveStudentId(UUID studentUuid) {
        for (User u : DataStore.users.values()) {
            if (u.getId().equals(studentUuid)) return u.getIdNumber();
        }
        return studentUuid.toString().substring(0, 8) + "…"; // fallback
    }

    private void refreshActive() {
        activeModel.setRowCount(0);
        activeIds.clear();
        int i = 1;
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(staff).entrySet()) {
            Request r = entry.getValue();
            // skip requests already marked Completed — they show in the Completed tab
            if ("Completed".equals(r.getStatus())) continue;
            activeModel.addRow(new Object[]{
                i++,
                r.getDocumentType(),
                r.getStatus(),
                r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                resolveStudentId(r.getStudentId()),  // real ID number, not UUID
                r.getCreatedAt().toString()
            });
            activeIds.add(entry.getKey());
        }

        // also show Completed requests that haven't been archived yet (completed < 24h ago)
        for (Map.Entry<UUID, Request> entry : DataStore.requests.entrySet()) {
            Request r = entry.getValue();
            if (!"Completed".equals(r.getStatus())) continue;
            activeModel.addRow(new Object[]{
                i++,
                r.getDocumentType(),
                r.getStatus(),
                r.getReason(),
                r.getMessage()        != null ? r.getMessage()                   : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                resolveStudentId(r.getStudentId()),
                r.getCreatedAt().toString()
            });
            activeIds.add(entry.getKey());
        }
    }

    private void refreshCompleted() {
        completedModel.setRowCount(0);
        int i = 1;
        for (Request r : DataStore.completed.values()) {
            completedModel.addRow(new Object[]{
                i++,
                r.getDocumentType(),
                r.getReason(),
                r.getMessage() != null ? r.getMessage() : "",
                resolveStudentId(r.getStudentId()),
                r.getCompletedAt() != null ? r.getCompletedAt().toString() : "",
                r.getCreatedAt().toString()
            });
        }
    }

    // generates a PDF of the active requests table
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
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            com.itextpdf.text.Font titleFont  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD,   PDF_MAROON);
            com.itextpdf.text.Font subFont    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,  9, com.itextpdf.text.Font.BOLD,   PDF_WHITE);
            com.itextpdf.text.Font cellFont   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,  8, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            Paragraph title = new Paragraph("SmartQueue - Document Requests Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("\n"));

            Paragraph info = new Paragraph(
                "Generated by: " + staff.getName() + " (" + staff.getDepartment() + ")" +
                "    Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                subFont);
            info.setAlignment(Element.ALIGN_CENTER);
            doc.add(info);

            Paragraph cnt = new Paragraph("Total Active Requests: " + activeModel.getRowCount(), subFont);
            cnt.setAlignment(Element.ALIGN_CENTER);
            doc.add(cnt);
            doc.add(new Paragraph("\n"));

            PdfPTable pdfTable = new PdfPTable(7);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{1f, 3f, 2f, 3f, 3f, 2.5f, 2.5f});

            for (String h : new String[]{"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(PDF_MAROON);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                pdfTable.addCell(cell);
            }

            for (int row = 0; row < activeModel.getRowCount(); row++) {
                BaseColor bg = (row % 2 == 0) ? PDF_WHITE : new BaseColor(255, 248, 220);
                for (int col : new int[]{0, 1, 2, 3, 4, 5, 7}) {
                    Object val = activeModel.getValueAt(row, col);
                    PdfPCell cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", cellFont));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(4);
                    pdfTable.addCell(cell);
                }
            }

            doc.add(pdfTable);
            doc.close();
            JOptionPane.showMessageDialog(this, "PDF saved to:\n" + file.getAbsolutePath());

        } catch (DocumentException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save PDF:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        AuthController.logout(staff);
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
