package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupButtonTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Signup Button Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            // Username field
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            JTextField usernameField = new JTextField(15);
            panel.add(usernameField, gbc);
            
            // Email field
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            JTextField emailField = new JTextField(15);
            panel.add(emailField, gbc);
            
            // Password field
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            JPasswordField passwordField = new JPasswordField(15);
            panel.add(passwordField, gbc);
            
            // Confirm Password field
            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("Confirm:"), gbc);
            gbc.gridx = 1;
            JPasswordField confirmField = new JPasswordField(15);
            panel.add(confirmField, gbc);
            
            // Status label
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            JLabel statusLabel = new JLabel("Ready to test signup...");
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(statusLabel, gbc);
            
            // Create Account button
            gbc.gridy = 5;
            JButton createButton = new JButton("Create Account");
            createButton.setPreferredSize(new Dimension(200, 35));
            
            createButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("✅ Button clicked!");
                    statusLabel.setText("Button clicked successfully!");
                    
                    String username = usernameField.getText();
                    String email = emailField.getText();
                    String password = new String(passwordField.getPassword());
                    String confirm = new String(confirmField.getPassword());
                    
                    System.out.println("Username: " + username);
                    System.out.println("Email: " + email);
                    System.out.println("Password length: " + password.length());
                    System.out.println("Confirm length: " + confirm.length());
                    
                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        statusLabel.setText("Please fill all fields");
                        return;
                    }
                    
                    if (!password.equals(confirm)) {
                        statusLabel.setText("Passwords don't match");
                        return;
                    }
                    
                    statusLabel.setText("All validations passed! (DB test disabled)");
                }
            });
            
            panel.add(createButton, gbc);
            
            frame.add(panel);
            frame.setVisible(true);
            
            System.out.println("Test window created. Try clicking the button!");
        });
    }
}