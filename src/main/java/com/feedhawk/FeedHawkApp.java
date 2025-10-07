package com.feedhawk;

import ui.LoginScreen;
import ui.StreamlinedMainWindow;
import auth.AuthController;
import db.models.User;

import javax.swing.*;
import java.awt.*;

/**
 * Main application launcher for FeedHawk RSS Reader
 * This starts the application with proper UI flow
 */
public class FeedHawkApp {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        // Start the application on EDT
        SwingUtilities.invokeLater(() -> {
            AuthController authController = AuthController.getInstance();
            
            // Check if user is already logged in (session)
            User currentUser = authController.getCurrentUser();
            
            if (currentUser != null) {
                // User is already logged in, go straight to main window
                launchMainApplication();
            } else {
                // Show login screen first
                launchLoginScreen();
            }
        });
    }
    
    public static void launchLoginScreen() {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }
    
    public static void launchMainApplication() {
        StreamlinedMainWindow mainWindow = new StreamlinedMainWindow();
        mainWindow.setVisible(true);
    }
    
    public static void logout() {
        AuthController.getInstance().logoutUser();
        // Close all windows and show login screen
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        launchLoginScreen();
    }
}
