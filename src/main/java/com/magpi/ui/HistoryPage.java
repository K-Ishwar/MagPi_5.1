package com.magpi.ui;

import com.magpi.model.TestSession;
import com.magpi.ui.table.CustomCellRenderer;
import com.magpi.ui.table.PersistentColorTableModel;
import com.magpi.util.PdfExporter;
// Video recordings feature temporarily disabled
// import com.magpi.video.RecordedVideosPage;
// import com.magpi.video.VLCJVideoStream;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Panel for displaying historical test data
 */
public class HistoryPage extends JPanel {
    private TestSession session;

    // Visible aggregated table: Part Test History
    private JTable partHistoryTable;
    private PersistentColorTableModel partHistoryTableModel;

    // Hidden measurement tables reused for details dialog and full PDF export
    private JTable headshotHistoryTable;
    private JTable coilshotHistoryTable;
    private PersistentColorTableModel headshotHistoryTableModel;
    private PersistentColorTableModel coilshotHistoryTableModel;

    // private RecordedVideosPage recordedVideosPage; // video feature disabled

    // Per-part metadata aligned to model rows (index matches DB order / hidden
    // tables)
    private java.util.List<java.util.Map<String, Object>> parts = new java.util.ArrayList<>();

    private java.util.List<String> operators = new java.util.ArrayList<>();
    private java.util.List<String> supervisors = new java.util.ArrayList<>();
    private java.util.List<String> dates = new java.util.ArrayList<>();
    private java.util.List<String> companies = new java.util.ArrayList<>();
    private java.util.List<String> machines = new java.util.ArrayList<>();
    private java.util.List<String> partDescriptions = new java.util.ArrayList<>();
    private java.util.List<String> startTimes = new java.util.ArrayList<>();
    private java.util.List<String> endTimes = new java.util.ArrayList<>();
    private java.util.List<Double> headThresholds = new java.util.ArrayList<>();
    private java.util.List<Double> coilThresholds = new java.util.ArrayList<>();
    private java.util.List<String> crackImagePaths = new java.util.ArrayList<>();
    private java.util.List<String> demagStatuses = new java.util.ArrayList<>();

    /**
     * Creates a new history page
     * 
     * @param session The test session
     */
    public HistoryPage(TestSession session) {
        this.session = session;
        initializeComponents();
        setupUI();
    }

    private void initializeComponents() {
        // Visible history table: one row per part
        String[] historyColumns = {
                "Part No", "Headshot", "CoilShot", "DeMag", "Part Description", "Date & Time", "Operator", "Details"
        };
        partHistoryTableModel = new PersistentColorTableModel(historyColumns, 0);
        partHistoryTable = new JTable(partHistoryTableModel);

        // Hidden detailed tables (same structure as live tables, plus Crack/Details)
        String[] measurementColumns = {
                "Part No", "Current 1", "T 1", "Current 2", "T 2",
                "Current 3", "T 3", "Current 4", "T 4",
                "Current 5", "T 5", "DeMag", "Status", "Crack", "Details"
        };
        headshotHistoryTableModel = new PersistentColorTableModel(measurementColumns, 0);
        coilshotHistoryTableModel = new PersistentColorTableModel(measurementColumns, 0);
        headshotHistoryTable = new JTable(headshotHistoryTableModel);
        coilshotHistoryTable = new JTable(coilshotHistoryTableModel);

        // Set custom renderers / styling
        updateTableRenderers();

        // Load all-time history from database
        loadAllHistoryFromDb();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Create header panel with improved styling
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Style the labels in the header (no operator name shown on history page)
        JLabel machineIdLabel = new JLabel("Machine ID: " + session.getMachineId());
        machineIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        machineIdLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(machineIdLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Single Part Test History table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        tablePanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("Part Test History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JScrollPane historyScrollPane = new JScrollPane(partHistoryTable);
        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        historyScrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(titleLabel, BorderLayout.NORTH);
        tablePanel.add(historyScrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // Details click handler for aggregated table
        addDetailsClickListenerForPartTable();

        // Create controls panel with improved styling
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlsPanel.setBackground(new Color(240, 240, 240));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Style search controls
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, new Color(41, 128, 185), Color.WHITE);
        searchButton.addActionListener(e -> performSearch(searchField.getText()));

        // Style filter controls
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JComboBox<String> filterComboBox = new JComboBox<>(new String[] { "All", "Pass", "Crack", "Error", "Retest" });
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.addActionListener(e -> filterResults((String) filterComboBox.getSelectedItem()));

        // Style action buttons
        JButton exportButton = new JButton("Export Detailed PDF");
        styleButton(exportButton, new Color(41, 128, 185), Color.WHITE);
        exportButton.addActionListener(e -> exportToPdf());

        JButton exportSummaryButton = new JButton("Export Summary PDF");
        styleButton(exportSummaryButton, new Color(52, 152, 219), Color.WHITE);
        exportSummaryButton.addActionListener(e -> exportSummaryPdf());

        // JButton viewRecordingsButton = new JButton("View Recordings");
        // styleButton(viewRecordingsButton, new Color(46, 204, 113), Color.WHITE);
        // viewRecordingsButton.addActionListener(e -> viewRecordings());

        JButton backToTableButton = new JButton("Back to Live View");
        styleButton(backToTableButton, new Color(156, 39, 176), Color.WHITE);
        backToTableButton.addActionListener(e -> navigateToTablePage());

        // Add components to controls panel
        controlsPanel.add(searchLabel);
        controlsPanel.add(searchField);
        controlsPanel.add(searchButton);
        controlsPanel.add(filterLabel);
        controlsPanel.add(filterComboBox);
        controlsPanel.add(exportButton);
        controlsPanel.add(exportSummaryButton);
        // controlsPanel.add(viewRecordingsButton); // disabled
        controlsPanel.add(backToTableButton);

        add(controlsPanel, BorderLayout.SOUTH);
    }

    /**
     * Performs a search across both tables
     * 
     * @param searchText The text to search for
     */
    private void performSearch(String searchText) {
        // Create row filter and apply to the aggregated part history table
        RowFilter<Object, Object> filter = null;
        if (searchText.length() > 0) {
            try {
                // Create regex-based filter that searches all columns
                filter = RowFilter.regexFilter("(?i)" + Pattern.quote(searchText));
            } catch (java.util.regex.PatternSyntaxException e) {
                // If an error in the regular expression, just return
                return;
            }
        }

        TableRowSorter<PersistentColorTableModel> sorter = new TableRowSorter<>(partHistoryTableModel);
        sorter.setRowFilter(filter);
        partHistoryTable.setRowSorter(sorter);
    }

    /**
     * Filters the tables by status
     * 
     * @param filterOption The status to filter by
     */
    private void filterResults(String filterOption) {
        RowFilter<Object, Object> filter = null;

        if (!"All".equalsIgnoreCase(filterOption)) {
            // Filter based on Headshot / CoilShot status text (Pass/Crack/Error/Retest)
            final int headCol = 1; // Headshot
            final int coilCol = 2; // CoilShot
            final String wanted = filterOption.toUpperCase();

            filter = new RowFilter<Object, Object>() {
                public boolean include(Entry entry) {
                    String h = String.valueOf(entry.getValue(headCol)).toUpperCase();
                    String c = String.valueOf(entry.getValue(coilCol)).toUpperCase();
                    return h.contains(wanted) || c.contains(wanted);
                }
            };
        }

        TableRowSorter<PersistentColorTableModel> sorter = new TableRowSorter<>(partHistoryTableModel);
        sorter.setRowFilter(filter);
        partHistoryTable.setRowSorter(sorter);
    }

    /**
     * Exports the session data to PDF with filtering options
     */
    private void exportToPdf() {
        showExportOptionsDialog();
    }

    /**
     * Show export options dialog for user to select date range, time range, and
     * operator
     */
    private void showExportOptionsDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Export Options", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Option 1: Export today's data
        JRadioButton todayOption = new JRadioButton("Export Today's Data", true);
        todayOption.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Option 2: Export with date/time range
        JRadioButton customRangeOption = new JRadioButton("Export Custom Date/Time Range");
        customRangeOption.setFont(new Font("Segoe UI", Font.BOLD, 14));

        ButtonGroup optionGroup = new ButtonGroup();
        optionGroup.add(todayOption);
        optionGroup.add(customRangeOption);

        mainPanel.add(todayOption);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(customRangeOption);
        mainPanel.add(Box.createVerticalStrut(15));

        // Date range panel
        JPanel dateRangePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        dateRangePanel.setBorder(BorderFactory.createTitledBorder("Date/Time Range (Optional)"));

        JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
        JTextField startDateField = new JTextField(java.time.LocalDate.now().toString());

        JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
        JTextField endDateField = new JTextField(java.time.LocalDate.now().toString());

        JLabel startTimeLabel = new JLabel("Start Time (HH:MM:SS, optional):");
        JTextField startTimeField = new JTextField("");

        JLabel endTimeLabel = new JLabel("End Time (HH:MM:SS, optional):");
        JTextField endTimeField = new JTextField("");

        dateRangePanel.add(startDateLabel);
        dateRangePanel.add(startDateField);
        dateRangePanel.add(endDateLabel);
        dateRangePanel.add(endDateField);
        dateRangePanel.add(startTimeLabel);
        dateRangePanel.add(startTimeField);
        dateRangePanel.add(endTimeLabel);
        dateRangePanel.add(endTimeField);

        // Initially disable date range fields
        startDateField.setEnabled(false);
        endDateField.setEnabled(false);
        startTimeField.setEnabled(false);
        endTimeField.setEnabled(false);

        // Enable/disable date fields based on selection
        todayOption.addActionListener(e -> {
            startDateField.setEnabled(false);
            endDateField.setEnabled(false);
            startTimeField.setEnabled(false);
            endTimeField.setEnabled(false);
        });

        customRangeOption.addActionListener(e -> {
            startDateField.setEnabled(true);
            endDateField.setEnabled(true);
            startTimeField.setEnabled(true);
            endTimeField.setEnabled(true);
        });

        mainPanel.add(dateRangePanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Operator filter panel
        JPanel operatorPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        operatorPanel.setBorder(BorderFactory.createTitledBorder("Operator Filter (Optional)"));

        JLabel operatorLabel = new JLabel("Operator Name:");

        // Get operators from database and create dropdown
        java.util.List<String> operators = com.magpi.db.Database.getInstance().getAllOperators();
        String[] operatorArray = new String[operators.size() + 1];
        operatorArray[0] = ""; // Empty option for "no filter"
        for (int i = 0; i < operators.size(); i++) {
            operatorArray[i + 1] = operators.get(i);
        }
        JComboBox<String> operatorCombo = new JComboBox<>(operatorArray);
        operatorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        operatorPanel.add(operatorLabel);
        operatorPanel.add(operatorCombo);

        mainPanel.add(operatorPanel);

        dialog.add(mainPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export");
        styleButton(exportBtn, new Color(41, 128, 185), Color.WHITE);
        exportBtn.addActionListener(e -> {
            String selectedOperator = (String) operatorCombo.getSelectedItem();
            performFilteredExport(
                    todayOption.isSelected(),
                    startDateField.getText().trim(),
                    endDateField.getText().trim(),
                    startTimeField.getText().trim(),
                    endTimeField.getText().trim(),
                    selectedOperator != null ? selectedOperator.trim() : "");
            dialog.dispose();
        });

        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn, new Color(150, 150, 150), Color.WHITE);
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(exportBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Perform filtered PDF export based on user's selections
     */
    private void performFilteredExport(boolean isTodayOnly, String startDate, String endDate,
            String startTime, String endTime, String operatorFilter) {
        try {
            // Build column names array from existing model
            int colCount = headshotHistoryTableModel.getColumnCount();
            String[] columnNames = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                columnNames[i] = headshotHistoryTableModel.getColumnName(i);
            }

            // Build filtered hidden tables
            PersistentColorTableModel filteredHeadModel = new PersistentColorTableModel(columnNames, 0);
            PersistentColorTableModel filteredCoilModel = new PersistentColorTableModel(columnNames, 0);

            String today = java.time.LocalDate.now().toString();

            for (int i = 0; i < headshotHistoryTableModel.getRowCount(); i++) {
                // Get date/time/operator for this row
                String rowDate = (i < dates.size() && dates.get(i) != null) ? dates.get(i) : "";
                String rowOperator = (i < operators.size() && operators.get(i) != null) ? operators.get(i) : "";

                // Parse date and time from created_at (format: "YYYY-MM-DD HH:MM:SS")
                String rowDateOnly = rowDate;
                String rowTimeOnly = "";
                int spaceIdx = rowDate.indexOf(' ');
                if (spaceIdx >= 0) {
                    rowDateOnly = rowDate.substring(0, spaceIdx);
                    rowTimeOnly = rowDate.substring(spaceIdx + 1);
                }

                boolean include = true;

                // Apply date filter
                if (isTodayOnly) {
                    if (!rowDateOnly.equals(today)) {
                        include = false;
                    }
                } else {
                    // Custom date range
                    if (!startDate.isEmpty() && rowDateOnly.compareTo(startDate) < 0) {
                        include = false;
                    }
                    if (!endDate.isEmpty() && rowDateOnly.compareTo(endDate) > 0) {
                        include = false;
                    }

                    // Apply time filter if both date and time provided
                    if (include && !startTime.isEmpty() && rowDateOnly.equals(startDate)) {
                        if (rowTimeOnly.compareTo(startTime) < 0) {
                            include = false;
                        }
                    }
                    if (include && !endTime.isEmpty() && rowDateOnly.equals(endDate)) {
                        if (rowTimeOnly.compareTo(endTime) > 0) {
                            include = false;
                        }
                    }
                }

                // Apply operator filter
                if (include && !operatorFilter.isEmpty()) {
                    if (!rowOperator.equalsIgnoreCase(operatorFilter)) {
                        include = false;
                    }
                }

                // If row passes all filters, copy it to filtered models
                if (include) {
                    // Copy headshot row
                    Object[] headRow = new Object[headshotHistoryTableModel.getColumnCount()];
                    for (int col = 0; col < headRow.length; col++) {
                        headRow[col] = headshotHistoryTableModel.getValueAt(i, col);
                    }
                    filteredHeadModel.addRow(headRow);
                    // Copy colors
                    for (int col = 0; col < headRow.length; col++) {
                        Color c = headshotHistoryTableModel.getCellColor(i, col);
                        if (c != null) {
                            filteredHeadModel.setCellColor(filteredHeadModel.getRowCount() - 1, col, c);
                        }
                    }

                    // Copy coilshot row
                    Object[] coilRow = new Object[coilshotHistoryTableModel.getColumnCount()];
                    for (int col = 0; col < coilRow.length; col++) {
                        coilRow[col] = coilshotHistoryTableModel.getValueAt(i, col);
                    }
                    filteredCoilModel.addRow(coilRow);
                    // Copy colors
                    for (int col = 0; col < coilRow.length; col++) {
                        Color c = coilshotHistoryTableModel.getCellColor(i, col);
                        if (c != null) {
                            filteredCoilModel.setCellColor(filteredCoilModel.getRowCount() - 1, col, c);
                        }
                    }
                }
            }

            // Create temporary tables for export
            JTable filteredHeadTable = new JTable(filteredHeadModel);
            JTable filteredCoilTable = new JTable(filteredCoilModel);

            // Export filtered data
            if (filteredHeadModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No data matches the selected filters.",
                        "No Data",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Build filter information strings for PDF display
                String filterDateRange = null;
                String filterTimeRange = null;
                String filterOperatorName = null;

                // Only show filter info if NOT exporting today's data (i.e., custom filters
                // were used)
                if (!isTodayOnly) {
                    // Date range
                    if (!startDate.isEmpty() || !endDate.isEmpty()) {
                        String start = startDate.isEmpty() ? "Any" : startDate;
                        String end = endDate.isEmpty() ? "Any" : endDate;
                        filterDateRange = start + " to " + end;
                    }

                    // Time range
                    if (!startTime.isEmpty() || !endTime.isEmpty()) {
                        String start = startTime.isEmpty() ? "Any" : startTime;
                        String end = endTime.isEmpty() ? "Any" : endTime;
                        filterTimeRange = start + " to " + end;
                    }

                    // Operator filter
                    if (!operatorFilter.isEmpty()) {
                        filterOperatorName = operatorFilter;
                    }
                }

                PdfExporter.exportToPdf(session, filteredHeadTable, filteredCoilTable, this,
                        filterDateRange, filterTimeRange, filterOperatorName, startDate, endDate);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error during export: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Export Part Test History summary table with filtering options
     */
    private void exportSummaryPdf() {
        showSummaryExportOptionsDialog();
    }

    /**
     * Show export options dialog for summary table export
     */
    private void showSummaryExportOptionsDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Export Summary Options", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Option 1: Export today's data
        JRadioButton todayOption = new JRadioButton("Export Today's Data", true);
        todayOption.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Option 2: Export with date/time range
        JRadioButton customRangeOption = new JRadioButton("Export Custom Date/Time Range");
        customRangeOption.setFont(new Font("Segoe UI", Font.BOLD, 14));

        ButtonGroup optionGroup = new ButtonGroup();
        optionGroup.add(todayOption);
        optionGroup.add(customRangeOption);

        mainPanel.add(todayOption);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(customRangeOption);
        mainPanel.add(Box.createVerticalStrut(15));

        // Date range panel
        JPanel dateRangePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        dateRangePanel.setBorder(BorderFactory.createTitledBorder("Date/Time Range (Optional)"));

        JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
        JTextField startDateField = new JTextField(java.time.LocalDate.now().toString());

        JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
        JTextField endDateField = new JTextField(java.time.LocalDate.now().toString());

        JLabel startTimeLabel = new JLabel("Start Time (HH:MM:SS, optional):");
        JTextField startTimeField = new JTextField("");

        JLabel endTimeLabel = new JLabel("End Time (HH:MM:SS, optional):");
        JTextField endTimeField = new JTextField("");

        dateRangePanel.add(startDateLabel);
        dateRangePanel.add(startDateField);
        dateRangePanel.add(endDateLabel);
        dateRangePanel.add(endDateField);
        dateRangePanel.add(startTimeLabel);
        dateRangePanel.add(startTimeField);
        dateRangePanel.add(endTimeLabel);
        dateRangePanel.add(endTimeField);

        // Initially disable date range fields
        startDateField.setEnabled(false);
        endDateField.setEnabled(false);
        startTimeField.setEnabled(false);
        endTimeField.setEnabled(false);

        // Enable/disable date fields based on selection
        todayOption.addActionListener(e -> {
            startDateField.setEnabled(false);
            endDateField.setEnabled(false);
            startTimeField.setEnabled(false);
            endTimeField.setEnabled(false);
        });

        customRangeOption.addActionListener(e -> {
            startDateField.setEnabled(true);
            endDateField.setEnabled(true);
            startTimeField.setEnabled(true);
            endTimeField.setEnabled(true);
        });

        mainPanel.add(dateRangePanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Operator filter panel
        JPanel operatorPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        operatorPanel.setBorder(BorderFactory.createTitledBorder("Operator Filter (Optional)"));

        JLabel operatorLabel = new JLabel("Operator Name:");

        // Get operators from database and create dropdown
        java.util.List<String> operators = com.magpi.db.Database.getInstance().getAllOperators();
        String[] operatorArray = new String[operators.size() + 1];
        operatorArray[0] = ""; // Empty option for "no filter"
        for (int i = 0; i < operators.size(); i++) {
            operatorArray[i + 1] = operators.get(i);
        }
        JComboBox<String> operatorCombo = new JComboBox<>(operatorArray);
        operatorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        operatorPanel.add(operatorLabel);
        operatorPanel.add(operatorCombo);

        mainPanel.add(operatorPanel);

        dialog.add(mainPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export");
        styleButton(exportBtn, new Color(41, 128, 185), Color.WHITE);
        exportBtn.addActionListener(e -> {
            String selectedOperator = (String) operatorCombo.getSelectedItem();
            performSummaryFilteredExport(
                    todayOption.isSelected(),
                    startDateField.getText().trim(),
                    endDateField.getText().trim(),
                    startTimeField.getText().trim(),
                    endTimeField.getText().trim(),
                    selectedOperator != null ? selectedOperator.trim() : "");
            dialog.dispose();
        });

        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn, new Color(150, 150, 150), Color.WHITE);
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(exportBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Perform filtered summary PDF export based on user's selections
     */
    private void performSummaryFilteredExport(boolean isTodayOnly, String startDate, String endDate,
            String startTime, String endTime, String operatorFilter) {
        try {
            // Build filtered Part Test History table with new structure
            String[] summaryColumns = { "Part No", "Headshot", "CoilShot", "DeMag", "Part Description", "Date & Time",
                    "Operator" };
            PersistentColorTableModel filteredSummaryModel = new PersistentColorTableModel(summaryColumns, 0);

            String today = java.time.LocalDate.now().toString();

            for (int i = 0; i < partHistoryTableModel.getRowCount(); i++) {
                // Get date/time/operator from visible table
                // New column structure: 0: Part No, 1: Headshot, 2: CoilShot, 3: DeMag,
                // 4: Part Description, 5: Date & Time, 6: Operator, 7: Details
                String rowDateTime = String.valueOf(partHistoryTableModel.getValueAt(i, 5)); // Date & Time column
                                                                                             // (combined)
                String rowOperator = String.valueOf(partHistoryTableModel.getValueAt(i, 6)); // Operator column
                String rowPartDescription = String.valueOf(partHistoryTableModel.getValueAt(i, 4)); // Part Description

                // Split date and time for filtering
                String rowDate = rowDateTime;
                String rowTime = "";
                int spaceIdx = rowDateTime.indexOf(' ');
                if (spaceIdx >= 0) {
                    rowDate = rowDateTime.substring(0, spaceIdx);
                    rowTime = rowDateTime.substring(spaceIdx + 1);
                }

                boolean include = true;

                // Apply date filter
                if (isTodayOnly) {
                    if (!rowDate.equals(today)) {
                        include = false;
                    }
                } else {
                    // Custom date range
                    if (!startDate.isEmpty() && rowDate.compareTo(startDate) < 0) {
                        include = false;
                    }
                    if (!endDate.isEmpty() && rowDate.compareTo(endDate) > 0) {
                        include = false;
                    }

                    // Apply time filter if both date and time provided
                    if (include && !startTime.isEmpty() && rowDate.equals(startDate)) {
                        if (rowTime.compareTo(startTime) < 0) {
                            include = false;
                        }
                    }
                    if (include && !endTime.isEmpty() && rowDate.equals(endDate)) {
                        if (rowTime.compareTo(endTime) > 0) {
                            include = false;
                        }
                    }
                }

                // Apply operator filter
                if (include && !operatorFilter.isEmpty()) {
                    if (!rowOperator.equalsIgnoreCase(operatorFilter)) {
                        include = false;
                    }
                }

                // If row passes all filters, copy it (exclude Details column)
                if (include) {
                    Object[] row = new Object[summaryColumns.length];
                    row[0] = partHistoryTableModel.getValueAt(i, 0); // Part No
                    row[1] = partHistoryTableModel.getValueAt(i, 1); // Headshot
                    row[2] = partHistoryTableModel.getValueAt(i, 2); // CoilShot
                    row[3] = partHistoryTableModel.getValueAt(i, 3); // DeMag
                    row[4] = rowPartDescription; // Part Description
                    row[5] = rowDateTime; // Date & Time (combined)
                    row[6] = rowOperator; // Operator

                    filteredSummaryModel.addRow(row);

                    // Copy colors for Headshot, CoilShot, and DeMag
                    Color headColor = partHistoryTableModel.getCellColor(i, 1);
                    if (headColor != null) {
                        filteredSummaryModel.setCellColor(filteredSummaryModel.getRowCount() - 1, 1, headColor);
                    }
                    Color coilColor = partHistoryTableModel.getCellColor(i, 2);
                    if (coilColor != null) {
                        filteredSummaryModel.setCellColor(filteredSummaryModel.getRowCount() - 1, 2, coilColor);
                    }
                    Color demagColor = partHistoryTableModel.getCellColor(i, 3);
                    if (demagColor != null) {
                        filteredSummaryModel.setCellColor(filteredSummaryModel.getRowCount() - 1, 3, demagColor);
                    }
                }
            }

            // Export filtered summary data
            if (filteredSummaryModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No data matches the selected filters.",
                        "No Data",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JTable filteredSummaryTable = new JTable(filteredSummaryModel);
                PdfExporter.exportSummaryToPdf(session, filteredSummaryTable, this);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error during export: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Video recordings feature temporarily disabled; keeping method commented for
    // future use
    // private void viewRecordings() {
    // if (recordedVideosPage == null || !recordedVideosPage.isVisible()) {
    // recordedVideosPage = new RecordedVideosPage(VLCJVideoStream.saveLocation);
    // recordedVideosPage.addWindowListener(new java.awt.event.WindowAdapter() {
    // @Override
    // public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    // recordedVideosPage.cleanup();
    // }
    // });
    // recordedVideosPage.setVisible(true);
    // } else {
    // recordedVideosPage.toFront();
    // }
    // }

    private void navigateToTablePage() {
        // Find the parent tabbed pane and switch to the table page
        Container parent = getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }

        if (parent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            // Table page is at index 1 (0=Login, 1=Table, 2=History)
            if (tabbedPane.getTabCount() > 1) {
                tabbedPane.setSelectedIndex(1);
            }
        }
    }

    private void restartApplication() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to end the current process and start a new one?",
                "Confirm Restart",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.dispose();

            // Restart the application
            SwingUtilities.invokeLater(() -> {
                try {
                    // This assumes you have a Main class with a main method
                    // that creates a new application instance
                    Class.forName("com.magpi.Main").getMethod("main", String[].class)
                            .invoke(null, (Object) new String[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Failed to restart the application: " + e.getMessage(),
                            "Restart Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    /**
     * Updates the displayed statistics
     */
    public void updateStatistics() {
        int totalParts = session.getTotalPartsCount();
        int acceptedParts = session.getAcceptedPartsCount();
        int rejectedParts = session.getRejectedPartsCount();
        //
        // totalPartsLabel.setText("Total Parts Tested: " + totalParts);
        // acceptedPartsLabel.setText("Accepted Parts: " + acceptedParts);
        // rejectedPartsLabel.setText("Rejected Parts: " + rejectedParts);
    }

    private void updateTableRenderers() {
        // Hidden detailed tables use the same cell renderer as live tables
        headshotHistoryTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getHeadShotThreshold(), headshotHistoryTableModel));
        coilshotHistoryTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getCoilShotThreshold(), coilshotHistoryTableModel));

        // Improve table appearance
        styleMeasurementTable(headshotHistoryTable);
        styleMeasurementTable(coilshotHistoryTable);

        // Style the visible aggregated table
        stylePartHistoryTable(partHistoryTable);
    }

    /**
     * Applies modern styling to the given table
     */
    private void styleMeasurementTable(JTable table) {
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
        for (int i = 1; i < table.getColumnCount() - 3; i += 2) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100); // Current columns
            table.getColumnModel().getColumn(i + 1).setPreferredWidth(80); // Time columns
        }
        table.getColumnModel().getColumn(getStatusColumnIndex((PersistentColorTableModel) table.getModel()))
                .setPreferredWidth(90); // Status
        table.getColumnModel().getColumn(getCrackColumnIndex((PersistentColorTableModel) table.getModel()))
                .setPreferredWidth(80); // Crack
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setPreferredWidth(90); // Details

        // Prevent column resizing and reordering
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void stylePartHistoryTable(JTable table) {
        table.setRowHeight(28);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 120), 1),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setForeground(new Color(44, 62, 80));

        // Column widths: Part No, Headshot, CoilShot, DeMag, Part Description, Date &
        // Time, Operator, Details
        if (table.getColumnModel().getColumnCount() >= 8) {
            table.getColumnModel().getColumn(0).setPreferredWidth(80); // Part No
            table.getColumnModel().getColumn(1).setPreferredWidth(80); // Headshot
            table.getColumnModel().getColumn(2).setPreferredWidth(80); // CoilShot
            table.getColumnModel().getColumn(3).setPreferredWidth(70); // DeMag
            table.getColumnModel().getColumn(4).setPreferredWidth(150); // Part Description
            table.getColumnModel().getColumn(5).setPreferredWidth(140); // Date & Time
            table.getColumnModel().getColumn(6).setPreferredWidth(100); // Operator
            table.getColumnModel().getColumn(7).setPreferredWidth(80); // Details
        }

        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Use CustomCellRenderer so PASS/FAIL colors saved in the model are shown
        table.setDefaultRenderer(Object.class,
                new CustomCellRenderer(0.0, partHistoryTableModel));

        // Render the Details column to look like a button
        int detailsColIndex = table.getColumnCount() - 1;
        if (detailsColIndex >= 0) {
            table.getColumnModel().getColumn(detailsColIndex)
                    .setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
                        JButton btn = new JButton(value == null ? "Details" : value.toString());
                        // Reuse existing button styling
                        styleButton(btn, new Color(41, 128, 185), Color.WHITE);
                        return btn;
                    });
        }
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Gets the headshot history table model
     * 
     * @return The headshot history table model
     */
    public PersistentColorTableModel getHeadshotHistoryTableModel() {
        return headshotHistoryTableModel;
    }

    /**
     * Gets the coilshot history table model
     * 
     * @return The coilshot history table model
     */
    public PersistentColorTableModel getCoilshotHistoryTableModel() {
        return coilshotHistoryTableModel;
    }

    /**
     * Add metadata for a part (called when transferring from TablePage)
     */
    public void addPartMetadata(String operator, String supervisor, String createdAt,
            String company, String machine, String partDesc,
            double headThreshold, double coilThreshold,
            String startTime, String endTime,
            String crackImagePath, String demagStatus) {
        operators.add(operator);
        supervisors.add(supervisor);
        dates.add(createdAt);
        companies.add(company);
        machines.add(machine);
        partDescriptions.add(partDesc);
        headThresholds.add(headThreshold);
        coilThresholds.add(coilThreshold);
        startTimes.add(startTime);
        endTimes.add(endTime);
        crackImagePaths.add(crackImagePath);
        demagStatuses.add(demagStatus);
    }

    private int getStatusColumnIndex(PersistentColorTableModel model) {
        return model.getColumnCount() - 3;
    }

    private int getCrackColumnIndex(PersistentColorTableModel model) {
        return model.getColumnCount() - 2;
    }

    private void addDetailsClickListenerForPartTable() {
        partHistoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = partHistoryTable.columnAtPoint(e.getPoint());
                int row = partHistoryTable.rowAtPoint(e.getPoint());
                if (col == partHistoryTable.getColumnCount() - 1 && row >= 0) {
                    int modelRow = partHistoryTable.convertRowIndexToModel(row);
                    if (modelRow < 0 || modelRow >= headshotHistoryTableModel.getRowCount())
                        return;

                    Object partLabel = partHistoryTableModel.getValueAt(modelRow, 0);

                    // Build meta map from per-part lists
                    java.util.Map<String, String> meta = new java.util.LinkedHashMap<>();
                    meta.put("Company Name", modelRow < companies.size() ? companies.get(modelRow) : "");
                    meta.put("Machine ID", modelRow < machines.size() ? machines.get(modelRow) : "");
                    meta.put("Part Description",
                            modelRow < partDescriptions.size() ? partDescriptions.get(modelRow) : "");
                    meta.put("Operator", modelRow < operators.size() ? operators.get(modelRow) : "");
                    meta.put("Supervisor", modelRow < supervisors.size() ? supervisors.get(modelRow) : "");
                    meta.put("Start Time", modelRow < startTimes.size() ? startTimes.get(modelRow) : "");
                    meta.put("End Time", modelRow < endTimes.size() ? endTimes.get(modelRow) : "");
                    meta.put("Headshot Threshold",
                            modelRow < headThresholds.size() ? String.valueOf(headThresholds.get(modelRow)) : "");
                    meta.put("Coilshot Threshold",
                            modelRow < coilThresholds.size() ? String.valueOf(coilThresholds.get(modelRow)) : "");
                    meta.put("DeMag Status",
                            modelRow < demagStatuses.size() ? demagStatuses.get(modelRow) : "");

                    // Optional crack image path (from DB or current session)
                    if (modelRow < crackImagePaths.size()) {
                        String imgPath = crackImagePaths.get(modelRow);
                        if (imgPath != null && !imgPath.trim().isEmpty()) {
                            meta.put("Crack Image Path", imgPath);
                        }
                    }

                    // Overall status / crack from hidden headshot table (if present)
                    String status = "";
                    String crack = "";
                    try {
                        int statusIdx = getStatusColumnIndex(headshotHistoryTableModel);
                        status = String.valueOf(headshotHistoryTableModel.getValueAt(modelRow, statusIdx));
                        int crackIdx = getCrackColumnIndex(headshotHistoryTableModel);
                        if (crackIdx >= 0) {
                            crack = String.valueOf(headshotHistoryTableModel.getValueAt(modelRow, crackIdx));
                        }
                    } catch (Exception ignored) {
                    }

                    meta.put("Status", status == null ? "" : status);
                    meta.put("Crack Status", crack == null ? "" : crack);

                    java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(HistoryPage.this);
                    com.magpi.ui.PartDetailsDialog.show(owner,
                            headshotHistoryTable,
                            coilshotHistoryTable,
                            modelRow,
                            modelRow,
                            meta,
                            partLabel == null ? "" : partLabel.toString());
                }
            }
        });
    }

    /**
     * Rebuild the visible Part Test History table from the hidden headshot/coilshot
     * history tables.
     * This should be called after the hidden tables are populated (from DB or from
     * TablePage transfer).
     */
    public void rebuildPartHistoryTable() {
        partHistoryTableModel.setRowCount(0);

        int rowCount = Math.min(headshotHistoryTableModel.getRowCount(), coilshotHistoryTableModel.getRowCount());
        int headStatusCol = getStatusColumnIndex(headshotHistoryTableModel);
        int coilStatusCol = getStatusColumnIndex(coilshotHistoryTableModel);

        // Track how many times each base part number has appeared to build retest
        // suffixes (for DB-loaded data)
        java.util.Map<Integer, Integer> partCounts = new java.util.HashMap<>();

        for (int i = 0; i < rowCount; i++) {
            // Part number from hidden table - use directly if PartIdCell, reconstruct if
            // Integer
            Object partObj = headshotHistoryTableModel.getValueAt(i, 0);
            Object displayPartNo;

            if (partObj instanceof com.magpi.ui.util.PartIdCell) {
                // From Table page transfer - use as-is (already has correct display)
                displayPartNo = partObj;
            } else if (partObj instanceof Integer) {
                // From DB load - reconstruct retest suffix
                int basePartNo = (Integer) partObj;
                int count = partCounts.getOrDefault(basePartNo, 0);
                displayPartNo = (count == 0) ? String.valueOf(basePartNo) : basePartNo + "-" + count;
                partCounts.put(basePartNo, count + 1);
            } else {
                // Fallback for other types
                displayPartNo = (partObj == null) ? "" : partObj.toString();
            }

            // Status text and color from the Status columns of each hidden table
            Object headTextObj = headshotHistoryTableModel.getValueAt(i, headStatusCol);
            Object coilTextObj = coilshotHistoryTableModel.getValueAt(i, coilStatusCol);
            String headStatus = (headTextObj == null) ? "" : formatStatusText(headTextObj.toString());
            String coilStatus = (coilTextObj == null) ? "" : formatStatusText(coilTextObj.toString());

            java.awt.Color headColor = headshotHistoryTableModel.getCellColor(i, headStatusCol);
            java.awt.Color coilColor = coilshotHistoryTableModel.getCellColor(i, coilStatusCol);

            // Date / Time from metadata - combine into single string
            String createdAt = (i < dates.size() && dates.get(i) != null) ? dates.get(i) : "";
            String dateTime = createdAt; // Keep full "YYYY-MM-DD HH:MM:SS" format

            String operator = (i < operators.size() && operators.get(i) != null) ? operators.get(i) : "";
            String demagStatus = (i < demagStatuses.size() && demagStatuses.get(i) != null) ? demagStatuses.get(i) : "";
            String partDescription = (i < partDescriptions.size() && partDescriptions.get(i) != null)
                    ? partDescriptions.get(i)
                    : "";

            Object[] row = new Object[partHistoryTableModel.getColumnCount()];
            row[0] = displayPartNo; // Part No (PartIdCell or String with retest suffix)
            row[1] = headStatus; // Headshot status text (Pass/Error/retest)
            row[2] = coilStatus; // CoilShot status text (Pass/Error/retest)
            row[3] = demagStatus; // DeMag status ("Done")
            row[4] = partDescription; // Part Description
            row[5] = dateTime; // Date & Time combined
            row[6] = operator;
            row[7] = "Details";

            partHistoryTableModel.addRow(row);

            // Set colors to match the Status columns
            if (headColor != null) {
                partHistoryTableModel.setCellColor(i, 1, headColor);
            }
            if (coilColor != null) {
                partHistoryTableModel.setCellColor(i, 2, coilColor);
            }
            // Set green background for DeMag "Done" cells
            if (demagStatus != null && !demagStatus.isEmpty() && "Done".equalsIgnoreCase(demagStatus)) {
                partHistoryTableModel.setCellColor(i, 3, Color.GREEN);
            }
        }
    }

    /**
     * Format status text to match Table page style (Pass/Error/retest instead of
     * PASS/ERROR)
     */
    private String formatStatusText(String status) {
        if (status == null || status.isEmpty())
            return "";

        String upper = status.toUpperCase();
        if ("PASS".equals(upper))
            return "Pass";
        if ("ERROR".equals(upper))
            return "Error";
        if ("RETEST".equals(upper))
            return "retest";

        // Return as-is for other values
        return status;
    }

    private void loadAllHistoryFromDb() {
        // Clear existing rows
        headshotHistoryTableModel.setRowCount(0);
        coilshotHistoryTableModel.setRowCount(0);
        partHistoryTableModel.setRowCount(0);

        operators.clear();
        supervisors.clear();
        dates.clear();
        companies.clear();
        machines.clear();
        partDescriptions.clear();
        startTimes.clear();
        endTimes.clear();
        headThresholds.clear();
        coilThresholds.clear();
        crackImagePaths.clear();
        demagStatuses.clear();
        parts.clear();

        try (java.sql.Connection c = com.magpi.db.Database.getInstance().getConnection()) {
            String baseSql = "SELECT sp.id, sp.part_number, sp.status, " +
                    "CASE WHEN (SELECT COUNT(*) FROM pragma_table_info('session_parts') WHERE name='crack_detected')>0 THEN sp.crack_detected ELSE NULL END AS crack_detected, "
                    +
                    "CASE WHEN (SELECT COUNT(*) FROM pragma_table_info('session_parts') WHERE name='crack_image_path')>0 THEN sp.crack_image_path ELSE NULL END AS crack_image_path, "
                    +
                    "CASE WHEN (SELECT COUNT(*) FROM pragma_table_info('session_parts') WHERE name='demag_status')>0 THEN sp.demag_status ELSE NULL END AS demag_status, "
                    +
                    "s.operator_name, s.supervisor_id, sp.created_at, s.company_name, s.machine_id, s.part_description, s.headshot_threshold, s.coilshot_threshold, s.start_time, s.end_time "
                    +
                    "FROM session_parts sp JOIN sessions s ON s.id = sp.session_id ORDER BY sp.created_at";

            try (java.sql.PreparedStatement ps = c.prepareStatement(baseSql);
                    java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", rs.getLong(1));
                    m.put("part_number", rs.getInt(2));
                    m.put("status", rs.getString(3));
                    m.put("crack_detected", rs.getObject(4));
                    m.put("crack_image_path", rs.getString(5));
                    m.put("demag_status", rs.getString(6));
                    m.put("operator_name", rs.getString(7));
                    m.put("supervisor_id", rs.getString(8));
                    m.put("created_at", rs.getString(9));
                    m.put("company_name", rs.getString(10));
                    m.put("machine_id", rs.getString(11));
                    m.put("part_description", rs.getString(12));
                    m.put("headshot_threshold", rs.getDouble(13));
                    m.put("coilshot_threshold", rs.getDouble(14));
                    m.put("start_time", rs.getString(15));
                    m.put("end_time", rs.getString(16));
                    parts.add(m);

                    operators.add(rs.getString(7));
                    supervisors.add(rs.getString(8));
                    dates.add(rs.getString(9));
                    companies.add(rs.getString(10));
                    machines.add(rs.getString(11));
                    partDescriptions.add(rs.getString(12));
                    headThresholds.add(rs.getDouble(13));
                    coilThresholds.add(rs.getDouble(14));
                    startTimes.add(rs.getString(15));
                    endTimes.add(rs.getString(16));
                    crackImagePaths.add(rs.getString(5));
                    demagStatuses.add(rs.getString(6) != null ? rs.getString(6) : "");
                }
            }

            // Helper to fill one hidden measurement table by meter type
            java.util.function.BiConsumer<PersistentColorTableModel, String> fill = (model, meterType) -> {
                for (java.util.Map<String, Object> p : parts) {
                    Object[] row = new Object[model.getColumnCount()];
                    row[0] = p.get("part_number");
                    int statusCol = getStatusColumnIndex(model);
                    int crackCol = getCrackColumnIndex(model);

                    // Find DeMag column index
                    int demagCol = -1;
                    for (int colIdx = 0; colIdx < model.getColumnCount(); colIdx++) {
                        if ("DeMag".equals(model.getColumnName(colIdx))) {
                            demagCol = colIdx;
                            break;
                        }
                    }

                    for (int i = 1; i < statusCol; i++) {
                        if (i == demagCol) {
                            String ds = (String) p.get("demag_status");
                            row[i] = ds == null ? "" : ds;
                            // Set color immediately (will be applied after adding row)
                        } else {
                            row[i] = "";
                        }
                    }

                    // Determine threshold for this meter type
                    double threshold = "Headshot".equals(meterType)
                            ? (Double) p.get("headshot_threshold")
                            : (Double) p.get("coilshot_threshold");

                    boolean rowHasRed = false;

                    // load up to 5 shots for this meter type
                    try (java.sql.PreparedStatement ps2 = c.prepareStatement(
                            "SELECT shot_index, current, duration FROM measurements WHERE session_part_id=? AND meter_type=? ORDER BY shot_index LIMIT 5")) {
                        ps2.setLong(1, (Long) p.get("id"));
                        ps2.setString(2, meterType);
                        try (java.sql.ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                int idx = rs2.getInt(1);
                                if (idx < 5) {
                                    int col = 1 + idx * 2;
                                    double current = rs2.getDouble(2);
                                    row[col] = current;
                                    row[col + 1] = String.format(java.util.Locale.US, "%.3f", rs2.getDouble(3));

                                    // Determine color based on threshold (same logic as CustomCellRenderer)
                                    // >= threshold is GREEN, < threshold is RED
                                    if (current >= threshold) {
                                        // We can't set cell color here because row isn't added yet,
                                        // but we can track if we found a red value.
                                        // Actually we need to set it after adding row.
                                    } else {
                                        rowHasRed = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception ignore) {
                    }

                    String st = (String) p.get("status");
                    row[statusCol] = st == null ? "" : st;
                    Object cd = p.get("crack_detected");
                    row[crackCol] = (cd == null) ? "" : (((Number) cd).intValue() == 1 ? "Yes" : "No");
                    row[row.length - 1] = "Details";

                    model.addRow(row);
                    int currentRow = model.getRowCount() - 1;

                    // Set colors for measurements
                    // We need to re-iterate or store which cols were red.
                    // Simpler: iterate columns now that row is added.
                    for (int col = 1; col < statusCol; col += 2) {
                        Object val = model.getValueAt(currentRow, col);
                        if (val instanceof Number) {
                            double d = ((Number) val).doubleValue();
                            if (d >= threshold) {
                                model.setCellColor(currentRow, col, java.awt.Color.GREEN);
                            } else {
                                model.setCellColor(currentRow, col, java.awt.Color.RED);
                            }
                        }
                    }

                    // Set DeMag Color
                    if (demagCol != -1) {
                        Object val = model.getValueAt(currentRow, demagCol);
                        if (val != null && "Done".equalsIgnoreCase(val.toString())) {
                            model.setCellColor(currentRow, demagCol, java.awt.Color.GREEN);
                        }
                    }

                    // Set Status Color
                    // Logic: If any Red measurement OR Status is ERROR -> RED
                    // Else -> GREEN
                    // (This preserves Green for "Crack" if measurements were Green)
                    java.awt.Color stColor;
                    if (rowHasRed || "ERROR".equalsIgnoreCase(st)) {
                        stColor = java.awt.Color.RED;
                    } else {
                        stColor = java.awt.Color.GREEN;
                    }

                    model.setCellColor(currentRow, statusCol, stColor);

                    if (cd != null) {
                        model.setCellColor(currentRow, crackCol,
                                ((Number) cd).intValue() == 1 ? java.awt.Color.RED : new java.awt.Color(224, 224, 224));
                    }
                }
            };

            fill.accept(headshotHistoryTableModel, "Headshot");
            fill.accept(coilshotHistoryTableModel, "Coilshot");

        } catch (Exception ex) {
            // Ignore failures to keep app running
        }

        // After loading hidden tables from DB, rebuild the aggregated Part Test History
        // table
        rebuildPartHistoryTable();
    }
}
