package ui;

import auth.AuthController;
import auth.AuthController.AuthResult;
import utils.Constants;
import utils.Validator;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        
        // Apply dark theme
        getContentPane().setBackground(ThemeManager.getBackgroundColor());
        ThemeManager.applyThemeToWindow(this);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Constants.APP_NAME + " - Login");
        setSize(500, 500); // Increased height from 400 to 500
        setLocationRelativeTo(null);
        setResizable(true); // Made resizable so users can adjust if needed
    }

    private void initializeComponents() {
        // Create components with dark theme
        usernameField = new JTextField(25);
        passwordField = new JPasswordField(25);
        loginButton = ThemeManager.createAccentButton("🔐 Login");
        signupButton = ThemeManager.createThemedButton("Create Account");
        statusLabel = new JLabel(" ");
        rememberMeCheckbox = new JCheckBox("Remember me");

        // Apply theme to components
        ThemeManager.applyTheme(usernameField);
        ThemeManager.applyTheme(passwordField);
        ThemeManager.applyTheme(rememberMeCheckbox);
        
        // Set component properties
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        loginButton.setFocusable(true);
        loginButton.setRequestFocusEnabled(true);
        signupButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        signupButton.setFocusable(true);
        statusLabel.setForeground(ThemeManager.getAccentColor());
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel with dark theme
        JPanel headerPanel = ThemeManager.createThemedPanel();
        headerPanel.setBorder(new EmptyBorder(30, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(Constants.APP_NAME);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        titleLabel.setForeground(ThemeManager.getAccentColor());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("RSS Feed Reader");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(ThemeManager.getTextSecondaryColor());
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        // Main panel with dark theme
        JPanel mainPanel = ThemeManager.createThemedPanel();
        mainPanel.setBorder(new EmptyBorder(15, 50, 20, 50)); // Reduced top padding
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0); // Reduced vertical spacing

        // Username label and field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username or Email");
        usernameLabel.setForeground(ThemeManager.getTextPrimaryColor());
        usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        // Password label and field
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(ThemeManager.getTextPrimaryColor());
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        // Remember me checkbox
        gbc.gridy = 4;
        gbc.weightx = 0;
        mainPanel.add(rememberMeCheckbox, gbc);

        // Status label
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 10, 0); // Reduced spacing
        mainPanel.add(statusLabel, gbc);

        // Login button
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(loginButton, gbc);

        // Signup button with different styling to make it more visible
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 0, 10, 0); // Added more spacing for signup button
        signupButton.setPreferredSize(new Dimension(200, 35)); // Ensure minimum size
        mainPanel.add(signupButton, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // Add a bottom spacer panel to ensure signup button is visible
        JPanel bottomPanel = ThemeManager.createThemedPanel();
        bottomPanel.setPreferredSize(new Dimension(500, 20));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        // Set up button action listeners
        loginButton.addActionListener(e -> performLogin());
        signupButton.addActionListener(e -> openSignupScreen());
        
        // Add Enter key support
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> performLogin());
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
                        // Open streamlined main window
                        SwingUtilities.invokeLater(() -> {
                            new StreamlinedMainWindow().setVisible(true);
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
