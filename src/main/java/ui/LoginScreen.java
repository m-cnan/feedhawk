package ui;

import auth.AuthController;
import auth.AuthController.AuthResult;
import utils.Constants;
import utils.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends JFrame {
    private final AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JLabel statusLabel;
    private JCheckBox rememberMeCheckbox;

    public LoginScreen() {
        this.authController = AuthController.getInstance();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Constants.APP_NAME + " - Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initializeComponents() {
        // Create components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");
        statusLabel = new JLabel(" ");
        rememberMeCheckbox = new JCheckBox("Remember me");

        // Set component properties
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        signupButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Set button colors
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        signupButton.setBackground(new Color(108, 117, 125));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel titleLabel = new JLabel(Constants.APP_NAME);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();

        // Username label and field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(new JLabel("Username or Email:"), gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        // Password label and field
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        // Remember me checkbox
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(rememberMeCheckbox, gbc);

        // Status label
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(statusLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSignupScreen();
            }
        });

        // Enter key support
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showError("Please enter your username or email");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(Color.BLUE);

        // Perform login in background thread
        SwingWorker<AuthResult, Void> worker = new SwingWorker<AuthResult, Void>() {
            @Override
            protected AuthResult doInBackground() throws Exception {
                return authController.loginUser(username, password);
            }

            @Override
            protected void done() {
                try {
                    AuthResult result = get();
                    if (result.isSuccess()) {
                        showSuccess("Login successful!");
                        // Open main window
                        SwingUtilities.invokeLater(() -> {
                            new FeedHawkMainWindow().setVisible(true);
                            dispose();
                        });
                    } else {
                        showError(result.getMessage());
                    }
                } catch (Exception e) {
                    showError("Login failed: " + e.getMessage());
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        worker.execute();
    }

    private void openSignupScreen() {
        SwingUtilities.invokeLater(() -> {
            new SignupScreen().setVisible(true);
            setVisible(false);
        });
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(40, 167, 69));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}
