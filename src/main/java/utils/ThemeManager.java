package utils;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced Theme manager for FeedHawk with PROPER dark mode that actually works
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
    private static Color hoverColor;

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
     * Apply the current theme colors - FIXED VERSION
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
            hoverColor = Constants.DARK_HOVER;
        } else {
            backgroundColor = Constants.LIGHT_BACKGROUND;
            surfaceColor = Constants.LIGHT_SURFACE;
            cardColor = Constants.LIGHT_CARD;
            textPrimaryColor = Constants.LIGHT_TEXT_PRIMARY;
            textSecondaryColor = Constants.LIGHT_TEXT_SECONDARY;
            accentColor = Constants.LIGHT_ACCENT;
            borderColor = Constants.LIGHT_BORDER;
            hoverColor = Constants.LIGHT_HOVER;
        }
    }
    
    /**
     * Apply theme to ANY component - ACTUALLY WORKS NOW
     */
    public static void applyTheme(JComponent component) {
        if (component == null) return;

        component.setBackground(surfaceColor);
        component.setForeground(textPrimaryColor);

        if (component instanceof JPanel) {
            component.setBackground(surfaceColor);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            styleButton(button);
        } else if (component instanceof JTextField) {
            JTextField field = (JTextField) component;
            field.setBackground(cardColor);
            field.setForeground(textPrimaryColor);
            field.setCaretColor(textPrimaryColor);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        } else if (component instanceof JPasswordField) {
            JPasswordField field = (JPasswordField) component;
            field.setBackground(cardColor);
            field.setForeground(textPrimaryColor);
            field.setCaretColor(textPrimaryColor);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        } else if (component instanceof JList) {
            component.setBackground(cardColor);
            component.setForeground(textPrimaryColor);
        } else if (component instanceof JComboBox) {
            JComboBox combo = (JComboBox) component;
            combo.setBackground(cardColor);
            combo.setForeground(textPrimaryColor);
            // Style the combo box renderer
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        c.setBackground(accentColor);
                        c.setForeground(isDarkMode ? Color.BLACK : Color.WHITE);
                    } else {
                        c.setBackground(cardColor);
                        c.setForeground(textPrimaryColor);
                    }
                    return c;
                }
            });
        } else if (component instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) component;
            scroll.setBackground(backgroundColor);
            scroll.getViewport().setBackground(backgroundColor);
            scroll.setBorder(null);
            // Apply theme to scrollbars
            scroll.getVerticalScrollBar().setBackground(surfaceColor);
            scroll.getHorizontalScrollBar().setBackground(surfaceColor);
        } else if (component instanceof JLabel) {
            component.setForeground(textPrimaryColor);
        } else if (component instanceof JCheckBox) {
            JCheckBox checkbox = (JCheckBox) component;
            checkbox.setBackground(surfaceColor);
            checkbox.setForeground(textPrimaryColor);
        } else if (component instanceof JTextArea) {
            JTextArea textArea = (JTextArea) component;
            textArea.setBackground(cardColor);
            textArea.setForeground(textPrimaryColor);
            textArea.setCaretColor(textPrimaryColor);
        }

        // Apply to all child components recursively
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                applyTheme((JComponent) child);
            }
        }
    }
    
    /**
     * Style a button properly with dark mode - FIXED FOR CLICKABILITY
     */
    private static void styleButton(JButton button) {
        button.setBackground(cardColor);
        button.setForeground(textPrimaryColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        button.setFocusPainted(false);
        button.setFocusable(true);  // CRITICAL: Make button focusable
        button.setEnabled(true);    // CRITICAL: Ensure button is enabled
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        // DON'T remove existing mouse listeners - they might be needed for ActionListeners
        // Instead, add hover effects that don't interfere with button functionality
        
        // Only add hover effect if button doesn't already have mouse listeners for it
        boolean hasHoverEffect = false;
        for (java.awt.event.MouseListener ml : button.getMouseListeners()) {
            if (ml.getClass().getName().contains("ThemeManager")) {
                hasHoverEffect = true;
                break;
            }
        }
        
        if (!hasHoverEffect) {
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) {
                        button.setBackground(accentColor);
                        button.setForeground(isDarkMode ? Color.BLACK : Color.WHITE);
                    }
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) {
                        button.setBackground(cardColor);
                        button.setForeground(textPrimaryColor);
                    }
                }
            });
        }
    }

    /**
     * Create a themed button - ACTUALLY DARK
     */
    public static JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        styleButton(button);
        return button;
    }
    
    /**
     * Create an accent button with orange color - FIXED FOR CLICKABILITY
     */
    public static JButton createAccentButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(accentColor);
        button.setForeground(isDarkMode ? Color.BLACK : Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setFocusable(true);  // CRITICAL: Make button focusable
        button.setEnabled(true);    // CRITICAL: Ensure button is enabled
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        // Hover effect for accent buttons
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(accentColor.brighter());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(accentColor);
                }
            }
        });

        return button;
    }
    
    /**
     * Create a themed panel - ACTUALLY DARK
     */
    public static JPanel createThemedPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(surfaceColor);
        panel.setForeground(textPrimaryColor);
        return panel;
    }

    /**
     * Create a themed card - DARK CARDS
     */
    public static JPanel createThemedCard() {
        JPanel card = new JPanel();
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return card;
    }

    /**
     * Apply theme to an entire window/dialog
     */
    public static void applyThemeToWindow(Window window) {
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().setBackground(backgroundColor);
            applyTheme((JComponent) frame.getContentPane());
        } else if (window instanceof JDialog) {
            JDialog dialog = (JDialog) window;
            dialog.getContentPane().setBackground(backgroundColor);
            applyTheme((JComponent) dialog.getContentPane());
        }

        // Force repaint
        window.repaint();
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
