package com.magpi;

import com.magpi.model.TestSession;
import com.magpi.ui.HistoryPage;
import com.magpi.ui.LoginPage;
import com.magpi.ui.TablePage;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main class for the application
 */
public class Main {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private LoginPage loginPage;
    private TablePage tablePage;
    private HistoryPage historyPage;
    private TestSession session;
    private JButton logoutButton;

    // Modern UI Colors
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210); // Modern blue
    private static final Color ACCENT_COLOR = new Color(255, 152, 0); // Orange accent
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray background
    private static final Color TEXT_COLOR = new Color(33, 33, 33); // Dark gray for text
    private static final Color MENU_COLOR = new Color(38, 50, 56); // Dark menu bar color

    /**
     * Creates and initializes the application
     */
    public Main() {
        // Set up custom UI styling
        setupModernUI();

        // Initialize database (creates schema and default admin if needed)
        try {
            com.magpi.db.Database.getInstance().init();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to initialize database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Initialize the session
        session = new TestSession();

        // Set up the main frame
        initializeFrame();

        // Create the panels
        createPanels();

        // Add panels to the tabbed pane
        createTabbedPane();

        // Create the menu bar
        createMenuBar();

        // Show the frame
        frame.setVisible(true);
    }

    /**
     * Sets up modern UI styling for the application
     */
    private void setupModernUI() {
        try {
            // Set system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Apply custom colors and fonts to UI components
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
            UIManager.put("Button.select", PRIMARY_COLOR.darker());
            UIManager.put("Button.border", BorderFactory.createEmptyBorder(8, 12, 8, 12));

            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Label.foreground", TEXT_COLOR);

            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Table.foreground", TEXT_COLOR);
            UIManager.put("Table.gridColor", new Color(220, 220, 220));
            UIManager.put("Table.selectionBackground", PRIMARY_COLOR.brighter());
            UIManager.put("Table.selectionForeground", Color.BLACK);

            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
            UIManager.put("TableHeader.background", new Color(230, 230, 230));
            UIManager.put("TableHeader.foreground", TEXT_COLOR);

            UIManager.put("TabbedPane.selected", PRIMARY_COLOR);
            UIManager.put("TabbedPane.selectedForeground", Color.BLACK);
            UIManager.put("TabbedPane.contentAreaColor", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.light", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.tabAreaBackground", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.unselectedBackground", new Color(230, 230, 230));
            UIManager.put("TabbedPane.borderHightlightColor", PRIMARY_COLOR);
            UIManager.put("TabbedPane.focus", PRIMARY_COLOR);
            UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.PLAIN, 14));

            // Prevent tabs from showing at the corners
            UIManager.put("TabbedPane.tabsOverlapBorder", true);
            UIManager.put("TabbedPane.tabRunOverlay", 0);
            UIManager.put("TabbedPane.tabsOpaque", false);

            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
            UIManager.put("TextField.selectionBackground", PRIMARY_COLOR);
            UIManager.put("TextField.selectionForeground", Color.BLACK);

            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("ComboBox.selectionBackground", PRIMARY_COLOR);
            UIManager.put("ComboBox.selectionForeground", Color.BLACK);

            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.PLAIN, 14));

            // Explicitly set MenuBar properties
            UIManager.put("MenuBar.background", MENU_COLOR);
            UIManager.put("MenuBar.foreground", Color.WHITE);
            UIManager.put("MenuBar.opaque", true);

            // Additional menu-related properties
            UIManager.put("Menu.background", MENU_COLOR);
            UIManager.put("Menu.foreground", Color.WHITE);
            UIManager.put("MenuItem.background", MENU_COLOR);
            UIManager.put("MenuItem.foreground", Color.WHITE);
            UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 14));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeFrame() {
        frame = new JFrame("MAG-Pi: Magnetic Particle Inspection System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Handle the window closing event to clean up resources
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tablePage != null) {
                    tablePage.shutdown();
                }
            }
        });
    }

    private void createPanels() {
        // Create login page
        loginPage = new LoginPage();
        loginPage.addLoginButton(e -> handleLogin());

        // Table page and history page are created after login
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        // Hide the tab headers completely
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0; // Set the tab area height to zero to hide tabs
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

        tabbedPane.addTab("Login", loginPage);
        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar() {
            @Override
            public void updateUI() {
                setUI(new BasicMenuBarUI() {
                    @Override
                    public void paint(Graphics g, JComponent c) {
                        // Fill entire component with menu color
                        g.setColor(MENU_COLOR);
                        g.fillRect(0, 0, c.getWidth(), c.getHeight());
                        super.paint(g, c);
                    }
                });
            }
        };

        // Ensure the menu bar is opaque and has the correct background
        menuBar.setBackground(MENU_COLOR);
        menuBar.setOpaque(true);
        menuBar.setBorderPainted(false);
        menuBar.setBorder(BorderFactory.createEmptyBorder());
        menuBar.setLayout(new BorderLayout());
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // Add Logo and Brand Name (left side)
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setBackground(MENU_COLOR);
        logoPanel.setOpaque(true);

        // Try to load the logo image
        Image logoImage = null;
        try {
            java.io.InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
            if (logoStream != null) {
                logoImage = javax.imageio.ImageIO.read(logoStream);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error loading logo for menubar: " + e.getMessage());
        }

        JLabel logoLabel;
        if (logoImage != null) {
            // Scale the logo to an appropriate size for the menubar
            ImageIcon logoIcon = new ImageIcon(logoImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            logoLabel = new JLabel("MAG-Pi", logoIcon, JLabel.CENTER);
            logoLabel.setForeground(Color.WHITE); // Changed to white for visibility
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        } else {
            // Fallback to text if image can't be loaded
            logoLabel = new JLabel("MAG-Pi");
            logoLabel.setForeground(Color.WHITE); // Changed to white for visibility
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        }

        JLabel companyLabel = new JLabel("Vinze Magnafield Controls");
        companyLabel.setForeground(ACCENT_COLOR);
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        logoPanel.add(logoLabel);
        logoPanel.add(companyLabel);
        menuBar.add(logoPanel, BorderLayout.WEST);

        // Add navigation buttons (right side)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(MENU_COLOR);
        buttonPanel.setOpaque(true);

        // Logout button replaces Home and is only visible after successful login
        logoutButton = createMenuButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        logoutButton.setVisible(false); // Initially hidden until login

        JButton historyButton = createMenuButton("View History");
        historyButton.addActionListener(e -> {
            if (historyPage != null) {
                tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(historyPage));
            }
        });

        JButton aboutButton = createMenuButton("About");
        aboutButton.addActionListener(e -> showAboutDialog());

        buttonPanel.add(logoutButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(aboutButton);

        logoPanel.setBackground(MENU_COLOR);
        buttonPanel.setBackground(MENU_COLOR);

        menuBar.add(logoPanel, BorderLayout.WEST);
        menuBar.add(buttonPanel, BorderLayout.EAST);
        frame.setJMenuBar(menuBar);
    }

    /**
     * Creates a styled button for the menu bar
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(MENU_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(MENU_COLOR.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(MENU_COLOR);
            }
        });

        return button;
    }

    private void handleLogin() {
        // Update session with login info
        if (!loginPage.updateSessionWithLoginInfo(session)) {
            return; // Login validation failed
        }

        // Persist session to DB
        try {
            long sid = new com.magpi.db.SessionDao().insert(session);
            session.setId(sid);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(frame, "Failed to save session: " + ex.getMessage(),
                    "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }

        // Create the table page and history page
        tablePage = new TablePage(session);
        historyPage = new HistoryPage(session);

        // Prevent column resizing in tables
        preventColumnResizing(tablePage);
        preventColumnResizing(historyPage);

        // Add reference to the history page in the session
        session.setHistoryPanel(historyPage);

        // Add pages to the tabbed pane
        tabbedPane.addTab("Table", tablePage);
        tabbedPane.addTab("History", historyPage);

        // Navigate to the table page
        tabbedPane.setSelectedIndex(1);

        // Show logout button now that a user is logged in
        if (logoutButton != null) {
            logoutButton.setVisible(true);
        }
    }

    /**
     * Prevents column resizing in tables
     */
    private void preventColumnResizing(JPanel panel) {
        // Find all JTables in the panel
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                table.getTableHeader().setResizingAllowed(false);
                table.getTableHeader().setReorderingAllowed(false);
            } else if (comp instanceof JScrollPane) {
                Component viewComp = ((JScrollPane) comp).getViewport().getView();
                if (viewComp instanceof JTable) {
                    JTable table = (JTable) viewComp;
                    table.getTableHeader().setResizingAllowed(false);
                    table.getTableHeader().setReorderingAllowed(false);
                }
            } else if (comp instanceof Container) {
                // Recursively search for tables in nested containers
                for (Component child : ((Container) comp).getComponents()) {
                    if (child instanceof JPanel) {
                        preventColumnResizing((JPanel) child);
                    } else if (child instanceof JScrollPane) {
                        Component viewComp = ((JScrollPane) child).getViewport().getView();
                        if (viewComp instanceof JTable) {
                            JTable table = (JTable) viewComp;
                            table.getTableHeader().setResizingAllowed(false);
                            table.getTableHeader().setReorderingAllowed(false);
                        }
                    }
                }
            }
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to logout and end the current session?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Clean up current table page resources (serial ports, etc.)
        if (tablePage != null) {
            tablePage.shutdown();
        }

        // Reset the tabbed pane back to just the login page
        tabbedPane.removeAll();

        // Create a fresh session and login page
        session = new TestSession();
        loginPage = new LoginPage();
        loginPage.addLoginButton(e -> handleLogin());
        tabbedPane.addTab("Login", loginPage);
        tabbedPane.setSelectedIndex(0);

        // Clear references to other pages
        tablePage = null;
        historyPage = null;

        // Hide logout button until the next successful login
        if (logoutButton != null) {
            logoutButton.setVisible(false);
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(frame,
                "MAG-Pi: Magnetic Particle Inspection System\n" +
                        "Version 1.0\n" +
                        "© 2025 Vinze Magnafield Controls\n\n" +
                        "A data acquisition system for magnetic particle inspection.",
                "About MAG-Pi",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Main method to start the application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Create the application on the event dispatch thread
        SwingUtilities.invokeLater(() -> new Main());
    }
}
