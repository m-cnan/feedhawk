package ui.components;

import db.models.Article;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ArticleDialog extends JDialog {

    private final Article article;

    public ArticleDialog(Frame parent, Article article) {
        super(parent, "Article Details", true);
        this.article = article;
        initializeUI();
    }

    private void initializeUI() {
        setSize(750, 650);
        setLocationRelativeTo(getParent());

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(new EmptyBorder(20, 20, 20, 20));
        container.setBackground(ThemeManager.getBackgroundColor());

        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        String title = article.getTitle() != null ? article.getTitle() : "Untitled Article";
        JLabel titleLabel = new JLabel("<html><h2 style='margin:0;'>" + title + "</h2></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(ThemeManager.getTextPrimaryColor());
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        container.add(headerPanel, BorderLayout.NORTH);

        // Main content panel (scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Metadata section
        JPanel metaPanel = new JPanel();
        metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.Y_AXIS));
        metaPanel.setOpaque(true);
        metaPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        metaPanel.setBackground(ThemeManager.getCardColor());

        // Published date
        if (article.getPublishedDate() != null) {
            String publishedText = article.getPublishedDate()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm"));
            metaPanel.add(createInfoRow("📅 Published:", publishedText));
            metaPanel.add(Box.createVerticalStrut(6));
        }

        // Author
        String author = article.getAuthor() != null ? article.getAuthor() : "Unknown Author";
        metaPanel.add(createInfoRow("✍️ Author:", author));
        metaPanel.add(Box.createVerticalStrut(6));

        // URL
        if (article.getUrl() != null && !article.getUrl().isEmpty()) {
            JPanel urlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            urlRow.setOpaque(false);
            JLabel urlLabelText = new JLabel("<html><b>🔗 URL:</b>&nbsp;</html>");
            urlLabelText.setForeground(ThemeManager.getTextPrimaryColor());
            JLabel urlValue = new JLabel("<html><a href=''>" + truncateUrl(article.getUrl(), 60) + "</a></html>");
            urlValue.setForeground(ThemeManager.getAccentColor());
            urlValue.setCursor(new Cursor(Cursor.HAND_CURSOR));
            urlValue.setToolTipText(article.getUrl());
            urlRow.add(urlLabelText);
            urlRow.add(urlValue);
            metaPanel.add(urlRow);
            metaPanel.add(Box.createVerticalStrut(6));
        }

        // Feed ID
        if (article.getFeedId() > 0) {
            metaPanel.add(createInfoRow("📡 Feed ID:", String.valueOf(article.getFeedId())));
        }

        contentPanel.add(metaPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Content section
        if (article.getContent() != null && !article.getContent().isEmpty()) {
            JLabel contentLabel = new JLabel("📄 Content");
            contentLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            contentLabel.setForeground(ThemeManager.getTextPrimaryColor());
            contentLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
            contentPanel.add(contentLabel);

            JTextArea contentArea = new JTextArea(article.getContent());
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setFont(new Font("Serif", Font.PLAIN, 14));
            contentArea.setForeground(ThemeManager.getTextSecondaryColor());
            contentArea.setBackground(ThemeManager.getCardColor());
            contentArea.setCaretColor(ThemeManager.getTextPrimaryColor());
            contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
                new EmptyBorder(12, 12, 12, 12)
            ));
            contentArea.setRows(10);
            
            JScrollPane contentScroll = new JScrollPane(contentArea);
            contentScroll.setBorder(BorderFactory.createEmptyBorder());
            contentScroll.getViewport().setBackground(ThemeManager.getCardColor());
            contentPanel.add(contentScroll);
        }

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getViewport().setBackground(ThemeManager.getBackgroundColor());
        container.add(mainScroll, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton openUrlButton = ThemeManager.createAccentButton("🔗 Open URL");
        openUrlButton.addActionListener(e -> openArticleInBrowser());

        JButton closeButton = ThemeManager.createThemedButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(openUrlButton);
        buttonPanel.add(closeButton);

        container.add(buttonPanel, BorderLayout.SOUTH);

        add(container);
        getContentPane().setBackground(ThemeManager.getBackgroundColor());
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        JLabel labelComponent = new JLabel("<html><b>" + label + "</b>&nbsp;</html>");
        labelComponent.setFont(new Font("SansSerif", Font.PLAIN, 13));
        labelComponent.setForeground(ThemeManager.getTextPrimaryColor());
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("SansSerif", Font.PLAIN, 13));
        valueComponent.setForeground(ThemeManager.getTextSecondaryColor());
        row.add(labelComponent);
        row.add(valueComponent);
        return row;
    }

    private String truncateUrl(String url, int maxLength) {
        if (url.length() <= maxLength) {
            return url;
        }
        return url.substring(0, maxLength - 3) + "...";
    }

    private void openArticleInBrowser() {
        if (article.getUrl() != null && !article.getUrl().isEmpty()) {
            try {
                Desktop.getDesktop().browse(java.net.URI.create(article.getUrl()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Could not open URL: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "No URL available for this article",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
