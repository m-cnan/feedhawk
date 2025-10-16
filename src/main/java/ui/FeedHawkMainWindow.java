package ui;

import auth.AuthController;
import db.FeedDAO;
import db.models.User;
import db.models.Article;
import db.models.Feed;
import rss.FeedParser;
import ui.components.ArticleDialog;
import utils.Constants;
import utils.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public class FeedHawkMainWindow extends JFrame {
    private final AuthController authController;
    private final FeedParser feedParser;
    private final FeedDAO feedDAO;

    private JPanel mainPanel;
    private JPanel feedPanel;
    private JScrollPane feedScrollPane;
    private JPanel sidePanel;
    private JList<String> listsList;
    private DefaultListModel<String> listsModel;
    private JButton refreshButton;
    private JButton addFeedButton;
    private JButton bookmarksButton;
    private JButton settingsButton;
    private JButton logoutButton;
    private JLabel statusLabel;
    private JLabel userLabel;

    // Current data
    private List<Article> currentArticles;
    private User currentUser;
    private String selectedCategory = "Home";

    public FeedHawkMainWindow() {
        this.authController = AuthController.getInstance();
        this.feedParser = new FeedParser();
        this.feedDAO = new FeedDAO();
        this.currentUser = authController.getCurrentUser();
        this.currentArticles = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadInitialData();

        setTitle(Constants.APP_NAME + " - RSS Feed Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Side panel for lists and controls
        sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
        sidePanel.setBackground(new Color(248, 249, 250));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // User info
        String displayName = currentUser != null ? currentUser.getUsername() : "Guest";
        userLabel = new JLabel("👤 " + displayName);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        userLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Categories/Lists
        listsModel = new DefaultListModel<>();
        listsModel.addElement("🏠 " + Constants.DEFAULT_LIST_HOME);
        listsModel.addElement("🔖 " + Constants.DEFAULT_LIST_BOOKMARKS);
        listsModel.addElement("📚 " + Constants.DEFAULT_LIST_READ_LATER);
        listsModel.addElement("⚙️ " + Constants.CATEGORY_TECH);
        listsModel.addElement("🏃‍♂️ " + Constants.CATEGORY_SPORTS);
        listsModel.addElement("📰 " + Constants.CATEGORY_NEWS);
        listsModel.addElement("🔬 " + Constants.CATEGORY_SCIENCE);
        listsModel.addElement("🎬 " + Constants.CATEGORY_ENTERTAINMENT);

        listsList = new JList<>(listsModel);
        listsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listsList.setSelectedIndex(0);
        listsList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        listsList.setCellRenderer(new CustomListCellRenderer());

        // Control buttons
        refreshButton = new JButton("🔄 Refresh Feeds");
        addFeedButton = new JButton("➕ Add Feed");
        bookmarksButton = new JButton("🔖 Bookmarks");
        settingsButton = new JButton("⚙️ Settings");
        logoutButton = new JButton("🚪 Logout");

        styleButton(refreshButton);
        styleButton(addFeedButton);
        styleButton(bookmarksButton);
        styleButton(settingsButton);
        styleButton(logoutButton);

        // Feed panel (main content area)
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(Color.WHITE);
        
        feedScrollPane = new JScrollPane(feedPanel);
        feedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Status bar
        statusLabel = new JLabel("Ready - Welcome to " + Constants.APP_NAME + "!");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(Color.GRAY);
    }
    
    private void styleButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setBackground(new Color(248, 249, 250));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(233, 236, 239));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(248, 249, 250));
            }
        });
    }
    
    private void setupLayout() {
        // Side panel layout
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setOpaque(false);
        userPanel.add(userLabel, BorderLayout.CENTER);

        JPanel listsPanel = new JPanel(new BorderLayout());
        listsPanel.setOpaque(false);
        JLabel listsLabel = new JLabel("📋 Categories");
        listsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        listsLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        listsPanel.add(listsLabel, BorderLayout.NORTH);
        listsPanel.add(new JScrollPane(listsList), BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(addFeedButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(bookmarksButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(settingsButton);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(logoutButton);

        sidePanel.add(userPanel, BorderLayout.NORTH);
        sidePanel.add(listsPanel, BorderLayout.CENTER);
        sidePanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Main panel layout
        mainPanel.add(sidePanel, BorderLayout.WEST);
        mainPanel.add(feedScrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventHandlers() {
        listsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedList = listsList.getSelectedValue();
                if (selectedList != null) {
                    selectedCategory = extractCategoryName(selectedList);
                    loadArticlesForCategory(selectedCategory);
                }
            }
        });
        
        refreshButton.addActionListener(e -> refreshFeeds());
        addFeedButton.addActionListener(e -> showAddFeedDialog());
        bookmarksButton.addActionListener(e -> {
            selectedCategory = Constants.DEFAULT_LIST_BOOKMARKS;
            setSelectedListByName(Constants.DEFAULT_LIST_BOOKMARKS);
            loadArticlesForCategory(selectedCategory);
        });
        settingsButton.addActionListener(e -> showSettingsDialog());
        logoutButton.addActionListener(e -> performLogout());
    }

    private String extractCategoryName(String listItem) {
        // Remove emoji and extract category name
        return listItem.substring(2).trim();
    }

    private void setSelectedListByName(String categoryName) {
        for (int i = 0; i < listsModel.size(); i++) {
            if (listsModel.getElementAt(i).contains(categoryName)) {
                listsList.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadInitialData() {
        statusLabel.setText("Loading sample RSS feeds...");

        // Load some sample RSS feeds to demonstrate functionality
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Simulate loading real RSS feeds
                publish("Fetching BBC News...");
                loadSampleFeed("https://feeds.bbci.co.uk/news/world/rss.xml", Constants.CATEGORY_NEWS);

                publish("Fetching TechCrunch...");
                loadSampleFeed("https://techcrunch.com/feed/", Constants.CATEGORY_TECH);

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    statusLabel.setText(message);
                }
            }

            @Override
            protected void done() {
                statusLabel.setText("Ready - " + currentArticles.size() + " articles loaded");
                loadArticlesForCategory(selectedCategory);
            }
        };
        worker.execute();
    }

    private void loadSampleFeed(String feedUrl, String category) {
        try {
            FeedParser.ParseResult result = feedParser.parseFeed(feedUrl);
            if (result.isSuccess() && result.getArticles() != null) {
                for (Article article : result.getArticles()) {
                    // Set category for filtering
                    article.setFeedId(category.hashCode()); // Simple category mapping
                    currentArticles.add(article);

                    // Limit articles to avoid overwhelming the UI
                    if (currentArticles.size() >= 20) break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading feed " + feedUrl + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadArticlesForCategory(String category) {
        feedPanel.removeAll();
        
        List<Article> filteredArticles = filterArticlesByCategory(category);

        if (filteredArticles.isEmpty()) {
            showEmptyState(category);
        } else {
            for (Article article : filteredArticles) {
                feedPanel.add(createArticleCard(article));
                feedPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        feedPanel.revalidate();
        feedPanel.repaint();
        
        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            feedScrollPane.getViewport().setViewPosition(new Point(0, 0));
        });

        statusLabel.setText("Showing " + filteredArticles.size() + " articles in " + category);
    }

    private List<Article> filterArticlesByCategory(String category) {
        if (category.equals(Constants.DEFAULT_LIST_HOME)) {
            return new ArrayList<>(currentArticles);
        } else if (category.equals(Constants.DEFAULT_LIST_BOOKMARKS)) {
            return currentArticles.stream()
                .filter(Article::isBookmarked)
                .toList();
        } else if (category.equals(Constants.DEFAULT_LIST_READ_LATER)) {
            return currentArticles.stream()
                .filter(Article::isReadLater)
                .toList();
        } else {
            // Filter by category based on feed source or content
            return currentArticles.stream()
                .filter(article -> matchesCategory(article, category))
                .toList();
        }
    }

    private boolean matchesCategory(Article article, String category) {
        // Simple category matching based on feed ID or content
        return article.getFeedId() == category.hashCode();
    }

    private void showEmptyState(String category) {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new EmptyBorder(50, 20, 50, 20));

        JLabel emptyLabel = new JLabel("<html><center>" +
            "<h2>No articles in " + category + "</h2>" +
            "<p>Add some RSS feeds to get started!</p>" +
            "</center></html>");
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setForeground(Color.GRAY);

        JButton addFeedBtn = new JButton("➕ Add Your First Feed");
        addFeedBtn.addActionListener(e -> showAddFeedDialog());
        styleButton(addFeedBtn);

        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        emptyPanel.add(addFeedBtn, BorderLayout.SOUTH);

        feedPanel.add(emptyPanel);
    }
    
    private JPanel createArticleCard(Article article) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 230, 235), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(article.isRead() ? new Color(249, 249, 249) : Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        // Header with source and date
        JPanel header = createArticleHeader(article);

        // Title
        JLabel titleLabel = new JLabel("<html><h3 style='margin: 5px 0;'>" +
            (article.getTitle() != null ? article.getTitle() : "Untitled") + "</h3></html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        // Content preview
        String description = article.getDescription();
        if (description != null && description.length() > Constants.ARTICLE_PREVIEW_LENGTH) {
            description = description.substring(0, Constants.ARTICLE_PREVIEW_LENGTH) + "...";
        }
        JLabel contentLabel = new JLabel("<html><p style='margin: 0; color: #666;'>" +
            (description != null ? description : "No description available") + "</p></html>");
        contentLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        // Action buttons
        JPanel actionPanel = createArticleActions(article);

        // Main content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(contentLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(actionPanel);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createArticleHeader(Article article) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel sourceLabel = new JLabel("📡 RSS Feed");
        sourceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        sourceLabel.setForeground(new Color(0, 123, 255));
        
        String dateText = "Recent";
        if (article.getPublishedDate() != null) {
            dateText = article.getPublishedDate().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        }

        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        dateLabel.setForeground(Color.GRAY);
        
        header.add(sourceLabel, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createArticleActions(Article article) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        actionPanel.setOpaque(false);
        
        JButton readButton = new JButton(article.isRead() ? "✅ Read" : "📖 Mark Read");
        JButton bookmarkButton = new JButton(article.isBookmarked() ? "🔖 Bookmarked" : "🔖 Bookmark");
        JButton readLaterButton = new JButton(article.isReadLater() ? "📚 Saved" : "📚 Read Later");
        JButton openButton = new JButton("🔗 Open");
        
        styleActionButton(readButton);
        styleActionButton(bookmarkButton);
        styleActionButton(readLaterButton);
        styleActionButton(openButton);
        
    // Button actions
    readButton.addActionListener(e -> toggleArticleRead(article, readButton));
    bookmarkButton.addActionListener(e -> toggleArticleBookmark(article, bookmarkButton));
    readLaterButton.addActionListener(e -> toggleArticleReadLater(article, readLaterButton));
    openButton.addActionListener(e -> showArticleDetailsDialog(article));

        actionPanel.add(readButton);
        actionPanel.add(Box.createHorizontalStrut(5));
        actionPanel.add(bookmarkButton);
        actionPanel.add(Box.createHorizontalStrut(5));
        actionPanel.add(readLaterButton);
        actionPanel.add(Box.createHorizontalStrut(5));
        actionPanel.add(openButton);
        
        return actionPanel;
    }
    
    private void showArticleDetailsDialog(Article article) {
        ArticleDialog dialog = new ArticleDialog(this, article);
        dialog.setVisible(true);
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        button.setBackground(new Color(248, 249, 250));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(233, 236, 239));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(248, 249, 250));
            }
        });
    }
    
    private void toggleArticleRead(Article article, JButton button) {
        boolean newReadStatus = !article.isRead();
        article.setRead(newReadStatus);
        button.setText(article.isRead() ? "✅ Read" : "📖 Mark Read");

        // Update card background
        JPanel card = (JPanel) button.getParent().getParent().getParent();
        card.setBackground(article.isRead() ? new Color(249, 249, 249) : Color.WHITE);
        card.repaint();

        System.out.println("Article '" + article.getTitle() + "' marked as " + (newReadStatus ? "read" : "unread"));
        System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() : "Guest"));
        
        // Save to database
        if (currentUser != null && article.getId() > 0) {
            feedDAO.markArticleAsRead(currentUser.getId(), article.getId(), newReadStatus);
        }
    }

    private void toggleArticleBookmark(Article article, JButton button) {
        boolean newBookmarkStatus = !article.isBookmarked();
        article.setBookmarked(newBookmarkStatus);
        button.setText(article.isBookmarked() ? "🔖 Bookmarked" : "🔖 Bookmark");
        
        // Save to database
        if (currentUser != null && article.getId() > 0) {
            feedDAO.markArticleAsSaved(currentUser.getId(), article.getId(), newBookmarkStatus);
        }
    }

    private void toggleArticleReadLater(Article article, JButton button) {
        boolean newSavedStatus = !article.isReadLater();
        article.setReadLater(newSavedStatus);
        button.setText(article.isReadLater() ? "📚 Saved" : "📚 Read Later");
        
        // Save to database
        if (currentUser != null && article.getId() > 0) {
            feedDAO.markArticleAsSaved(currentUser.getId(), article.getId(), newSavedStatus);
        }
    }

    private void refreshFeeds() {
        statusLabel.setText("Refreshing feeds...");
        refreshButton.setEnabled(false);
        refreshButton.setText("🔄 Refreshing...");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Clear current articles
                currentArticles.clear();

                // Reload feeds
                publish("Fetching latest articles...");
                loadSampleFeed("https://feeds.bbci.co.uk/news/world/rss.xml", Constants.CATEGORY_NEWS);
                loadSampleFeed("https://techcrunch.com/feed/", Constants.CATEGORY_TECH);

                // Simulate some processing time
                Thread.sleep(1000);

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    statusLabel.setText(message);
                }
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                refreshButton.setText("🔄 Refresh Feeds");
                statusLabel.setText("Feeds refreshed - " + currentArticles.size() + " articles loaded");
                loadArticlesForCategory(selectedCategory);
            }
        };
        worker.execute();
    }

    private void showAddFeedDialog() {
        String feedUrl = JOptionPane.showInputDialog(this,
            "Enter RSS feed URL:",
            "Add New Feed",
            JOptionPane.QUESTION_MESSAGE);

        if (feedUrl != null && !feedUrl.trim().isEmpty()) {
            if (Validator.isValidRSSUrl(feedUrl.trim())) {
                addNewFeed(feedUrl.trim());
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid RSS feed URL",
                    "Invalid URL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addNewFeed(String feedUrl) {
        statusLabel.setText("Adding new feed...");

        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Validating feed: " + feedUrl);
                FeedParser.ParseResult result = feedParser.parseFeed(feedUrl);

                if (result.isSuccess()) {
                    publish("Feed validated successfully");
                    // Add articles from the new feed
                    if (result.getArticles() != null) {
                        for (Article article : result.getArticles()) {
                            article.setFeedId("Custom".hashCode());
                            currentArticles.add(article);
                        }
                    }
                    return true;
                } else {
                    publish("Failed to add feed: " + result.getMessage());
                    return false;
                }
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    statusLabel.setText(message);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        statusLabel.setText("Feed added successfully!");
                        loadArticlesForCategory(selectedCategory);
                        JOptionPane.showMessageDialog(FeedHawkMainWindow.this,
                            "Feed added successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("Failed to add feed");
                        JOptionPane.showMessageDialog(FeedHawkMainWindow.this,
                            "Failed to add feed. Please check the URL and try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error adding feed");
                    JOptionPane.showMessageDialog(FeedHawkMainWindow.this,
                        "Error adding feed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setSize(400, 300);
        settingsDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("⚙️ " + Constants.APP_NAME + " Settings");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        JPanel settingsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        settingsPanel.add(new JLabel("Refresh interval: 60 minutes"));
        settingsPanel.add(new JLabel("Auto-refresh: Enabled"));
        settingsPanel.add(new JLabel("Theme: System Default"));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> settingsDialog.dispose());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(settingsPanel, BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);

        settingsDialog.add(panel);
        settingsDialog.setVisible(true);
    }
    
    private void performLogout() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            authController.logoutUser();
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginScreen().setVisible(true);
            });
        }
    }

    // Custom list cell renderer for better appearance
    private static class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(8, 12, 8, 12));

            if (isSelected) {
                label.setBackground(new Color(0, 123, 255));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}

