package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simple test window to verify button clicks are working
 * Run this to test if the issue is with buttons or database
 */
public class ButtonTestWindow extends JFrame {
    
    public ButtonTestWindow() {
        setTitle("Button Test Window");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton testButton1 = new JButton("Test Button 1");
        JButton testButton2 = new JButton("Test Button 2");
        JLabel statusLabel = new JLabel("Click buttons to test", SwingConstants.CENTER);
        
        testButton1.addActionListener(e -> {
            System.out.println("Button 1 clicked!");
            statusLabel.setText("Button 1 was clicked!");
        });
        
        testButton2.addActionListener(e -> {
            System.out.println("Button 2 clicked!");
            statusLabel.setText("Button 2 was clicked!");
        });
        
        panel.add(testButton1);
        panel.add(testButton2);
        panel.add(statusLabel);
        
        add(panel);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ButtonTestWindow().setVisible(true);
        });
    }
}