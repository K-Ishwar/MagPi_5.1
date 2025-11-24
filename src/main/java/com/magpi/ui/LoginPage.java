package com.magpi.ui;

import com.magpi.model.TestSession;
import com.magpi.util.PersistentLibrary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Panel for user login and session initialization
 */
public class LoginPage extends JPanel {
    private JTextField companyNameField;
    private JTextField machineIdField;
    private JTextField supervisorIdField;
    private JComboBox<String> operatorComboBox;
    private JComboBox<String> partDescriptionComboBox;
    private JTextField headShotThresholdField;
    private JTextField coilShotThresholdField;
    private JList<String> operatorsList;
    private JList<String> partDescriptionList;
    private JList<PersistentLibrary.PartParameters> parametersHistoryList;
    private ActionListener loginAction;
    private Image backgroundImage;
    private Image logoImage;
    private PersistentLibrary library;

    /**
     * Creates a new login panel
     */
    public LoginPage() {
        this.library = PersistentLibrary.getInstance();
        loadImages();
        initializeComponents();
        setupUI();
    }

    private void loadImages() {
        try {
            // Load background image
            InputStream bgStream = getClass().getClassLoader().getResourceAsStream("BackroundImage.png");
            if (bgStream != null) {
                backgroundImage = ImageIO.read(bgStream);
            } else {
                System.err.println("Could not find background image");
            }

            // Load logo image
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
            if (logoStream != null) {
                logoImage = ImageIO.read(logoStream);
            } else {
                System.err.println("Could not find logo image");
            }
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        // Initialize form components
        companyNameField = new JTextField(15);
        machineIdField = new JTextField(15);
        supervisorIdField = new JTextField(15);
        headShotThresholdField = new JTextField("0.0", 8);
        coilShotThresholdField = new JTextField("0.0", 8);

        // Load operators and part descriptions from persistent storage
        operatorComboBox = new JComboBox<>();
        for (String operator : library.getOperators()) {
            operatorComboBox.addItem(operator);
        }
        operatorComboBox.setEditable(true);

        partDescriptionComboBox = new JComboBox<>();
        for (String part : library.getPartDescriptions()) {
            partDescriptionComboBox.addItem(part);
        }
        partDescriptionComboBox.setEditable(true);

        // Create lists
        operatorsList = new JList<>(library.getOperators().toArray(new String[0]));
        partDescriptionList = new JList<>(library.getPartDescriptions().toArray(new String[0]));
        parametersHistoryList = new JList<>();

        // Add selection listeners
        operatorsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && operatorsList.getSelectedValue() != null) {
                operatorComboBox.setSelectedItem(operatorsList.getSelectedValue());
            }
        });

        partDescriptionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && partDescriptionList.getSelectedValue() != null) {
                partDescriptionComboBox.setSelectedItem(partDescriptionList.getSelectedValue());
            }
        });

        parametersHistoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && parametersHistoryList.getSelectedValue() != null) {
                PersistentLibrary.PartParameters selected = parametersHistoryList.getSelectedValue();
                headShotThresholdField.setText(String.format("%.2f", selected.getHeadshotThreshold()));
                coilShotThresholdField.setText(String.format("%.2f", selected.getCoilshotThreshold()));
            }
        });

        // Add listener to load parameters when part description changes
        partDescriptionComboBox.addActionListener(e -> loadParametersForSelectedPart());
    }

    private void loadParametersForSelectedPart() {
        String partDescription = (String) partDescriptionComboBox.getSelectedItem();
        if (partDescription != null && !partDescription.trim().isEmpty()) {
            // Load current parameters
            PersistentLibrary.PartParameters params = library.getPartParameters(partDescription);
            if (params != null) {
                headShotThresholdField.setText(String.format("%.2f", params.getHeadshotThreshold()));
                coilShotThresholdField.setText(String.format("%.2f", params.getCoilshotThreshold()));
            } else {
                // Reset to default if no saved parameters exist
                headShotThresholdField.setText("0.0");
                coilShotThresholdField.setText("0.0");
            }

            // Update parameter history list
            updateParameterHistoryList(partDescription);
        }
    }

    private void updateParameterHistoryList(String partDescription) {
        List<PersistentLibrary.PartParameters> history = library.getPartParameterHistory(partDescription);
        parametersHistoryList.setListData(history.toArray(new PersistentLibrary.PartParameters[0]));
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Add background image panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw background image if loaded, otherwise use gradient as fallback
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback to gradient
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    int w = getWidth();
                    int h = getHeight();
                    Color color1 = new Color(0, 153, 204);
                    Color color2 = new Color(0, 51, 102);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, w, h);
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // Create center panel for company logo and login button
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.insets = new Insets(10, 10, 10, 10);
        centerGbc.fill = GridBagConstraints.HORIZONTAL;
        centerGbc.anchor = GridBagConstraints.CENTER;

        // Add some vertical space before logo
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerPanel.add(Box.createVerticalStrut(80), centerGbc);

        // Add company logo
        JLabel logoLabel;
        if (logoImage != null) {
            // Scale the logo to appropriate size
            ImageIcon logoIcon = new ImageIcon(logoImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            logoLabel = new JLabel(logoIcon, JLabel.CENTER);
        } else {
            // Fallback to text
            logoLabel = new JLabel("VINZE MAGNAFIELD CONTROLS", JLabel.CENTER);
            logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
            logoLabel.setForeground(Color.WHITE);
        }

        // Create styled panel for logo
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(0, 0, 0, 0)); // transparent
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        logoPanel.add(logoLabel, BorderLayout.CENTER);

        // Add logo to center panel
        centerGbc.gridx = 0;
        centerGbc.gridy = 1;
        centerGbc.weightx = 1.0;
        centerPanel.add(logoPanel, centerGbc);

        // Add login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.setBackground(new Color(255, 153, 0));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);

        loginButton.addActionListener(e -> showLoginDialog());

        // Add some vertical space
        centerGbc.gridy = 2;
        centerGbc.insets = new Insets(5, 5, 5, 5);
        centerPanel.add(Box.createVerticalStrut(5), centerGbc);

        // Add login button
        centerGbc.gridy = 3;
        centerGbc.insets = new Insets(10, 10, 10, 10);
        centerPanel.add(loginButton, centerGbc);

        // Add description label
        JLabel descriptionLabel = new JLabel("Magnetic Particle Inspection System", JLabel.CENTER);
        descriptionLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        descriptionLabel.setForeground(Color.WHITE);

        centerGbc.gridy = 4;
        centerPanel.add(descriptionLabel, centerGbc);

        // Add the center panel to the background panel
        backgroundPanel.add(centerPanel);

        // Add the background panel to the main panel
        add(backgroundPanel, BorderLayout.CENTER);
    }

    /**
     * Adds a login button with the provided action listener
     * 
     * @param loginAction The action to perform on login
     */
    public void addLoginButton(ActionListener loginAction) {
        this.loginAction = loginAction;
    }

    private void showLoginDialog() {
        // Create a dialog for login
        JDialog loginDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Login", true);
        loginDialog.setSize(600, 550); // Increased size to better show parameter history
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setLayout(new BorderLayout());

        // Add heading at the top center
        JLabel titleLabel = new JLabel("Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginDialog.add(titleLabel, BorderLayout.NORTH);

        // Create tabbed pane for login and parameters
        JTabbedPane tabbedPane = new JTabbedPane();

        // Hide the visible tab headers (Login Details / Parameters)
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0; // Hide tab area
            }

            @Override
            protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                    Rectangle iconRect, Rectangle textRect) {
                // Do nothing to hide the tabs
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                // Do nothing to hide the tab borders
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Do nothing to hide the content border
            }
        });

        // Create login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add login fields
        // Company Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Company Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        loginPanel.add(companyNameField, gbc);

        // Machine ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(new JLabel("Machine ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        loginPanel.add(machineIdField, gbc);

        // Supervisor ID
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        loginPanel.add(new JLabel("Supervisor ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        loginPanel.add(supervisorIdField, gbc);

        // Operator section
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        loginPanel.add(new JLabel("Operator Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        loginPanel.add(operatorComboBox, gbc);

        // Add operator button
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        JButton addOperatorButton = new JButton("Add New Operator");
        addOperatorButton.addActionListener(e -> addNewOperator());
        loginPanel.add(addOperatorButton, gbc);

        // Part Description section has been moved to the Parameters tab

        // Add to tabbed pane
        tabbedPane.addTab("Login Details", loginPanel);

        // Create parameters panel
        JPanel parametersPanel = new JPanel(new GridBagLayout());
        GridBagConstraints paramGbc = new GridBagConstraints();
        paramGbc.insets = new Insets(15, 15, 15, 15);
        paramGbc.fill = GridBagConstraints.HORIZONTAL;

        // Part Description section (moved from Login Details tab)
        paramGbc.gridx = 0;
        paramGbc.gridy = 0;
        paramGbc.gridwidth = 1;
        parametersPanel.add(new JLabel("Part Description:"), paramGbc);

        paramGbc.gridx = 1;
        paramGbc.gridwidth = 1;
        parametersPanel.add(partDescriptionComboBox, paramGbc);

        // Add part button
        paramGbc.gridx = 1;
        paramGbc.gridy = 1;
        paramGbc.gridwidth = 1;
        paramGbc.fill = GridBagConstraints.HORIZONTAL;
        paramGbc.weightx = 0.0;
        paramGbc.weighty = 0.0;
        JButton addPartButton = new JButton("Add New Part Description");
        addPartButton.addActionListener(e -> addNewPartDescription());
        parametersPanel.add(addPartButton, paramGbc);

        // Headshot Threshold
        paramGbc.gridx = 0;
        paramGbc.gridy = 2;
        paramGbc.gridwidth = 1;
        parametersPanel.add(new JLabel("Headshot Threshold (kA):"), paramGbc);

        paramGbc.gridx = 1;
        paramGbc.gridwidth = 1;
        parametersPanel.add(headShotThresholdField, paramGbc);

        // Coilshot Threshold
        paramGbc.gridx = 0;
        paramGbc.gridy = 3;
        paramGbc.gridwidth = 1;
        parametersPanel.add(new JLabel("Coilshot Threshold (kA):"), paramGbc);

        paramGbc.gridx = 1;
        paramGbc.gridwidth = 1;
        parametersPanel.add(coilShotThresholdField, paramGbc);

        // Save button
        paramGbc.gridx = 0;
        paramGbc.gridy = 4;
        paramGbc.gridwidth = 2;
        JButton saveParametersButton = new JButton("Save Parameters");
        saveParametersButton.addActionListener(e -> saveParameters());
        saveParametersButton.setBackground(new Color(102, 255, 102));
        parametersPanel.add(saveParametersButton, paramGbc);

        // Parameter history list
        paramGbc.gridx = 0;
        paramGbc.gridy = 5;
        paramGbc.gridwidth = 2;
        paramGbc.fill = GridBagConstraints.BOTH;
        paramGbc.weightx = 1.0;
        paramGbc.weighty = 1.0;
        JScrollPane historyScrollPane = new JScrollPane(parametersHistoryList);
        historyScrollPane.setPreferredSize(new Dimension(300, 150));
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Parameter History"));
        parametersPanel.add(historyScrollPane, paramGbc);

        // Add to tabbed pane
        tabbedPane.addTab("Parameters", parametersPanel);

        // Create action panels
        JPanel loginDetailsActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginNextButton = new JButton("Next");
        loginNextButton.addActionListener(e -> {
            if (validateLoginDetails()) {
                tabbedPane.setSelectedIndex(1); // Show Parameters tab
            }
        });
        loginDetailsActionPanel.add(loginNextButton);

        JPanel parametersActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        JButton submitButton = new JButton("Submit");

        backButton.addActionListener(e -> tabbedPane.setSelectedIndex(0)); // Go back to Login Details
        submitButton.addActionListener(e -> {
            if (validateParameters()) {
                loginDialog.dispose();
                if (loginAction != null) {
                    loginAction.actionPerformed(null);
                }
            }
        });

        parametersActionPanel.add(backButton);
        parametersActionPanel.add(submitButton);

        // Card layout for showing different action panels based on selected tab
        final CardLayout cardLayout = new CardLayout();
        final JPanel actionPanelContainer = new JPanel(cardLayout);
        actionPanelContainer.add(loginDetailsActionPanel, "login");
        actionPanelContainer.add(parametersActionPanel, "parameters");

        // Listen for tab changes
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 0) {
                cardLayout.show(actionPanelContainer, "login");
            } else {
                cardLayout.show(actionPanelContainer, "parameters");
            }
        });

        // Initially show login panel actions
        cardLayout.show(actionPanelContainer, "login");

        // Add components to dialog
        loginDialog.add(tabbedPane, BorderLayout.CENTER);
        loginDialog.add(actionPanelContainer, BorderLayout.SOUTH);

        // Prevent user from directly clicking on Parameters tab
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) { // Parameters tab
                if (!validateLoginDetails()) {
                    // Switch back to Login Details if validation fails
                    tabbedPane.setSelectedIndex(0);
                }
            }
        });

        // Show dialog
        loginDialog.setVisible(true);
    }

    private boolean validateLoginDetails() {
        String companyName = companyNameField.getText().trim();
        String machineId = machineIdField.getText().trim();
        String supervisorId = supervisorIdField.getText().trim();
        String operatorName = (String) operatorComboBox.getSelectedItem();

        // Validate required fields for the Login Details tab only
        // (Part Description is now handled/validated on the Parameters tab.)
        if (companyName.isEmpty() || machineId.isEmpty() ||
                supervisorId.isEmpty() || operatorName == null ||
                operatorName.trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please fill all required fields",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean validateParameters() {
        String partDescription = (String) partDescriptionComboBox.getSelectedItem();
        if (partDescription == null || partDescription.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Part Description and save parameters",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double headShotThreshold = Double.parseDouble(headShotThresholdField.getText().trim());
            double coilShotThreshold = Double.parseDouble(coilShotThresholdField.getText().trim());

            if (headShotThreshold < 0 || coilShotThreshold < 0) {
                JOptionPane.showMessageDialog(this,
                        "Threshold values must be non-negative",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid threshold values",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveParameters() {
        try {
            double headShotThreshold = Double.parseDouble(headShotThresholdField.getText().trim());
            double coilShotThreshold = Double.parseDouble(coilShotThresholdField.getText().trim());
            String partDescription = (String) partDescriptionComboBox.getSelectedItem();

            if (partDescription != null && !partDescription.trim().isEmpty()) {
                library.savePartParameters(partDescription, headShotThreshold, coilShotThreshold);

                // Update parameter history list
                updateParameterHistoryList(partDescription);

                JOptionPane.showMessageDialog(this,
                        "Parameters saved for " + partDescription,
                        "Parameters Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid threshold values",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewOperator() {
        String newOperator = JOptionPane.showInputDialog(this, "Enter new operator name:");
        if (newOperator != null && !newOperator.trim().isEmpty()) {
            library.addOperator(newOperator);
            updateOperatorComboBox(newOperator);
            updateOperatorsList();
        }
    }

    private void updateOperatorComboBox(String selectedOperator) {
        operatorComboBox.removeAllItems();
        for (String operator : library.getOperators()) {
            operatorComboBox.addItem(operator);
        }
        if (selectedOperator != null) {
            operatorComboBox.setSelectedItem(selectedOperator);
        }
    }

    private void updateOperatorsList() {
        operatorsList.setListData(library.getOperators().toArray(new String[0]));
    }

    private void addNewPartDescription() {
        String newPartDesc = JOptionPane.showInputDialog(this, "Enter new part description:");
        if (newPartDesc != null && !newPartDesc.trim().isEmpty()) {
            library.addPartDescription(newPartDesc);
            updatePartDescriptionComboBox(newPartDesc);
            updatePartDescriptionList();
        }
    }

    private void updatePartDescriptionComboBox(String selectedPart) {
        partDescriptionComboBox.removeAllItems();
        for (String part : library.getPartDescriptions()) {
            partDescriptionComboBox.addItem(part);
        }
        if (selectedPart != null) {
            partDescriptionComboBox.setSelectedItem(selectedPart);
        }
        // Load parameters for selected part
        loadParametersForSelectedPart();
    }

    private void updatePartDescriptionList() {
        partDescriptionList.setListData(library.getPartDescriptions().toArray(new String[0]));
    }

    /**
     * Updates the session with login information
     * 
     * @param session The session to update
     * @return true if all required fields are filled, false otherwise
     */
    public boolean updateSessionWithLoginInfo(TestSession session) {
        String companyName = companyNameField.getText().trim();
        String machineId = machineIdField.getText().trim();
        String supervisorId = supervisorIdField.getText().trim();
        String operatorName = (String) operatorComboBox.getSelectedItem();
        String partDescription = (String) partDescriptionComboBox.getSelectedItem();

        // Save new operator and part description if they don't exist
        if (operatorName != null && !operatorName.trim().isEmpty() && !library.getOperators().contains(operatorName)) {
            library.addOperator(operatorName);
            updateOperatorsList();
        }

        if (partDescription != null && !partDescription.trim().isEmpty()
                && !library.getPartDescriptions().contains(partDescription)) {
            library.addPartDescription(partDescription);
            updatePartDescriptionList();
        }

        // Update session
        session.setCompanyName(companyName);
        session.setMachineId(machineId);
        session.setSupervisorId(supervisorId);
        session.setOperatorName(operatorName);
        session.setPartDescription(partDescription);

        // Update threshold parameters
        try {
            double headShotThreshold = Double.parseDouble(headShotThresholdField.getText().trim());
            double coilShotThreshold = Double.parseDouble(coilShotThresholdField.getText().trim());
            session.setHeadShotThreshold(headShotThreshold);
            session.setCoilShotThreshold(coilShotThreshold);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}