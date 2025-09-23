package ui;

import auth.AuthController;
import auth.AuthController.AuthResult;
import utils.Constants;
import utils.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Constants.APP_NAME + " - Sign Up");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initializeComponents() {
        // Create components
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        signupButton = new JButton("Create Account");
        backToLoginButton = new JButton("Back to Login");
        statusLabel = new JLabel(" ");
        passwordStrengthLabel = new JLabel(" ");

        // Set component properties
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        emailField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        confirmPasswordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        signupButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        backToLoginButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));

        // Set button colors
        signupButton.setBackground(new Color(40, 167, 69));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);

        backToLoginButton.setBackground(new Color(108, 117, 125));
        backToLoginButton.setForeground(Color.WHITE);
        backToLoginButton.setFocusPainted(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel titleLabel = new JLabel("Create Your " + Constants.APP_NAME + " Account");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        // Email
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Email:"), gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(emailField, gbc);

        // Password
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        // Password strength indicator
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(passwordStrengthLabel, gbc);

        // Confirm Password
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(confirmPasswordField, gbc);

        // Status label
        gbc.gridy = 9;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(statusLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(signupButton);
        buttonPanel.add(backToLoginButton);

        gbc.gridy = 10;
        mainPanel.add(buttonPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSignup();
            }
        });

        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backToLogin();
            }
        });

        // Password strength indicator
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePasswordStrength();
            }
        });

        // Enter key support
        confirmPasswordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSignup();
            }
        });
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        String strength = Validator.getPasswordStrength(password);

        passwordStrengthLabel.setText("Password strength: " + strength);

        switch (strength) {
            case "Very Weak":
                passwordStrengthLabel.setForeground(Color.RED);
                break;
            case "Weak":
                passwordStrengthLabel.setForeground(new Color(255, 140, 0));
                break;
            case "Fair":
                passwordStrengthLabel.setForeground(new Color(255, 215, 0));
                break;
            case "Good":
                passwordStrengthLabel.setForeground(new Color(173, 255, 47));
                break;
            case "Strong":
                passwordStrengthLabel.setForeground(new Color(0, 128, 0));
                break;
        }
    }

    private void performSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Basic validation
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

        // Show loading state
        signupButton.setEnabled(false);
        signupButton.setText("Creating Account...");
        statusLabel.setText("Creating your account...");
        statusLabel.setForeground(Color.BLUE);

        // Perform signup in background thread
        SwingWorker<AuthResult, Void> worker = new SwingWorker<AuthResult, Void>() {
            @Override
            protected AuthResult doInBackground() throws Exception {
                return authController.registerUser(username, email, password, confirmPassword);
            }

            @Override
            protected void done() {
                try {
                    AuthResult result = get();
                    if (result.isSuccess()) {
                        showSuccess("Account created successfully! Please log in.");
                        // Clear form and go back to login after delay
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
                    signupButton.setText("Create Account");
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
        statusLabel.setForeground(Color.RED);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(40, 167, 69));
    }
}
