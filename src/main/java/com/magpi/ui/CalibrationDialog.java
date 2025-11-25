package com.magpi.ui;

import com.magpi.db.CalibrationDao;
import com.magpi.model.CalibrationLog;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for system calibration data entry
 */
public class CalibrationDialog extends JDialog {
    private JLabel dateLabel;
    private JTextField machineCalibrationField;
    private JTextField blackLightIntensityField;
    private JTextField magneticBathConcentrationField;
    private JComboBox<String> pieGaugeComboBox;
    private boolean calibrationSaved = false;

    public CalibrationDialog(Frame owner) {
        super(owner, "System Calibration", true);
        initializeComponents();
        setupUI();
        setSize(450, 400);
        setLocationRelativeTo(owner);
    }

    private void initializeComponents() {
        // Date label - showing today's date
        dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Numeric input fields
        machineCalibrationField = new JTextField(15);
        blackLightIntensityField = new JTextField(15);
        magneticBathConcentrationField = new JTextField(15);

        // Pie Gauge status selector
        pieGaugeComboBox = new JComboBox<>(new String[] { "OK", "Not OK" });
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Main panel with form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(dateLabel, gbc);

        // Machine Calibration Dvcon
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Machine Calibration Dvcon (µW/cm²):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(machineCalibrationField, gbc);

        // Black Light Intensity
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Black Light Intensity (µW/cm²):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(blackLightIntensityField, gbc);

        // Magnetic Bath Concentration
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Magnetic Bath Concentration (2-3 Div):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(magneticBathConcentrationField, gbc);

        // Pie Gauge Status
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Pie Gauge Status:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(pieGaugeComboBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveCalibration());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(new Color(149, 165, 166));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveCalibration() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Parse input values
            double machineCalibration = Double.parseDouble(machineCalibrationField.getText().trim());
            double blackLightIntensity = Double.parseDouble(blackLightIntensityField.getText().trim());
            String magneticBathConcentration = magneticBathConcentrationField.getText().trim();
            boolean pieGaugeStatus = "OK".equals(pieGaugeComboBox.getSelectedItem());

            // Create calibration log
            CalibrationLog log = new CalibrationLog(
                    LocalDate.now(),
                    machineCalibration,
                    blackLightIntensity,
                    magneticBathConcentration,
                    pieGaugeStatus);

            // Save to database
            CalibrationDao dao = new CalibrationDao();
            dao.insert(log);

            calibrationSaved = true;

            JOptionPane.showMessageDialog(this,
                    "Calibration data saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for calibration fields.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save calibration data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInputs() {
        // Check if all fields are filled
        if (machineCalibrationField.getText().trim().isEmpty() ||
                blackLightIntensityField.getText().trim().isEmpty() ||
                magneticBathConcentrationField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please fill in all calibration fields.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate numeric fields
        try {
            double machineCalibration = Double.parseDouble(machineCalibrationField.getText().trim());
            double blackLightIntensity = Double.parseDouble(blackLightIntensityField.getText().trim());

            if (machineCalibration < 0 || blackLightIntensity < 0) {
                JOptionPane.showMessageDialog(this,
                        "Calibration values must be non-negative.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for Machine Calibration and Black Light Intensity.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate magnetic bath concentration (optional: could add range validation)
        String concentration = magneticBathConcentrationField.getText().trim();
        if (concentration.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter Magnetic Bath Concentration.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Check if calibration was successfully saved
     * 
     * @return true if calibration was saved, false otherwise
     */
    public boolean isCalibrationSaved() {
        return calibrationSaved;
    }

    /**
     * Show the calibration dialog
     * 
     * @param owner The parent frame
     * @return true if calibration was saved, false otherwise
     */
    public static boolean showDialog(Frame owner) {
        CalibrationDialog dialog = new CalibrationDialog(owner);
        dialog.setVisible(true);
        return dialog.isCalibrationSaved();
    }
}
