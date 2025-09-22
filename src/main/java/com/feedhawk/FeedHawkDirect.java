package com.feedhawk;

import ui.FeedHawkMainWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Direct launcher for FeedHawk Main Window without authentication
 * Use this to test RSS functionality without database setup
 */
public class FeedHawkDirect {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        SwingUtilities.invokeLater(() -> {
            System.out.println("🚀 Launching FeedHawk RSS Reader (Direct Mode)");
            System.out.println("📡 This mode bypasses authentication to test RSS functionality");
            
            new FeedHawkMainWindow().setVisible(true);
        });
    }
}
