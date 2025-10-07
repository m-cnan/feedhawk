package ui;

import auth.AuthController;
import auth.AuthController.AuthResult;
import utils.Constants;
import utils.Validator;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class SignupScreen extends JFrame {
    private final AuthController authController;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton signupButton;
    private JButton backToLoginButton;
    private JLabel statusLabel;
    private JLabel passwordStrengthLabel;

    public SignupScreen() {
        this.authController = AuthController.getInstance();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        getContentPane().setBackground(ThemeManager.getBackgroundColor());
        ThemeManager.applyThemeToWindow(this);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Constants.APP_NAME + " - Sign Up");
        setSize(550, 550);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initializeComponents() {
        usernameField = new JTextField(25);
        emailField = new JTextField(25);
        passwordField = new JPasswordField(25);
        confirmPasswordField = new JPasswordField(25);
        signupButton = ThemeManager.createAccentButton("✨ Create Account");
        backToLoginButton = ThemeManager.createThemedButton("← Back to Login");
        statusLabel = new JLabel(" ");
        passwordStrengthLabel = new JLabel(" ");

        ThemeManager.applyTheme(usernameField);
        ThemeManager.applyTheme(emailField);
        ThemeManager.applyTheme(passwordField);
        ThemeManager.applyTheme(confirmPasswordField);

        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        emailField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        confirmPasswordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        signupButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        backToLoginButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        statusLabel.setForeground(ThemeManager.getAccentColor());
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        passwordStrengthLabel.setForeground(ThemeManager.getTextSecondaryColor());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel headerPanel = ThemeManager.createThemedPanel();
        headerPanel.setBorder(new EmptyBorder(30, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Create Your Account");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        titleLabel.setForeground(ThemeManager.getAccentColor());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Join " + Constants.APP_NAME + " today");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(ThemeManager.getTextSecondaryColor());
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        JPanel mainPanel = ThemeManager.createThemedPanel();
        mainPanel.setBorder(new EmptyBorder(20, 50, 30, 50));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(ThemeManager.getTextPrimaryColor());
        usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setForeground(ThemeManager.getTextPrimaryColor());
        emailLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(emailLabel, gbc);

        gbc.gridy = 3;
        gbc.weightx = 1.0;
        mainPanel.add(emailField, gbc);

        gbc.gridy = 4;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(ThemeManager.getTextPrimaryColor());
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(passwordLabel, gbc);

        gbc.gridy = 5;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        gbc.gridy = 6;
        mainPanel.add(passwordStrengthLabel, gbc);

        gbc.gridy = 7;
        gbc.weightx = 0;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setForeground(ThemeManager.getTextPrimaryColor());
        confirmPasswordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(confirmPasswordLabel, gbc);

        gbc.gridy = 8;
        gbc.weightx = 1.0;
        mainPanel.add(confirmPasswordField, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(15, 0, 15, 0);
        mainPanel.add(statusLabel, gbc);

        gbc.gridy = 10;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(signupButton, gbc);

        gbc.gridy = 11;
        mainPanel.add(backToLoginButton, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        signupButton.addActionListener(e -> performSignup());
        backToLoginButton.addActionListener(e -> backToLogin());
        
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updatePasswordStrength();
            }
        });
        
        confirmPasswordField.addActionListener(e -> performSignup());
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        String strength = Validator.getPasswordStrength(password);

        passwordStrengthLabel.setText("Password strength: " + strength);

        switch (strength) {
            case "Very Weak":
                passwordStrengthLabel.setForeground(new Color(220, 53, 69));
                break;
            case "Weak":
                passwordStrengthLabel.setForeground(ThemeManager.getAccentColor());
                break;
            case "Fair":
                passwordStrengthLabel.setForeground(new Color(255, 193, 7));
                break;
            case "Good":
                passwordStrengthLabel.setForeground(new Color(40, 167, 69));
                break;
            case "Strong":
                passwordStrengthLabel.setForeground(new Color(25, 135, 84));
                break;
        }
    }

    private void performSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty()) {
            showError("Please enter a username");
            usernameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showError("Please enter an email address");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter a password");
            passwordField.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            showError("Please confirm your password");
            confirmPasswordField.requestFocus();
            return;
        }

        signupButton.setEnabled(false);
        signupButton.setText("Creating...");
        statusLabel.setText("Creating your account...");
        statusLabel.setForeground(ThemeManager.getTextSecondaryColor());

        SwingWorker<AuthResult, Void> worker = new SwingWorker<AuthResult, Void>() {
            protected AuthResult doInBackground() throws Exception {
                return authController.registerUser(username, email, password, confirmPassword);
            }

            protected void done() {
                try {
                    AuthResult result = get();
                    if (result.isSuccess()) {
                        showSuccess("Account created successfully! Please log in.");
                        Timer timer = new Timer(2000, e -> backToLogin());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showError(result.getMessage());
                    }
                } catch (Exception e) {
                    showError("Signup failed: " + e.getMessage());
                } finally {
                    signupButton.setEnabled(true);
                    signupButton.setText("✨ Create Account");
                }
            }
        };
        worker.execute();
    }

    private void backToLogin() {
        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
            dispose();
        });
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(220, 53, 69));
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(40, 167, 69));
    }
}
