package com.magpi.ui;

import com.magpi.model.Measurement;
import com.magpi.model.TestPart;
import com.magpi.model.TestSession;
import com.magpi.ui.table.CustomCellRenderer;
import com.magpi.ui.table.PersistentColorTableModel;
import com.magpi.util.SerialPortManager;
// Video capture feature (VLCJ) temporarily disabled
// import com.magpi.video.VLCJVideoStream;
import com.magpi.util.PersistentLibrary;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

/**
 * Panel that displays the measurement tables
 */
public class TablePage extends JPanel {
    private JTable headshotTable;
    private JTable coilshotTable;
    private PersistentColorTableModel headshotTableModel;
    private PersistentColorTableModel coilshotTableModel;
    private JLabel dateLabel;
    private JLabel startTimeLabel;
    private JLabel endTimeLabel;
    private JLabel parametersLabel;
    private JLabel Part_D;
    private JLabel operatorLabel;
    private TestSession session;
    private SerialPortManager serialPortManager;

    /**
     * Creates a new table page
     * 
     * @param session The test session
     */
    public TablePage(TestSession session) {
        this.session = session;
        initializeComponents();
        setupUI();
        setupSerialPort();
        updateParameters(); // Update parameter display
    }

    private void initializeComponents() {
        String[] columnNames = {
                "Part No", "Current 1", "T 1", "Current 2", "T 2",
                "Current 3", "T 3", "Current 4", "T 4",
                "Current 5", "T 5", "Status"
        };

        // Initialize table models
        headshotTableModel = new PersistentColorTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        coilshotTableModel = new PersistentColorTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        // Initialize tables
        headshotTable = new JTable(headshotTableModel);
        coilshotTable = new JTable(coilshotTableModel);

        // Initialize labels
        dateLabel = new JLabel("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        startTimeLabel = new JLabel(
                "Start Time: " + session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        endTimeLabel = new JLabel("End Time: Not Ended");
        parametersLabel = new JLabel("Parameters: Not Set");
        parametersLabel = new JLabel("Parameters: Not Set");
        Part_D = new JLabel("Part: " + session.getPartDescription());
        operatorLabel = new JLabel("Operator: " + session.getOperatorName());

        // Initialize serial port manager
        serialPortManager = new SerialPortManager();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Set up the header panel with a more modern look
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // First row of header - Date and time info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(createStyledLabel(dateLabel, new Font("Segoe UI", Font.PLAIN, 14)));
        infoPanel.add(createStyledLabel(startTimeLabel, new Font("Segoe UI", Font.PLAIN, 14)));
        infoPanel.add(createStyledLabel(endTimeLabel, new Font("Segoe UI", Font.PLAIN, 14)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridwidth = 3;
        headerPanel.add(infoPanel, gbc);

        // Reset gridwidth
        gbc.gridwidth = 1;

        // Second row of header - Parameters, part info and operator
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        parametersLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(parametersLabel, gbc);

        // Part description and operator name
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        Part_D.setFont(new Font("Segoe UI", Font.BOLD, 14));
        operatorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPanel partOperatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        partOperatorPanel.setOpaque(false);
        partOperatorPanel.add(Part_D);
        partOperatorPanel.add(operatorLabel);
        headerPanel.add(partOperatorPanel, gbc);

        // Button panel in header
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        // Next Part button with modern styling
        JButton addPartButton = new JButton("Next Part");
        styleButton(addPartButton, new Color(41, 128, 185), Color.WHITE);
        addPartButton.addActionListener(e -> addNewPart());
        addPartButton.setPreferredSize(new Dimension(180, 40));

        // Video capture button with modern styling (temporarily disabled)
        // JButton videoStreamButton = new JButton("Capture Video");
        // styleButton(videoStreamButton, new Color(46, 204, 113), Color.WHITE);
        // videoStreamButton.addActionListener(e -> openVideoStream());
        // videoStreamButton.setPreferredSize(new Dimension(150, 40));

        // buttonsPanel.add(rbutton);
        buttonsPanel.add(addPartButton);
        // buttonsPanel.add(videoStreamButton); // disabled
        headerPanel.add(buttonsPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);

        // Set up table panel with more padding and modern styling
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        tablesPanel.setBackground(new Color(230, 230, 230));

        // Headshot table panel
        JPanel headshotPanel = createTablePanel(headshotTable, "Headshot Table");
        tablesPanel.add(headshotPanel);

        // Coilshot table panel
        JPanel coilshotPanel = createTablePanel(coilshotTable, "Coilshot Table");
        tablesPanel.add(coilshotPanel);

        add(tablesPanel, BorderLayout.CENTER);

        // Add action buttons at the bottom with modern styling
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        actionPanel.setBackground(new Color(255, 255, 255));

        JButton endButton = new JButton("End Test");
        styleButton(endButton, new Color(231, 76, 60), Color.WHITE);
        endButton.addActionListener(e -> endTest());
        endButton.setPreferredSize(new Dimension(150, 40));
        actionPanel.add(endButton);

        add(actionPanel, BorderLayout.SOUTH);

        // Set renderers for the tables
        updateTableRenderers();

        // Pre-color the status column cells
        initializeStatusColumn(headshotTable);
        initializeStatusColumn(coilshotTable);
    }

    /**
     * Creates a styled JPanel containing a table with a header
     */
    private JPanel createTablePanel(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Create a styled title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Style the table
        styleTable(table);

        // Create a custom scrollpane with padding and border
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Styles a JLabel with the given font
     */
    private JLabel createStyledLabel(JLabel label, Font font) {
        label.setFont(font);
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    /**
     * Styles a button with the given background and foreground colors
     */
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Pre-colors the status column cells in the table
     */
    private void initializeStatusColumn(JTable table) {
        PersistentColorTableModel model = (PersistentColorTableModel) table.getModel();
        int statusColumn = getStatusColumnIndex(model);

        for (int row = 0; row < model.getRowCount(); row++) {
            // Default color is light gray (neutral)
            model.setCellColor(row, statusColumn, new Color(224, 224, 224));
        }
    }

    private void setupSerialPort() {
        String portName = serialPortManager.detectArduinoPort();
        if (portName == null) {
            JOptionPane.showMessageDialog(this,
                    "No Arduino port detected. Serial communication will not be available.",
                    "Port Not Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!serialPortManager.openConnection(portName)) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open serial port " + portName,
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Start reading data
        serialPortManager.startReading(this::processMeasurement);
    }

    /**
     * Process incoming measurements from the serial port
     * 
     * @param measurement The measurement received
     */
    private void processMeasurement(Measurement measurement) {
        SwingUtilities.invokeLater(() -> {
            // Ignore incoming measurements until a part is explicitly created via "Next
            // Part"
            if (session.getParts().isEmpty()) {
                return;
            }

            // Get the current part (most recent part)
            int currentPartNumber = getCurrentPartNumber();
            TestPart part = session.getPartByNumber(currentPartNumber);
            if (part == null) {
                // Safety check: if no part matches, skip this measurement
                return;
            }

            // Determine shot index before adding
            int shotIndex;
            if ("Headshot".equals(measurement.getMeterType())) {
                shotIndex = part.getHeadshotMeasurements().size();
                part.addHeadshotMeasurement(measurement);
                updateTableWithMeasurement(headshotTableModel, currentPartNumber,
                        measurement.getCurrent(), measurement.getDuration());
                updateStatusColor(headshotTableModel, currentPartNumber);
            } else if ("Coilshot".equals(measurement.getMeterType())) {
                shotIndex = part.getCoilshotMeasurements().size();
                part.addCoilshotMeasurement(measurement);
                updateTableWithMeasurement(coilshotTableModel, currentPartNumber,
                        measurement.getCurrent(), measurement.getDuration());
                updateStatusColor(coilshotTableModel, currentPartNumber);
            } else {
                shotIndex = 0;
            }

            // Persist measurement
            try {
                if (part.getId() != null) {
                    new com.magpi.db.MeasurementDao().insert(
                            part.getId(),
                            measurement.getMeterType(),
                            shotIndex,
                            measurement.getCurrent(),
                            measurement.getDuration());
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Failed to save measurement: " + ex.getMessage(),
                        "Database Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private int getCurrentPartNumber() {
        // Assumes at least one part exists; caller must check
        // session.getParts().isEmpty() first
        return session.getParts().get(session.getParts().size() - 1).getPartNumber();
    }

    private void insertNewPartRow(PersistentColorTableModel tableModel, TestPart part) {
        Object[] rowData = new Object[tableModel.getColumnCount()];
        // Part identifier cell that can display recheck suffix
        rowData[0] = new com.magpi.ui.util.PartIdCell(part.getPartNumber(), part.getRecheckCount());

        int statusCol = getStatusColumnIndex(tableModel);

        // Clear measurement cells
        for (int i = 1; i < statusCol; i++) {
            rowData[i] = "";
        }

        // Default status only (live page)
        rowData[statusCol] = "";

        tableModel.addRow(rowData);
    }

    private void updateTableWithMeasurement(PersistentColorTableModel tableModel,
            int partNumber, double current, double duration) {
        // Find the row for this part
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(partNumber)) {
                // Find the first empty current column
                for (int col = 1; col < tableModel.getColumnCount() - 1; col += 2) {
                    if (tableModel.getValueAt(i, col).equals("")) {
                        tableModel.setValueAt(current, i, col);
                        tableModel.setValueAt(String.format("%.3f", duration), i, col + 1);
                        return;
                    }
                }
            }
        }
    }

    private void updateStatusColor(PersistentColorTableModel tableModel, int partNumber) {
        // Find the row for this part (search from bottom so latest retest row is used)
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if (tableModel.getValueAt(i, 0).equals(partNumber)) {
                int statusCol = getStatusColumnIndex(tableModel);
                boolean hasRedValue = false;
                boolean hasValidValue = false;

                // Check if any current value is red
                for (int col = 1; col < statusCol; col += 2) {
                    Object value = tableModel.getValueAt(i, col);
                    if (value != null && !value.equals("")) {
                        hasValidValue = true;
                        Color color = tableModel.getCellColor(i, col);
                        if (color != null && Color.RED.equals(color)) {
                            hasRedValue = true;
                            break;
                        }
                    }
                }

                // Update status cell background color only (text is controlled by workflow)
                if (hasValidValue) {
                    if (hasRedValue) {
                        tableModel.setCellColor(i, statusCol, Color.RED);
                    } else {
                        tableModel.setCellColor(i, statusCol, Color.GREEN);
                    }
                }

                // Update part status in the model and DB (PASS/ERROR based on measurements
                // only)
                TestPart part = session.getPartByNumber(partNumber);
                if (part != null) {
                    // FIX: Don't overwrite if status is already "Crack" or "Retest"
                    String currentStatus = part.getStatus();
                    if ("Crack".equalsIgnoreCase(currentStatus) || "Retest".equalsIgnoreCase(currentStatus)) {
                        return;
                    }

                    String st = hasRedValue ? "ERROR" : "PASS";
                    part.setStatus(st);
                    if (part.getId() != null) {
                        try {
                            new com.magpi.db.SessionPartDao().updateStatus(part.getId(), st);
                        } catch (Exception ignored) {
                        }
                    }
                }

                return;
            }
        }
    }

    private void addNewPart() {
        // Determine current part and evaluate status before moving on
        if (!session.getParts().isEmpty()) {
            int currentPartNumber = getCurrentPartNumber();
            boolean hasRed = hasAnyRedForPart(headshotTableModel, currentPartNumber) ||
                    hasAnyRedForPart(coilshotTableModel, currentPartNumber);

            if (hasRed) {
                // Any red cell -> mark status as Error with RED background
                applyErrorStatusForPart(headshotTableModel, currentPartNumber);
                applyErrorStatusForPart(coilshotTableModel, currentPartNumber);

                // Update underlying part status and DB
                TestPart part = session.getPartByNumber(currentPartNumber);
                if (part != null) {
                    part.setStatus("ERROR");
                    if (part.getId() != null) {
                        try {
                            new com.magpi.db.SessionPartDao().updateStatus(part.getId(), "ERROR");
                        } catch (Exception ignored) {
                        }
                    }
                }

                // Show popup with only a Retest option
                Object[] options = { "Retest" };
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "Red values detected for Part #" + currentPartNumber + ". Retest?",
                        "Retest Required",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (choice == 0) { // Retest
                    // FIX: Save "Retest" status to DB for the current part (Red case)
                    if (part != null) {
                        part.setStatus("Retest");
                        if (part.getId() != null) {
                            try {
                                new com.magpi.db.SessionPartDao().updateStatus(part.getId(), "Retest");
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    TestPart newRecheckPart = new TestPart(currentPartNumber, session.getPartDescription());
                    newRecheckPart.setRecheckCount(countRechecksFor(currentPartNumber) + 1);
                    session.addPart(newRecheckPart);
                    persistPartIfPossible(newRecheckPart);
                    insertNewPartRow(headshotTableModel, newRecheckPart);
                    insertNewPartRow(coilshotTableModel, newRecheckPart);
                    return;
                }
            } else {
                // All green - ask crack detected, with additional Retest option
                Object[] options = { "Yes", "No", "Retest" };
                int crackOption = JOptionPane.showOptionDialog(
                        this,
                        "Crack detected on Part #" + currentPartNumber + "?",
                        "Crack Status",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);

                TestPart part = session.getPartByNumber(currentPartNumber);

                if (crackOption == 0 || crackOption == 1) { // Yes / No
                    boolean cracksFound = (crackOption == 0);

                    // Update status text over existing GREEN background
                    // Show "Crack" when user explicitly reports a crack, otherwise "Pass".
                    String uiText = cracksFound ? "Crack" : "Pass";
                    setStatusText(headshotTableModel, currentPartNumber, uiText);
                    setStatusText(coilshotTableModel, currentPartNumber, uiText);

                    // Update underlying part status and DB (ERROR/PASS + crack flag)
                    if (part != null) {
                        // FIX: Save "Crack" status directly if cracks found, otherwise "Pass"
                        String st = cracksFound ? "Crack" : "Pass";
                        part.setStatus(st);
                        if (part.getId() != null) {
                            try {
                                com.magpi.db.SessionPartDao dao = new com.magpi.db.SessionPartDao();
                                dao.updateStatus(part.getId(), st);
                                dao.updateCrackDetected(part.getId(), cracksFound);
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    // Optional crack image capture when cracks are found
                    if (part != null && cracksFound) {
                        int captureChoice = JOptionPane.showConfirmDialog(
                                this,
                                "Capture crack image for Part #" + currentPartNumber + " now?",
                                "Capture Crack Image",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if (captureChoice == JOptionPane.YES_OPTION) {
                            java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
                            String imagePath = com.magpi.ui.CrackImageCaptureDialog.captureForPart(owner,
                                    currentPartNumber);
                            if (imagePath != null && !imagePath.trim().isEmpty()) {
                                part.setCrackImagePath(imagePath);
                                if (part.getId() != null) {
                                    try {
                                        new com.magpi.db.SessionPartDao().updateCrackImagePath(part.getId(), imagePath);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                } else if (crackOption == 2) { // Retest
                    // Mark current part as "retest" in status text, keep GREEN color
                    setStatusText(headshotTableModel, currentPartNumber, "Retest");
                    setStatusText(coilshotTableModel, currentPartNumber, "Retest");

                    // FIX: Save "Retest" status to DB for the current part
                    if (part != null) {
                        part.setStatus("Retest");
                        if (part.getId() != null) {
                            try {
                                new com.magpi.db.SessionPartDao().updateStatus(part.getId(), "Retest");
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    // Create a new row for the same part number, with a recheck suffix (e.g. 124-1,
                    // 124-2)
                    TestPart retestPart = new TestPart(currentPartNumber, session.getPartDescription());
                    retestPart.setRecheckCount(countRechecksFor(currentPartNumber) + 1);
                    session.addPart(retestPart);
                    persistPartIfPossible(retestPart);
                    insertNewPartRow(headshotTableModel, retestPart);
                    insertNewPartRow(coilshotTableModel, retestPart);
                    // Skip prompting for a new part number; user will now measure this retest row
                    return;
                }
            }
        }

        // Now prompt for the new part number (re-prompt on invalid/duplicate)
        while (true) {
            String input = JOptionPane.showInputDialog(this,
                    "Enter Part Number:",
                    "New Part",
                    JOptionPane.QUESTION_MESSAGE);

            // User cancelled or closed dialog
            if (input == null || input.trim().isEmpty()) {
                break;
            }

            try {
                int partNumber = Integer.parseInt(input.trim());

                // Check if part already exists in the current session
                if (session.getPartByNumber(partNumber) != null) {
                    JOptionPane.showMessageDialog(this,
                            "Part number " + partNumber + " already exists in this session.",
                            "Duplicate Part",
                            JOptionPane.WARNING_MESSAGE);
                    // Re-prompt
                    continue;
                }

                // Check if part number/description already exists in history (any previous
                // session)
                try {
                    com.magpi.db.SessionPartDao dao = new com.magpi.db.SessionPartDao();
                    if (dao.existsPartNumberForDescription(partNumber, session.getPartDescription())) {
                        JOptionPane.showMessageDialog(this,
                                "Part number " + partNumber + " for part '" + session.getPartDescription()
                                        + "' already exists in history.",
                                "Duplicate Part in History",
                                JOptionPane.WARNING_MESSAGE);
                        // Re-prompt
                        continue;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to check history for duplicate part: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                    // On DB error, stop prompting to avoid infinite loop
                    break;
                }

                // Create new part
                TestPart part = new TestPart(partNumber, session.getPartDescription());
                session.addPart(part);
                persistPartIfPossible(part);

                // Add to tables
                insertNewPartRow(headshotTableModel, part);
                insertNewPartRow(coilshotTableModel, part);

                // Successfully created part, exit loop
                break;

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                // Re-prompt
            }
        }
    }

    /**
     * Ensures a TestPart is persisted to the database and has an ID if possible.
     */
    private void persistPartIfPossible(TestPart part) {
        try {
            if (part != null && part.getId() == null && session.getId() != null) {
                long pid = new com.magpi.db.SessionPartDao().insert(session.getId(), part);
                part.setId(pid);
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to save part: " + ex.getMessage(),
                    "Database Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the status text with crack information while preserving existing
     * color.
     * Searches from bottom so that when there are retests (e.g., 4, 4-1, 4-2),
     * the latest row for that base part number is updated.
     */
    private void updateStatusWithCrackInfo(PersistentColorTableModel tableModel, int partNumber, boolean cracksFound) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if (tableModel.getValueAt(i, 0).equals(partNumber)) {
                int statusCol = getStatusColumnIndex(tableModel);

                // Update the text only; keep whatever background color is already set
                // Show "Crack" when user explicitly reports a crack, otherwise "Pass".
                String statusText = cracksFound ? "Crack" : "Pass";
                tableModel.setValueAt(statusText, i, statusCol);

                // If there is no color yet (e.g. called at endTest), set a sensible default
                if (tableModel.getCellColor(i, statusCol) == null) {
                    tableModel.setCellColor(i, statusCol, cracksFound ? Color.RED : Color.GREEN);
                }
                return;
            }
        }
    }

    private void updateParameters() {
        // Use thresholds already stored in the session (set from LoginPage)
        // Do NOT override them from PersistentLibrary here, otherwise changing
        // parameters in the login dialog would appear to have no effect.

        parametersLabel.setText(String.format("Parameters: Headshot (%.2f), Coilshot (%.2f)",
                session.getHeadShotThreshold(), session.getCoilShotThreshold()));

        // Update table renderers with new thresholds
        updateTableRenderers();
    }

    private void updateTableRenderers() {
        headshotTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getHeadShotThreshold(), headshotTableModel));
        coilshotTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getCoilShotThreshold(), coilshotTableModel));
    }

    private void endTest() {
        // Only ask about cracks for the last part if it exists
        if (!session.getParts().isEmpty()) {
            // Get the last part (this will be the latest retest if any exist)
            TestPart lastPart = session.getParts().get(session.getParts().size() - 1);

            // FIX: Don't overwrite if status is already "Retest" or "Crack"
            String currentStatus = lastPart.getStatus();
            if (!"Retest".equalsIgnoreCase(currentStatus) && !currentStatus.contains("Crack")) {
                int partNumber = lastPart.getPartNumber();

                // NEW REQUIREMENT: Check for red cells first
                boolean hasRed = hasAnyRedForPart(headshotTableModel, partNumber) ||
                        hasAnyRedForPart(coilshotTableModel, partNumber);

                if (hasRed) {
                    // If clicked when any cell is red, don't ask for crack found, just put Error
                    String st = "ERROR";
                    lastPart.setStatus(st);

                    // Update UI text to "Error" so it transfers to History correctly
                    setStatusText(headshotTableModel, partNumber, "Error");
                    setStatusText(coilshotTableModel, partNumber, "Error");

                    if (lastPart.getId() != null) {
                        try {
                            new com.magpi.db.SessionPartDao().updateStatus(lastPart.getId(), st);
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    // No red cells: Ask about cracks (Original Logic)
                    int recheckCount = lastPart.getRecheckCount();
                    String partLabel = (recheckCount > 0)
                            ? (partNumber + "-" + recheckCount)
                            : String.valueOf(partNumber);

                    // Ask if cracks were found
                    int crackOption = JOptionPane.showConfirmDialog(
                            this,
                            "Were any cracks found on Part #" + partLabel + "?",
                            "Crack Status",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    // Update the status cell with the crack information
                    boolean cracksFound = (crackOption == JOptionPane.YES_OPTION);
                    updateStatusWithCrackInfo(headshotTableModel, partNumber, cracksFound);
                    updateStatusWithCrackInfo(coilshotTableModel, partNumber, cracksFound);

                    // Optional crack image capture when ending session and cracks were found
                    if (cracksFound) {
                        int captureChoice = JOptionPane.showConfirmDialog(
                                this,
                                "Capture crack image for Part #" + partLabel + " now?",
                                "Capture Crack Image",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if (captureChoice == JOptionPane.YES_OPTION) {
                            java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
                            String imagePath = com.magpi.ui.CrackImageCaptureDialog.captureForPart(owner, partNumber);
                            if (imagePath != null && !imagePath.trim().isEmpty()) {
                                lastPart.setCrackImagePath(imagePath);
                                if (lastPart.getId() != null) {
                                    try {
                                        new com.magpi.db.SessionPartDao().updateCrackImagePath(lastPart.getId(),
                                                imagePath);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }

                    // Update part status in the model and DB
                    String st = cracksFound ? "Crack" : (hasRed ? "ERROR" : "Pass");
                    lastPart.setStatus(st);
                    if (lastPart.getId() != null) {
                        try {
                            new com.magpi.db.SessionPartDao().updateStatus(lastPart.getId(), st);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        // Transfer all data to history before ending session
        transferCurrentPartsToHistory();

        session.endTest();
        endTimeLabel.setText("End Time: " +
                session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        // Persist session end time
        try {
            if (session.getId() != null) {
                String ts = session.getEndTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                new com.magpi.db.SessionDao().setEndTime(session.getId(), ts);
            }
        } catch (Exception ignored) {
        }

        JOptionPane.showMessageDialog(this,
                "Session ended. All data has been transferred to the history page.\nYou can now view the history and export reports.",
                "Session Ended",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Transfer all existing parts in the table to history
     */
    private void transferCurrentPartsToHistory() {
        // Process headshot table
        transferTableToHistory(headshotTableModel, session.getHistoryPanel().getHeadshotHistoryTableModel());

        // Process coilshot table
        transferTableToHistory(coilshotTableModel, session.getHistoryPanel().getCoilshotHistoryTableModel());

        // Add metadata for each part from the current session
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");
        for (com.magpi.model.TestPart part : session.getParts()) {
            session.getHistoryPanel().addPartMetadata(
                    session.getOperatorName(),
                    session.getSupervisorId(),
                    part.getTestTime().format(formatter),
                    session.getCompanyName(),
                    session.getMachineId(),
                    session.getPartDescription(),
                    session.getHeadShotThreshold(),
                    session.getCoilShotThreshold(),
                    session.getStartTime().format(formatter),
                    session.getEndTime() != null ? session.getEndTime().format(formatter) : "",
                    part.getCrackImagePath());
        }

        // Rebuild the visible Part Test History table from the updated hidden tables
        session.getHistoryPanel().rebuildPartHistoryTable();
    }

    /**
     * Transfer all rows from source table to history table
     */
    private void transferTableToHistory(PersistentColorTableModel sourceModel,
            PersistentColorTableModel historyModel) {
        // Process each row in the source table
        for (int sourceRow = 0; sourceRow < sourceModel.getRowCount(); sourceRow++) {
            // Get the part number for this row (support PartIdCell)
            Object partObj = sourceModel.getValueAt(sourceRow, 0);

            // Skip rows that don't have any data
            boolean hasData = false;
            for (int col = 1; col < sourceModel.getColumnCount() - 1; col += 2) {
                Object value = sourceModel.getValueAt(sourceRow, col);
                if (value != null && !value.toString().isEmpty()) {
                    hasData = true;
                    break;
                }
            }

            if (!hasData) {
                continue;
            }

            // Always append to history (don't check for duplicates)
            // Each row in the table represents a unique test (including retests)
            copyRowToHistoryTable(sourceModel, sourceRow, historyModel);
        }
    }

    /**
     * Updates values in an existing history row
     */
    private void updateHistoryRow(PersistentColorTableModel sourceModel, int sourceRow,
            PersistentColorTableModel historyModel, int historyRow) {
        int srcStatusCol = getStatusColumnIndex(sourceModel);
        int histStatusCol = historyModel.getColumnCount() - 3;
        int histCrackCol = historyModel.getColumnCount() - 2;
        int histDetailsCol = historyModel.getColumnCount() - 1;

        // Part number
        historyModel.setValueAt(sourceModel.getValueAt(sourceRow, 0), historyRow, 0);
        Color c0 = sourceModel.getCellColor(sourceRow, 0);
        if (c0 != null)
            historyModel.setCellColor(historyRow, 0, c0);

        // Measurements
        for (int col = 1; col < srcStatusCol; col++) {
            Object v = sourceModel.getValueAt(sourceRow, col);
            historyModel.setValueAt(v, historyRow, col);
            Color cc = sourceModel.getCellColor(sourceRow, col);
            if (cc != null)
                historyModel.setCellColor(historyRow, col, cc);
        }

        // Status
        Object st = sourceModel.getValueAt(sourceRow, srcStatusCol);
        historyModel.setValueAt(st, historyRow, histStatusCol);
        Color sc = sourceModel.getCellColor(sourceRow, srcStatusCol);
        if (sc != null)
            historyModel.setCellColor(historyRow, histStatusCol, sc);

        // Crack and Details defaults
        historyModel.setValueAt("", historyRow, histCrackCol);
        historyModel.setValueAt("Details", historyRow, histDetailsCol);

        // Fire table changed to update the UI
        historyModel.fireTableRowsUpdated(historyRow, historyRow);
    }

    /**
     * Copies a row from a source table to a history table
     * 
     * @param sourceModel  The source table model
     * @param sourceRow    The row to copy
     * @param historyModel The destination table model
     */
    private void copyRowToHistoryTable(PersistentColorTableModel sourceModel,
            int sourceRow,
            PersistentColorTableModel historyModel) {
        int srcStatusCol = getStatusColumnIndex(sourceModel);
        int histStatusCol = historyModel.getColumnCount() - 3;
        int histCrackCol = historyModel.getColumnCount() - 2;
        int histDetailsCol = historyModel.getColumnCount() - 1;

        Object[] rowData = new Object[historyModel.getColumnCount()];
        // Part number
        rowData[0] = sourceModel.getValueAt(sourceRow, 0);
        // Measurements
        for (int col = 1; col < srcStatusCol; col++) {
            rowData[col] = sourceModel.getValueAt(sourceRow, col);
        }
        // Status
        rowData[histStatusCol] = sourceModel.getValueAt(sourceRow, srcStatusCol);
        // Defaults for new columns
        rowData[histCrackCol] = "";
        rowData[histDetailsCol] = "Details";

        // Add the row to the history table
        historyModel.addRow(rowData);

        // Copy cell colors
        int historyRow = historyModel.getRowCount() - 1;
        // Part number color
        Color c0 = sourceModel.getCellColor(sourceRow, 0);
        if (c0 != null)
            historyModel.setCellColor(historyRow, 0, c0);
        // Measurement colors
        for (int col = 1; col < srcStatusCol; col++) {
            Color cc = sourceModel.getCellColor(sourceRow, col);
            if (cc != null)
                historyModel.setCellColor(historyRow, col, cc);
        }
        // Status color
        Color sc = sourceModel.getCellColor(sourceRow, srcStatusCol);
        if (sc != null)
            historyModel.setCellColor(historyRow, histStatusCol, sc);
    }

    private void styleTable(JTable table) {
        // Set row height and spacing
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setShowGrid(true);
        table.setGridColor(new Color(120, 120, 120)); // Darker grid lines
        table.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        // Style the header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setForeground(new Color(44, 62, 80));
        table.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80); // Part No
        for (int i = 1; i < table.getColumnCount() - 1; i += 2) {
            // Current columns
            table.getColumnModel().getColumn(i).setPreferredWidth(100);
            // Time columns
            table.getColumnModel().getColumn(i + 1).setPreferredWidth(80);
        }
        table.getColumnModel().getColumn(getStatusColumnIndex((PersistentColorTableModel) table.getModel()))
                .setPreferredWidth(100); // Status

        // Prevent column resizing and reordering
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Set default renderer for better number formatting and borders
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                // Add border to each cell
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(180, 180, 180)), // Bottom and right
                                                                                               // borders
                        BorderFactory.createEmptyBorder(2, 5, 2, 5) // Padding
                ));

                // Add left border for first column
                if (column == 0) {
                    c.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(180, 180, 180)),
                            BorderFactory.createEmptyBorder(2, 5, 2, 5)));
                }

                // Format current values (odd columns)
                if (column > 0 && column < table.getColumnCount() - 1 && column % 2 == 1) {
                    if (value instanceof Number) {
                        setText(String.format("%.2f", ((Number) value).doubleValue()));
                    }
                }
                // Format time values (even columns)
                else if (column > 0 && column < table.getColumnCount() - 1 && column % 2 == 0) {
                    if (value instanceof Number) {
                        setText(String.format("%.3f", ((Number) value).doubleValue()));
                    }
                }

                // Center align all cells
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                // Set background color
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }

                return c;
            }
        });
    }

    /**
     * Shuts down the table page and releases resources
     */
    public void shutdown() {
        if (serialPortManager != null) {
            serialPortManager.stopReading();
            serialPortManager.closeConnection();
        }
    }

    private int getStatusColumnIndex(PersistentColorTableModel model) {
        return model.getColumnCount() - 1; // Status is last column in live TablePage
    }

    private boolean hasAnyRedForPart(PersistentColorTableModel model, int partNumber) {
        // Search from bottom so we inspect the latest row for this part (e.g., 3-2 over
        // 3-1 over 3)
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            if (model.getValueAt(i, 0).equals(partNumber)) {
                int statusCol = getStatusColumnIndex(model);
                for (int col = 1; col < statusCol; col += 2) {
                    Color c = model.getCellColor(i, col);
                    if (Color.RED.equals(c))
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Helper to update only the status text for a given part, preserving background
     * color.
     * Searches from bottom so that for repeated retests of the same base part
     * number
     * (e.g., 3, 3-1, 3-2), the latest row gets updated.
     */
    private void setStatusText(PersistentColorTableModel model, int partNumber, String text) {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            if (model.getValueAt(i, 0).equals(partNumber)) {
                int statusCol = getStatusColumnIndex(model);
                model.setValueAt(text, i, statusCol);
                return;
            }
        }
    }

    private void applyErrorStatusForPart(PersistentColorTableModel model, int partNumber) {
        // Apply error to the latest row for this base part number
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            if (model.getValueAt(i, 0).equals(partNumber)) {
                int statusCol = getStatusColumnIndex(model);
                model.setValueAt("Error", i, statusCol);
                model.setCellColor(i, statusCol, Color.RED);
                return;
            }
        }
    }

    private int countRechecksFor(int basePartNumber) {
        int count = 0;
        for (com.magpi.model.TestPart p : session.getParts()) {
            if (p.getPartNumber() == basePartNumber && p.getRecheckCount() > 0) {
                count = Math.max(count, p.getRecheckCount());
            }
        }
        return count;
    }
}
