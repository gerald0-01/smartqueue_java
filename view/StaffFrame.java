package view;

import controllers.AuthController;
import controllers.RequestController;
import models.*;
import store.DataStore;

// iText 5 imports for PDF generation
// Note: we do NOT import com.itextpdf.text.Font to avoid clashing with java.awt.Font
// Instead we reference it as IFont (aliased via a local variable) or fully qualified
import com.itextpdf.text.Document;           // represents the whole PDF document
import com.itextpdf.text.DocumentException;  // iText's checked exception
import com.itextpdf.text.Element;            // alignment constants (ALIGN_CENTER, etc.)
import com.itextpdf.text.BaseColor;          // iText's color class (like java.awt.Color)
import com.itextpdf.text.Paragraph;          // a block of text with spacing
import com.itextpdf.text.Phrase;             // inline text used inside table cells
import com.itextpdf.text.pdf.PdfPCell;       // a single cell in a PDF table
import com.itextpdf.text.pdf.PdfPTable;      // a table in the PDF
import com.itextpdf.text.pdf.PdfWriter;      // writes the PDF bytes to an output stream

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;             // writes bytes to a file on disk
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Staff dashboard: view all requests, update status, and export to PDF
public class StaffFrame extends JFrame {

    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color GOLD   = new Color(255, 215, 0);

    // iText colors matching the app theme
    // BaseColor takes RGB values just like java.awt.Color
    private static final BaseColor PDF_MAROON = new BaseColor(128, 0, 0);
    private static final BaseColor PDF_GOLD   = new BaseColor(255, 215, 0);
    private static final BaseColor PDF_WHITE  = BaseColor.WHITE;

    private final Staff staff;
    private DefaultTableModel tableModel;
    private List<UUID> rowIds = new ArrayList<>(); // tracks which UUID each row maps to

    public StaffFrame(Staff staff) {
        this.staff = staff;
        setTitle("SmartQueue - Staff: " + staff.getName());
        setSize(900, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MAROON);

        // top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAROON);

        JLabel welcome = new JLabel("Staff: " + staff.getName() + " | " + staff.getDepartment(), SwingConstants.LEFT);
        welcome.setForeground(GOLD);
        welcome.setFont(new Font("Arial", Font.BOLD, 14));
        welcome.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        // right side of top bar holds two buttons
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 3));
        topRight.setBackground(MAROON);

        JButton pdfBtn = new JButton("Download PDF");
        styleButton(pdfBtn);
        // when clicked, open a save dialog and generate the PDF
        pdfBtn.addActionListener(e -> generatePdf());

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());

        topRight.add(pdfBtn);
        topRight.add(logoutBtn);

        top.add(welcome, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        // request table — added "Message" and "Pick-Up" columns so the PDF has full info
        String[] cols = {"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Student ID", "Submitted"};
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

        // edit panel
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.setBackground(MAROON);
        String[] statuses = {"Pending", "Processing", "Ready for Pick-Up", "Completed", "Rejected"};
        JComboBox<String> statusBox  = new JComboBox<>(statuses);
        JTextField msgField          = new JTextField(18);
        JTextField pickUpField       = new JTextField(16); // format: yyyy-MM-ddTHH:mm
        JButton updateBtn            = new JButton("Update Selected");
        styleButton(updateBtn);

        form.add(label("Status:"));
        form.add(statusBox);
        form.add(label("Message:"));
        form.add(msgField);
        form.add(label("Pick-Up (yyyy-MM-ddTHH:mm):"));
        form.add(pickUpField);
        form.add(updateBtn);

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
            UUID id = rowIds.get(row);
            LocalDateTime pickUp = null;
            String pickUpText = pickUpField.getText().trim();
            if (!pickUpText.isEmpty()) {
                try { pickUp = LocalDateTime.parse(pickUpText); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-ddTHH:mm");
                    return;
                }
            }
            RequestController.update(staff, id, (String) statusBox.getSelectedItem(),
                                     msgField.getText().trim(), pickUp);
            msgField.setText("");
            pickUpField.setText("");
            refreshTable();
        });

        setLayout(new BorderLayout(5, 5));
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);

        refreshTable();
    }

    // fills the Swing table with current request data
    private void refreshTable() {
        tableModel.setRowCount(0); // clear all existing rows
        rowIds.clear();
        int i = 1;
        for (Map.Entry<UUID, Request> entry : RequestController.getFor(staff).entrySet()) {
            Request r = entry.getValue();
            tableModel.addRow(new Object[]{
                i++,
                r.getDocumentType(),
                r.getStatus(),
                r.getReason(),
                r.getMessage()        != null ? r.getMessage()                : "",
                r.getPickUpDateTime() != null ? r.getPickUpDateTime().toString() : "",
                r.getStudentId().toString().substring(0, 8),
                r.getCreatedAt().toString()
            });
            rowIds.add(entry.getKey());
        }
    }

    // builds and saves the PDF report
    private void generatePdf() {

        // open a "Save As" dialog so the user picks the file location
        JFileChooser chooser = new JFileChooser();
        // suggest a default filename with today's date
        chooser.setSelectedFile(new java.io.File("requests_" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf"));

        // showSaveDialog returns an int constant; APPROVE_OPTION means the user clicked Save
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        // getSelectedFile() gives us the File object the user chose
        java.io.File file = chooser.getSelectedFile();

        // make sure the filename ends with .pdf even if the user forgot to type it
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new java.io.File(file.getAbsolutePath() + ".pdf");
        }

        // Document is iText's representation of the whole PDF page
        // PageSize.A4 sets the paper size; you could also use LETTER
        Document doc = new Document(com.itextpdf.text.PageSize.A4);

        try {
            // PdfWriter connects the Document to an output stream
            // every call to doc.add(...) will be written to this file
            PdfWriter.getInstance(doc, new FileOutputStream(file));

            // open() must be called before adding any content
            doc.open();

            // --- fonts ---
            // com.itextpdf.text.Font is used fully qualified here because java.awt.Font
            // is also imported — using the short name "Font" would be ambiguous
            // FontFamily.HELVETICA is a built-in PDF font — no external file needed
            com.itextpdf.text.Font titleFont  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD,  PDF_MAROON);
            com.itextpdf.text.Font subFont    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,  9, com.itextpdf.text.Font.BOLD,  PDF_WHITE);
            com.itextpdf.text.Font cellFont   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,  8, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            // --- title block ---
            Paragraph title = new Paragraph("SmartQueue — Document Requests Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER); // center the text on the page
            doc.add(title);

            // Paragraph with just "\n" adds a blank line for spacing
            doc.add(new Paragraph("\n"));

            // staff info line below the title
            Paragraph info = new Paragraph(
                "Generated by: " + staff.getName() +
                " (" + staff.getDepartment() + ")" +
                "    Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                subFont);
            info.setAlignment(Element.ALIGN_CENTER);
            doc.add(info);

            // total request count
            Paragraph count = new Paragraph(
                "Total Requests: " + tableModel.getRowCount(), subFont);
            count.setAlignment(Element.ALIGN_CENTER);
            doc.add(count);

            doc.add(new Paragraph("\n")); // blank line before the table

            // --- table ---
            // PdfPTable(n) creates a table with n columns
            // we have 7 columns: #, Document Type, Status, Reason, Message, Pick-Up, Submitted
            PdfPTable pdfTable = new PdfPTable(7);

            // setWidthPercentage(100) makes the table stretch to the full page width
            pdfTable.setWidthPercentage(100);

            // setWidths sets the relative width of each column
            // these numbers are proportions, not pixels — they just need to add up to something consistent
            pdfTable.setWidths(new float[]{1f, 3f, 2f, 3f, 3f, 2.5f, 2.5f});

            // --- header row ---
            String[] headers = {"#", "Document Type", "Status", "Reason", "Message", "Pick-Up", "Submitted"};
            for (String h : headers) {
                // PdfPCell wraps a Phrase (inline text with a font)
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(PDF_MAROON);   // maroon background
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                pdfTable.addCell(cell);
            }

            // --- data rows ---
            // loop through the Swing table model rows — same data the screen shows
            for (int row = 0; row < tableModel.getRowCount(); row++) {

                // alternate row background: white and a light gold tint for readability
                BaseColor rowBg = (row % 2 == 0) ? PDF_WHITE : new BaseColor(255, 248, 220);

                // columns in order: #, docType, status, reason, message, pickUp, submitted
                // we skip column index 6 (Student ID) from the screen table — not needed in the report
                int[] colIndexes = {0, 1, 2, 3, 4, 5, 7};
                for (int col : colIndexes) {
                    Object val = tableModel.getValueAt(row, col);
                    String text = val != null ? val.toString() : "";
                    PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(4);
                    pdfTable.addCell(cell);
                }
            }

            // add the completed table to the document
            doc.add(pdfTable);

            // close() finalises the PDF and flushes all bytes to the file
            doc.close();

            JOptionPane.showMessageDialog(this, "PDF saved to:\n" + file.getAbsolutePath());

        } catch (DocumentException | java.io.IOException ex) {
            // DocumentException comes from iText (e.g. bad font, corrupt state)
            // IOException comes from FileOutputStream (e.g. no write permission)
            JOptionPane.showMessageDialog(this,
                "Failed to save PDF:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        AuthController.logout(staff);
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
