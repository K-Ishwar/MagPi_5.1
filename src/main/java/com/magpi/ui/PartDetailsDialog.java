package com.magpi.ui;

import com.magpi.util.PdfExporter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.magpi.ui.table.PersistentColorTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dialog to show part details and export as PDF.
 * This reads values directly from a selected table row.
 */
public class PartDetailsDialog extends JDialog {
    public PartDetailsDialog(Window owner, String title,
            JTable sourceHeadshotTable, int headRow,
            JTable sourceCoilshotTable, int coilRow,
            java.util.Map<String, String> meta) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel metaPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        metaPanel.setBorder(BorderFactory.createTitledBorder("Part Metadata"));
        String[] keys = new String[] {
                "Company Name", "Machine ID", "Part Description", "Part No",
                "Operator", "Supervisor", "Start Time", "End Time",
                "Headshot Threshold", "Coilshot Threshold", "DeMag Status", "Crack Status" };
        for (String k : keys) {
            if (meta.containsKey(k) && meta.get(k) != null && !meta.get(k).trim().isEmpty()) {
                metaPanel.add(new JLabel(k + ":"));
                metaPanel.add(new JLabel(meta.get(k)));
            }
        }
        add(metaPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel tables = new JPanel(new GridLayout(1, 2, 10, 10));
        JTable headCopy = buildFilteredSingleRowTable(sourceHeadshotTable, headRow);
        JTable coilCopy = buildFilteredSingleRowTable(sourceCoilshotTable, coilRow);
        tables.add(wrapWithTitled(headCopy, "Headshot Measurements"));
        tables.add(wrapWithTitled(coilCopy, "Coilshot Measurements"));
        centerPanel.add(tables, BorderLayout.CENTER);

        // Optional crack image preview on the right side
        String crackImagePath = meta.get("Crack Image Path");
        if (crackImagePath != null && !crackImagePath.trim().isEmpty()) {
            try {
                File imgFile = new File(crackImagePath);
                if (imgFile.exists()) {
                    BufferedImage img = ImageIO.read(imgFile);
                    if (img != null) {
                        int targetWidth = 260;
                        int targetHeight = (int) ((double) img.getHeight() / img.getWidth() * targetWidth);
                        Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        imgLabel.setVerticalAlignment(SwingConstants.TOP);
                        imgLabel.setBorder(BorderFactory.createTitledBorder("Crack Image"));
                        centerPanel.add(imgLabel, BorderLayout.EAST);
                    }
                }
            } catch (Exception ignored) {
                // If image cannot be loaded, silently ignore and show no preview
            }
        }

        add(centerPanel, BorderLayout.CENTER);

        JButton exportBtn = new JButton("Export Part to PDF");
        exportBtn.addActionListener(e -> PdfExporter.exportPartDetails(meta, headCopy, coilCopy, this));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(exportBtn);
        add(south, BorderLayout.SOUTH);
    }

    private JScrollPane wrapWithTitled(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }

    private JTable buildFilteredSingleRowTable(JTable src, int row) {
        if (row < 0 || row >= src.getRowCount()) {
            return new JTable(new DefaultTableModel());
        }
        // Determine indices
        int statusCol = findColumnIndex(src, "Status");
        int crackCol = findColumnIndex(src, "Crack");
        int detailsCol = findColumnIndex(src, "Details");

        java.util.List<String> columns = new java.util.ArrayList<>();
        java.util.List<Object> values = new java.util.ArrayList<>();

        // Part No always
        columns.add(src.getColumnName(0));
        values.add(src.getValueAt(row, 0));

        // Add only measurement pairs that have data
        for (int c = 1; c >= 1 && c < (statusCol >= 0 ? statusCol : src.getColumnCount()); c += 2) {
            Object cur = src.getValueAt(row, c);
            if (cur != null && !cur.toString().trim().isEmpty()) {
                columns.add(src.getColumnName(c));
                values.add(cur);
                // duration column exists c+1
                if (c + 1 < src.getColumnCount()) {
                    Object dur = src.getValueAt(row, c + 1);
                    columns.add(src.getColumnName(c + 1));
                    values.add(dur == null ? "" : dur);
                }
            }
        }
        // Do NOT include Status column (it's in metadata section)
        // Crack if present (history only)
        if (crackCol >= 0) {
            Object crackVal = src.getValueAt(row, crackCol);
            if (crackVal != null && !crackVal.toString().trim().isEmpty()) {
                columns.add(src.getColumnName(crackCol));
                values.add(crackVal);
            }
        }
        // Do NOT include Details column

        // Do NOT include Details column

        PersistentColorTableModel model = new PersistentColorTableModel(columns.toArray(new String[0]), 0);
        model.addRow(values.toArray());

        // Copy colors from source table
        if (src.getModel() instanceof PersistentColorTableModel) {
            PersistentColorTableModel srcModel = (PersistentColorTableModel) src.getModel();
            int newColIndex = 0;

            // Part No (col 0)
            Color c0 = srcModel.getCellColor(row, 0);
            if (c0 != null)
                model.setCellColor(0, newColIndex, c0);
            newColIndex++;

            // Measurements
            for (int c = 1; c >= 1 && c < (statusCol >= 0 ? statusCol : src.getColumnCount()); c += 2) {
                Object cur = src.getValueAt(row, c);
                if (cur != null && !cur.toString().trim().isEmpty()) {
                    Color c1 = srcModel.getCellColor(row, c);
                    if (c1 != null)
                        model.setCellColor(0, newColIndex, c1);
                    newColIndex++;

                    if (c + 1 < src.getColumnCount()) {
                        Color c2 = srcModel.getCellColor(row, c + 1);
                        if (c2 != null)
                            model.setCellColor(0, newColIndex, c2);
                        newColIndex++;
                    }
                }
            }

            // Crack
            if (crackCol >= 0) {
                Object crackVal = src.getValueAt(row, crackCol);
                if (crackVal != null && !crackVal.toString().trim().isEmpty()) {
                    Color cC = srcModel.getCellColor(row, crackCol);
                    if (cC != null)
                        model.setCellColor(0, newColIndex, cC);
                    newColIndex++;
                }
            }
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }

    private int findColumnIndex(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (name.equalsIgnoreCase(table.getColumnName(i)))
                return i;
        }
        return -1;
    }

    public static void show(Window owner,
            JTable headTable, JTable coilTable, int headRow, int coilRow,
            java.util.Map<String, String> meta, String partLabel) {
        meta.put("Part No", partLabel);
        PartDetailsDialog dlg = new PartDetailsDialog(owner,
                "Part Details - " + partLabel, headTable, headRow, coilTable, coilRow, meta);
        dlg.setVisible(true);
    }
}
