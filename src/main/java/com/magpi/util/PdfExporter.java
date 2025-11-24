package com.magpi.util;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.properties.UnitValue;
import com.magpi.model.TestPart;
import com.magpi.model.TestSession;
import com.magpi.ui.table.PersistentColorTableModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for exporting test data to PDF format
 */
public class PdfExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Exports the current test session to a PDF file
     * 
     * @param session         The test session to export
     * @param headshotTable   The headshot table to include in the PDF
     * @param coilshotTable   The coilshot table to include in the PDF
     * @param parentComponent The parent component for dialog display
     */
    public static void exportToPdf(TestSession session, JTable headshotTable, JTable coilshotTable,
            Component parentComponent) {
        exportToPdf(session, headshotTable, coilshotTable, parentComponent, null, null, null, null, null);
    }

    /**
     * Exports the current test session to a PDF file with optional filter
     * information
     * 
     * @param session         The test session to export
     * @param headshotTable   The headshot table to include in the PDF
     * @param coilshotTable   The coilshot table to include in the PDF
     * @param parentComponent The parent component for dialog display
     * @param filterDateRange Date range filter (e.g., "2025-01-01 to 2025-01-31"),
     *                        null if not used
     * @param filterTimeRange Time range filter (e.g., "09:00:00 to 17:00:00"), null
     *                        if not used
     * @param filterOperator  Operator filter, null if not used
     * @param filterStartDate Start date for display, null if not used
     * @param filterEndDate   End date for display, null if not used
     */
    public static void exportToPdf(TestSession session, JTable headshotTable, JTable coilshotTable,
            Component parentComponent, String filterDateRange, String filterTimeRange, String filterOperator,
            String filterStartDate, String filterEndDate) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        int userSelection = fileChooser.showSaveDialog(parentComponent);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
        }

        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
            // Create PDF document
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            document.add(new Paragraph("Magnetic Particle Inspection Report")
                    .setFontSize(16)
                    .setBold());

            document.add(new Paragraph("\n"));

            // Add metadata section
            document.add(new Paragraph("Report Details")
                    .setFontSize(14)
                    .setBold());

            // Create a table for metadata
            Table metadataTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

            if (session != null) {
                // New order: Company Name, Machine ID, PDF Exported Date, Total Parts, Pass,
                // Crack, Error
                addMetadataRow(metadataTable, "Company Name:", session.getCompanyName());
                addMetadataRow(metadataTable, "Machine ID:", session.getMachineId());

                // Add current date as PDF Exported Date
                String exportDate = java.time.LocalDate.now().format(DATE_FORMATTER);
                addMetadataRow(metadataTable, "PDF Exported Date:", exportDate);

                // Count Pass, Crack, Error, and Retest parts from the actual tables being
                // exported
                int[] counts = countPassErrorParts(headshotTable); // [passCount, crackCount, errorCount]
                int retestedCount = countRetestedParts(headshotTable);
                String totalText = headshotTable.getRowCount() + " (" + retestedCount + " retested)";
                addMetadataRow(metadataTable, "Total Parts Exported:", totalText);
                addMetadataRow(metadataTable, "Pass Parts:", String.valueOf(counts[0]));
                addMetadataRow(metadataTable, "Crack Parts:", String.valueOf(counts[1]));
                addMetadataRow(metadataTable, "Error Parts:", String.valueOf(counts[2]));

                // Add filter information if custom filters were applied
                if (filterDateRange != null || filterTimeRange != null || filterOperator != null) {
                    document.add(metadataTable);
                    document.add(new Paragraph("\n"));

                    // Add filter details section
                    document.add(new Paragraph("Applied Filters")
                            .setFontSize(14)
                            .setBold());
                    Table filterTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

                    if (filterDateRange != null) {
                        addMetadataRow(filterTable, "Date Range:", filterDateRange);
                    }
                    if (filterTimeRange != null) {
                        addMetadataRow(filterTable, "Time Range:", filterTimeRange);
                    }
                    if (filterOperator != null) {
                        addMetadataRow(filterTable, "Operator:", filterOperator);
                    }

                    document.add(filterTable);
                } else {
                    document.add(metadataTable);
                }
            }

            document.add(new Paragraph("\n"));

            // Headshot Table
            if (headshotTable.getRowCount() > 0) {
                document.add(new Paragraph("Headshot Measurements")
                        .setFontSize(14)
                        .setBold());
                Table headTable = createPdfTableWithColors(headshotTable);
                document.add(headTable);
                document.add(new Paragraph("\n"));
            }

            // Coilshot Table
            if (coilshotTable.getRowCount() > 0) {
                document.add(new Paragraph("Coilshot Measurements")
                        .setFontSize(14)
                        .setBold());
                Table coilTable = createPdfTableWithColors(coilshotTable);
                document.add(coilTable);
            }

            document.close();

            JOptionPane.showMessageDialog(parentComponent,
                    "Report exported successfully to " + selectedFile.getName(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent,
                    "Error exporting to PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addMetadataRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)).setBold());
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")));
    }

    /**
     * Count Pass, Crack, and Error parts from the Status column
     * 
     * @param table The table to analyze
     * @return Array with [passCount, crackCount, errorCount]
     */
    private static int[] countPassErrorParts(JTable table) {
        int passCount = 0;
        int crackCount = 0;
        int errorCount = 0;
        javax.swing.table.TableModel model = table.getModel();

        // Find Status column
        int statusCol = -1;
        for (int c = 0; c < model.getColumnCount(); c++) {
            if ("Status".equalsIgnoreCase(model.getColumnName(c))) {
                statusCol = c;
                break;
            }
        }

        if (statusCol >= 0) {
            for (int row = 0; row < model.getRowCount(); row++) {
                Object statusVal = model.getValueAt(row, statusCol);
                if (statusVal != null) {
                    String status = statusVal.toString().toUpperCase();
                    // Check for Crack first (highest priority)
                    if (status.contains("CRACK")) {
                        crackCount++;
                    } else if (status.contains("PASS")) {
                        passCount++;
                    } else if (status.contains("ERROR")) {
                        errorCount++;
                    }
                }
            }
        }

        return new int[] { passCount, crackCount, errorCount };
    }

    /**
     * Count how many parts in the table are retests
     * Handles both PartIdCell objects (with retest suffix) and plain integers from
     * DB
     * 
     * @param table The table to analyze
     * @return Count of retested parts
     */
    private static int countRetestedParts(JTable table) {
        int retestedCount = 0;
        javax.swing.table.TableModel model = table.getModel();

        // Track how many times each base part number appears
        java.util.Map<Integer, Integer> partCounts = new java.util.HashMap<>();

        // Part No is always column 0
        for (int row = 0; row < model.getRowCount(); row++) {
            Object partNo = model.getValueAt(row, 0);
            if (partNo != null) {
                // Handle PartIdCell objects
                if (partNo instanceof com.magpi.ui.util.PartIdCell) {
                    com.magpi.ui.util.PartIdCell cell = (com.magpi.ui.util.PartIdCell) partNo;
                    int basePartNo = cell.getBasePartNumber();
                    int count = partCounts.getOrDefault(basePartNo, 0);
                    partCounts.put(basePartNo, count + 1);

                    // If this is a retest (recheckCount > 0), count it
                    if (cell.getRecheckCount() > 0) {
                        retestedCount++;
                    }
                }
                // Handle plain integers or strings from database
                else {
                    String partStr = partNo.toString();
                    // If part number contains '-', it's a retest (e.g., "5-1", "5-2", "7-1")
                    if (partStr.contains("-")) {
                        retestedCount++;
                        // Extract base part number for counting
                        try {
                            int basePartNo = Integer.parseInt(partStr.substring(0, partStr.indexOf('-')));
                            int count = partCounts.getOrDefault(basePartNo, 0);
                            partCounts.put(basePartNo, count + 1);
                        } catch (NumberFormatException ignored) {
                        }
                    } else {
                        // Plain integer - track occurrences
                        try {
                            int basePartNo = Integer.parseInt(partStr);
                            int count = partCounts.getOrDefault(basePartNo, 0);
                            partCounts.put(basePartNo, count + 1);

                            // If this part number has appeared before, it's a retest
                            if (count > 0) {
                                retestedCount++;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        return retestedCount;
    }

    /**
     * Creates a PDF table with color highlighting from a JTable
     * 
     * @param table The JTable to convert
     * @return A PDF Table representation
     */
    private static Table createPdfTableWithColors(JTable table) {
        javax.swing.table.TableModel model = table.getModel();
        // Build a list of columns to include (skip "Details" and "Crack")
        java.util.List<Integer> cols = new java.util.ArrayList<>();
        for (int c = 0; c < model.getColumnCount(); c++) {
            String name = model.getColumnName(c);
            if (!"Details".equalsIgnoreCase(name) && !"Crack".equalsIgnoreCase(name)) {
                cols.add(c);
            }
        }
        Table pdfTable = new Table(UnitValue.createPercentArray(cols.size())).useAllAvailableWidth();

        // Add headers
        for (int idx : cols) {
            pdfTable.addHeaderCell(new Cell()
                    .add(new Paragraph(model.getColumnName(idx)))
                    .setBold());
        }

        // Add data with colors if available (skip rows with no measurement data)
        for (int row = 0; row < model.getRowCount(); row++) {
            boolean hasData = false;
            // Check non-first columns for any non-empty
            for (int k = 1; k < cols.size(); k++) {
                Object v = model.getValueAt(row, cols.get(k));
                if (v != null && !v.toString().trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (!hasData)
                continue;

            for (int idx : cols) {
                Object value = model.getValueAt(row, idx);
                Cell pdfCell = new Cell();
                pdfCell.add(new Paragraph(value != null ? value.toString() : ""));

                if (model instanceof PersistentColorTableModel) {
                    PersistentColorTableModel colorModel = (PersistentColorTableModel) model;
                    Color cellColor = colorModel.getCellColor(row, idx);
                    if (cellColor != null) {
                        pdfCell.setBackgroundColor(new DeviceRgb(
                                cellColor.getRed(),
                                cellColor.getGreen(),
                                cellColor.getBlue()));
                    }
                }
                pdfTable.addCell(pdfCell);
            }
        }
        return pdfTable;
    }

    /**
     * Export Part Test History summary table (single table view)
     * 
     * @param session         The test session (can be null)
     * @param summaryTable    The Part Test History table to export
     * @param parentComponent The parent component for dialog display
     */
    public static void exportSummaryToPdf(TestSession session, JTable summaryTable, Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Summary PDF Report");
        int userSelection = fileChooser.showSaveDialog(parentComponent);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
        }

        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            document.add(new Paragraph("Part Test History Summary Report")
                    .setFontSize(16)
                    .setBold());

            document.add(new Paragraph("\n"));

            // Add metadata section
            document.add(new Paragraph("Report Details")
                    .setFontSize(14)
                    .setBold());

            Table metadataTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

            if (session != null) {
                // New order: Company Name, Machine ID, Total Parts, Pass, Crack, Error
                addMetadataRow(metadataTable, "Company Name:", session.getCompanyName());
                addMetadataRow(metadataTable, "Machine ID:", session.getMachineId());
            }

            // Count stats from summary table
            int retestedCount = countRetestedParts(summaryTable);
            int[] statusCounts = countSummaryTableStats(summaryTable); // [passCount, crackCount, errorCount]

            String totalText = summaryTable.getRowCount() + " (" + retestedCount + " retested)";
            addMetadataRow(metadataTable, "Total Parts Exported:", totalText);
            addMetadataRow(metadataTable, "Pass Parts:", String.valueOf(statusCounts[0]));
            addMetadataRow(metadataTable, "Crack Parts:", String.valueOf(statusCounts[1]));
            addMetadataRow(metadataTable, "Error Parts:", String.valueOf(statusCounts[2]));

            document.add(metadataTable);
            document.add(new Paragraph("\n"));

            // Add the summary table
            if (summaryTable.getRowCount() > 0) {
                document.add(new Paragraph("Part Test Summary")
                        .setFontSize(14)
                        .setBold());
                Table pdfSummaryTable = createPdfTableWithColors(summaryTable);
                document.add(pdfSummaryTable);
            }

            document.close();

            JOptionPane.showMessageDialog(parentComponent,
                    "Summary report exported successfully to " + selectedFile.getName(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent,
                    "Error exporting to PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Count Pass, Crack, and Error from summary table (Headshot and CoilShot
     * columns)
     * 
     * @param summaryTable The Part Test History table
     * @return Array with [passCount, crackCount, errorCount]
     */
    private static int[] countSummaryTableStats(JTable summaryTable) {
        int passCount = 0;
        int crackCount = 0;
        int errorCount = 0;
        javax.swing.table.TableModel model = summaryTable.getModel();

        // Find Headshot and CoilShot columns (typically columns 1 and 2)
        int headshotCol = -1;
        int coilshotCol = -1;
        for (int c = 0; c < model.getColumnCount(); c++) {
            String name = model.getColumnName(c);
            if ("Headshot".equalsIgnoreCase(name)) {
                headshotCol = c;
            } else if ("CoilShot".equalsIgnoreCase(name)) {
                coilshotCol = c;
            }
        }

        // Count parts where BOTH Headshot and CoilShot are Pass (overall pass)
        // Count parts where either contains "Crack" (overall crack)
        // Count parts where either is Error (overall error)
        for (int row = 0; row < model.getRowCount(); row++) {
            String headStatus = "";
            String coilStatus = "";

            if (headshotCol >= 0) {
                Object val = model.getValueAt(row, headshotCol);
                headStatus = val == null ? "" : val.toString().toUpperCase();
            }
            if (coilshotCol >= 0) {
                Object val = model.getValueAt(row, coilshotCol);
                coilStatus = val == null ? "" : val.toString().toUpperCase();
            }

            // Check for Crack first (highest priority)
            if (headStatus.contains("CRACK") || coilStatus.contains("CRACK")) {
                crackCount++;
            }
            // If either headshot or coilshot is ERROR, count as error
            else if (headStatus.contains("ERROR") || coilStatus.contains("ERROR")) {
                errorCount++;
            }
            // If both are PASS, count as pass
            else if (headStatus.contains("PASS") && coilStatus.contains("PASS")) {
                passCount++;
            }
        }

        return new int[] { passCount, crackCount, errorCount };
    }

    public static void exportPartDetails(java.util.Map<String, String> meta,
            JTable headshotTable, JTable coilshotTable,
            Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Part PDF Report");
        int userSelection = fileChooser.showSaveDialog(parentComponent);
        if (userSelection != JFileChooser.APPROVE_OPTION)
            return;
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
        }
        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Part Inspection Report").setFontSize(16).setBold());
            document.add(new Paragraph("\n"));

            // Metadata
            Table metadataTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            String[] order = new String[] {
                    "Company Name", "Machine ID", "Part Description", "Part No",
                    "Operator", "Supervisor", "Start Time", "End Time",
                    "Headshot Threshold", "Coilshot Threshold", "DeMag Status", "Crack Status" };
            for (String k : order) {
                String v = meta.get(k);
                if (v != null && !v.trim().isEmpty()) {
                    if ("DeMag Status".equals(k) && "Done".equalsIgnoreCase(v)) {
                        addMetadataRowWithColor(metadataTable, k + ":", v, new DeviceRgb(0, 255, 0));
                    } else {
                        addMetadataRow(metadataTable, k + ":", v);
                    }
                }
            }
            document.add(metadataTable);
            document.add(new Paragraph("\n"));

            // Optional crack image from metadata, if available
            String imgPath = metaGet(meta, "Crack Image Path");
            if (imgPath != null && !imgPath.trim().isEmpty()) {
                try {
                    ImageData data = ImageDataFactory.create(imgPath);
                    Image img = new Image(data);
                    img.setAutoScale(true);
                    document.add(new Paragraph("Crack Image").setFontSize(14).setBold());
                    document.add(img);
                    document.add(new Paragraph("\n"));
                } catch (Exception ignored) {
                    // If image fails to load, continue without it
                }
            }

            if (headshotTable.getRowCount() > 0) {
                document.add(new Paragraph("Headshot Measurements").setFontSize(14).setBold());
                document.add(createPdfTableWithColors(headshotTable));
                document.add(new Paragraph("\n"));
            }
            if (coilshotTable.getRowCount() > 0) {
                document.add(new Paragraph("Coilshot Measurements").setFontSize(14).setBold());
                document.add(createPdfTableWithColors(coilshotTable));
            }
            document.close();
            JOptionPane.showMessageDialog(parentComponent,
                    "Report exported successfully to " + selectedFile.getName(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent,
                    "Error exporting to PDF: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addMetadataRowWithColor(Table table, String label, String value,
            com.itextpdf.kernel.colors.Color color) {
        table.addCell(new Cell().add(new Paragraph(label)).setBold());
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")).setBackgroundColor(color));
    }

    private static String metaGet(java.util.Map<String, String> meta, String key) {
        if (meta == null)
            return null;
        String v = meta.get(key);
        return (v == null || v.trim().isEmpty()) ? null : v;
    }
}
