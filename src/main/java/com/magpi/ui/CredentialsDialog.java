package com.magpi.ui;

import com.magpi.auth.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for username/password authentication and optional registration
 */
public class CredentialsDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean authenticated = false;

    public CredentialsDialog(Frame owner) {
        super(owner, "User Authentication", true);
        setSize(380, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(passwordField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> doRegister());
        cancelBtn.addActionListener(e -> { authenticated = false; dispose(); });

        buttons.add(registerBtn);
        buttons.add(cancelBtn);
        buttons.add(loginBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public boolean showDialog() {
        setVisible(true);
        return authenticated;
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean ok = AuthService.getInstance().authenticate(user, pass);
            if (ok) {
                authenticated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        String user = usernameField.getText().trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a username to register", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JPasswordField pass1 = new JPasswordField();
        JPasswordField pass2 = new JPasswordField();
        Object[] msg = { new JLabel("Password:"), pass1, new JLabel("Confirm Password:"), pass2 };
        int opt = JOptionPane.showConfirmDialog(this, msg, "Register New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String p1 = new String(pass1.getPassword());
            String p2 = new String(pass2.getPassword());
            if (p1.isEmpty() || !p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                AuthService.getInstance().register(user, p1);
                JOptionPane.showMessageDialog(this, "User registered. You can login now.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
