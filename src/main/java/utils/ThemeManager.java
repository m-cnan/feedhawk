package utils;

import javax.swing.*;
import java.awt.*;

/**
 * Theme manager for FeedHawk that handles dark/light mode switching
 */
public class ThemeManager {
    private static boolean isDarkMode = Constants.DEFAULT_DARK_MODE;
    
    // Current theme colors (will be set based on mode)
    private static Color backgroundColor;
    private static Color surfaceColor;
    private static Color cardColor;
    private static Color textPrimaryColor;
    private static Color textSecondaryColor;
    private static Color accentColor;
    private static Color borderColor;
    
    static {
        applyTheme();
    }
    
    /**
     * Toggle between dark and light mode
     */
    public static void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }
    
    /**
     * Set theme mode explicitly
     */
    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        applyTheme();
    }
    
    /**
     * Apply the current theme colors
     */
    private static void applyTheme() {
        if (isDarkMode) {
            backgroundColor = Constants.DARK_BACKGROUND;
            surfaceColor = Constants.DARK_SURFACE;
            cardColor = Constants.DARK_CARD;
            textPrimaryColor = Constants.DARK_TEXT_PRIMARY;
            textSecondaryColor = Constants.DARK_TEXT_SECONDARY;
            accentColor = Constants.DARK_ACCENT;
            borderColor = Constants.DARK_BORDER;
        } else {
            backgroundColor = Constants.LIGHT_BACKGROUND;
            surfaceColor = Constants.LIGHT_SURFACE;
            cardColor = Constants.LIGHT_CARD;
            textPrimaryColor = Constants.LIGHT_TEXT_PRIMARY;
            textSecondaryColor = Constants.LIGHT_TEXT_SECONDARY;
            accentColor = Constants.LIGHT_ACCENT;
            borderColor = Constants.LIGHT_BORDER;
        }
    }
    
    /**
     * Apply theme to a component
     */
    public static void applyTheme(JComponent component) {
        component.setBackground(getBackgroundColor());
        component.setForeground(getTextPrimaryColor());
        
        if (component instanceof JPanel) {
            component.setBackground(getSurfaceColor());
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setBackground(getCardColor());
            button.setForeground(getTextPrimaryColor());
            button.setBorder(BorderFactory.createLineBorder(getBorderColor()));
            
            // Add hover effects
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(getAccentColor());
                    button.setForeground(isDarkMode ? Color.BLACK : Color.WHITE);
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(getCardColor());
                    button.setForeground(getTextPrimaryColor());
                }
            });
        } else if (component instanceof JTextField || component instanceof JPasswordField) {
            component.setBackground(getCardColor());
            component.setForeground(getTextPrimaryColor());
            component.setBorder(BorderFactory.createLineBorder(getBorderColor()));
        }
    }
    
    /**
     * Create a themed button
     */
    public static JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(getCardColor());
        button.setForeground(getTextPrimaryColor());
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getBorderColor()),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(getAccentColor());
                button.setForeground(isDarkMode ? Color.BLACK : Color.WHITE);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(getCardColor());
                button.setForeground(getTextPrimaryColor());
            }
        });
        
        return button;
    }
    
    /**
     * Create a themed panel
     */
    public static JPanel createThemedPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(getSurfaceColor());
        return panel;
    }
    
    /**
     * Create a themed card panel (for articles, feeds, etc.)
     */
    public static JPanel createThemedCard() {
        JPanel card = new JPanel();
        card.setBackground(getCardColor());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getBorderColor()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return card;
    }
    
    // Getters for current theme colors
    public static boolean isDarkMode() { return isDarkMode; }
    public static Color getBackgroundColor() { return backgroundColor; }
    public static Color getSurfaceColor() { return surfaceColor; }
    public static Color getCardColor() { return cardColor; }
    public static Color getTextPrimaryColor() { return textPrimaryColor; }
    public static Color getTextSecondaryColor() { return textSecondaryColor; }
    public static Color getAccentColor() { return accentColor; }
    public static Color getBorderColor() { return borderColor; }
}
