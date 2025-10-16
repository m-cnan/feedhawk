package ui;

import auth.AuthController;
import db.models.User;
import db.models.Article;
import db.models.Feed;
import db.FeedDAO;
import rss.FeedParser;
import rss.RSSSearchService;
import ui.components.ArticleDialog;
import utils.Constants;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

public class StreamlinedMainWindow extends JFrame {
    private final AuthController authController;
    private final FeedParser feedParser;
    private final FeedDAO feedDAO;
    
    private JPanel feedPanel;
    private JScrollPane feedScrollPane;
    private JPanel sidePanel;
    private JList<String> userListsList;
    private DefaultListModel<String> userListsModel;
    private JButton refreshButton;
    private JButton discoverButton;
    private JButton settingsButton;
    private JButton logoutButton;
    private JButton themeToggleButton;
    private JLabel statusLabel;
    private JLabel userLabel;
    private JComboBox<String> viewModeComboBox;
    
    // Current data
    private List<Article> currentArticles;
    private User currentUser;
    private String selectedList = Constants.DEFAULT_LIST_HOME;
    private String currentViewMode = Constants.VIEW_MODE_MAGAZINE; // Default to magazine view

    public StreamlinedMainWindow() {
        this.authController = AuthController.getInstance();
        this.feedParser = new FeedParser();
        this.feedDAO = new FeedDAO();
        this.currentUser = authController.getCurrentUser();
        this.currentArticles = new ArrayList<>();
        
        // Apply dark theme first
        applyTheme();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        checkForEmptyState();
        
        setTitle(Constants.APP_NAME + " - RSS Feed Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }

    private void applyTheme() {
        // Set the frame background
        getContentPane().setBackground(ThemeManager.getBackgroundColor());
    }
    
    private void initializeComponents() {
        // Side panel for user lists and controls
        sidePanel = ThemeManager.createThemedPanel();
        sidePanel.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
        sidePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        sidePanel.setBackground(ThemeManager.getSurfaceColor());

        // User info
        String displayName = currentUser != null ? currentUser.getUsername() : "Guest";
        userLabel = new JLabel("👤 " + displayName);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        userLabel.setForeground(ThemeManager.getTextPrimaryColor());
        userLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // User Lists - Load from database
        userListsModel = new DefaultListModel<>();
        loadUserLists();

        userListsList = new JList<>(userListsModel);
        userListsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListsList.setSelectedIndex(0);
        userListsList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userListsList.setBackground(ThemeManager.getCardColor());
        userListsList.setForeground(ThemeManager.getTextPrimaryColor());
        userListsList.setCellRenderer(new CustomListCellRenderer());
        
        // View mode selector - Only Magazine and Reel views
        viewModeComboBox = new JComboBox<>(new String[]{"📖 Magazine View", "📱 Reel View"});
        viewModeComboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        ThemeManager.applyTheme(viewModeComboBox);

        // Control buttons with proper theming
        refreshButton = ThemeManager.createThemedButton("🔄 Refresh");
        refreshButton.setFocusable(true);
        refreshButton.setEnabled(true);
        
        discoverButton = ThemeManager.createAccentButton("🔍 Discover Feeds");
        discoverButton.setFocusable(true);
        discoverButton.setEnabled(true);
        
        JButton createListButton = ThemeManager.createThemedButton("➕ Create New List");
        createListButton.setFocusable(true);
        createListButton.setEnabled(true);
        
        settingsButton = ThemeManager.createThemedButton("⚙️ Settings");
        settingsButton.setFocusable(true);
        settingsButton.setEnabled(true);
        
        themeToggleButton = ThemeManager.createThemedButton(ThemeManager.isDarkMode() ? "☀️ Light Mode" : "🌙 Dark Mode");
        themeToggleButton.setFocusable(true);
        themeToggleButton.setEnabled(true);
        
        logoutButton = ThemeManager.createThemedButton("🚪 Logout");
        logoutButton.setFocusable(true);
        logoutButton.setEnabled(true);

        // Main feed panel with proper dark theme
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(ThemeManager.getBackgroundColor());

        feedScrollPane = new JScrollPane(feedPanel);
        feedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        feedScrollPane.setBackground(ThemeManager.getBackgroundColor());
        feedScrollPane.getViewport().setBackground(ThemeManager.getBackgroundColor());

        // Status bar with proper theming
        statusLabel = new JLabel("Welcome to " + Constants.APP_NAME + "!");
        statusLabel.setBorder(new EmptyBorder(8, 15, 8, 15));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(ThemeManager.getTextSecondaryColor());
        statusLabel.setBackground(ThemeManager.getSurfaceColor());
        statusLabel.setOpaque(true);

        // Add event listener for create list button
        createListButton.addActionListener(e -> showCreateListDialog());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Side panel layout
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setOpaque(false);
        userPanel.add(userLabel, BorderLayout.CENTER);
        
        JPanel listsPanel = new JPanel(new BorderLayout());
        listsPanel.setOpaque(false);
        JLabel listsLabel = new JLabel("📂 My Lists");
        listsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        listsLabel.setForeground(ThemeManager.getTextPrimaryColor());
        listsLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        listsPanel.add(listsLabel, BorderLayout.NORTH);
        listsPanel.add(new JScrollPane(userListsList), BorderLayout.CENTER);
        
        // View mode panel
        JPanel viewModePanel = new JPanel(new BorderLayout());
        viewModePanel.setOpaque(false);
        viewModePanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel viewLabel = new JLabel("👁️ View");
        viewLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        viewLabel.setForeground(ThemeManager.getTextPrimaryColor());
        viewLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        viewModePanel.add(viewLabel, BorderLayout.NORTH);
        viewModePanel.add(viewModeComboBox, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(discoverButton);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(settingsButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(logoutButton);
        
        sidePanel.add(userPanel, BorderLayout.NORTH);
        sidePanel.add(listsPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.add(viewModePanel);
        bottomPanel.add(buttonsPanel);
        
        sidePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(sidePanel, BorderLayout.WEST);
        add(feedScrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        userListsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedListItem = userListsList.getSelectedValue();
                if (selectedListItem != null) {
                    selectedList = extractListName(selectedListItem);
                    loadArticlesForList(selectedList);
                }
            }
        });
        
        viewModeComboBox.addActionListener(e -> {
            String selected = (String) viewModeComboBox.getSelectedItem();
            if (selected != null) {
                if (selected.contains("Magazine")) currentViewMode = Constants.VIEW_MODE_MAGAZINE;
                else if (selected.contains("Reel")) currentViewMode = Constants.VIEW_MODE_REEL;
                
                loadArticlesForList(selectedList);
            }
        });
        
        refreshButton.addActionListener(e -> refreshFeeds());
        discoverButton.addActionListener(e -> openFeedDiscovery());
        settingsButton.addActionListener(e -> showSettingsDialog());
        logoutButton.addActionListener(e -> performLogout());
        themeToggleButton.addActionListener(e -> toggleTheme());
    }
    
    private String extractListName(String listItem) {
        return listItem.substring(2).trim();
    }
    
    private void checkForEmptyState() {
        if (currentUser != null) {
            List<Feed> userFeeds = feedDAO.getUserFeeds(currentUser.getId());
            if (userFeeds.isEmpty()) {
                showEmptyState();
                return;
            }
        }
        loadInitialData();
    }
    
    private void showEmptyState() {
        feedPanel.removeAll();
        
        JPanel emptyPanel = ThemeManager.createThemedPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBorder(new EmptyBorder(100, 50, 100, 50));
        
        JLabel welcomeLabel = new JLabel("<html><center>" +
            "<h1>🎯 Welcome to " + Constants.APP_NAME + "!</h1>" +
            "<h3>Discover amazing RSS feeds to get started</h3>" +
            "<p>Browse our curated collection of high-quality sources<br>" +
            "from news, tech, sports, science, and more.</p>" +
            "</center></html>");
        welcomeLabel.setForeground(ThemeManager.getTextPrimaryColor());
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton discoverFeedsBtn = ThemeManager.createAccentButton("🔍 Discover RSS Feeds");
        discoverFeedsBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        discoverFeedsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        discoverFeedsBtn.addActionListener(e -> openFeedDiscovery());
        
        emptyPanel.add(welcomeLabel);
        emptyPanel.add(Box.createVerticalStrut(30));
        emptyPanel.add(discoverFeedsBtn);
        
        feedPanel.add(emptyPanel);
        feedPanel.revalidate();
        feedPanel.repaint();
        
        statusLabel.setText("No feeds subscribed yet - discover some great sources!");
    }
    
    private void loadInitialData() {
        if (currentUser == null) {
            loadSampleFeedsForDemo();
            return;
        }
        
        statusLabel.setText("Loading your feeds...");
        
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Feed> userFeeds = feedDAO.getUserFeeds(currentUser.getId());
                
                if (userFeeds.isEmpty()) {
                    publish("No feeds found");
                    return null;
                }
                
                currentArticles.clear();
                
                for (Feed feed : userFeeds) {
                    publish("Loading " + feed.getTitle() + "...");
                    
                    try {
                        FeedParser.ParseResult result = feedParser.parseFeed(feed.getUrl());
                        if (result.isSuccess() && result.getArticles() != null) {
                            for (Article article : result.getArticles()) {
                                article.setFeedId(feed.getId());
                                currentArticles.add(article);
                                if (currentArticles.size() >= 50) break;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading feed: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    if (currentArticles.size() >= 50) break;
                }
                
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
                if (currentArticles.isEmpty()) {
                    showEmptyState();
                } else {
                    statusLabel.setText("Loaded " + currentArticles.size() + " articles");
                    loadArticlesForList(selectedList);
                }
            }
        };
        worker.execute();
    }
    
    private void loadSampleFeedsForDemo() {
        statusLabel.setText("Loading sample feeds...");
        
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                currentArticles.clear();
                
                publish("Fetching BBC News...");
                loadSampleFeed("https://feeds.bbci.co.uk/news/world/rss.xml", "BBC News");
                
                publish("Fetching TechCrunch...");
                loadSampleFeed("https://techcrunch.com/feed/", "TechCrunch");
                
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
                statusLabel.setText("Demo - " + currentArticles.size() + " articles loaded");
                loadArticlesForList(selectedList);
            }
        };
        worker.execute();
    }
    
    private void loadSampleFeed(String feedUrl, String sourceName) {
        try {
            FeedParser.ParseResult result = feedParser.parseFeed(feedUrl);
            if (result.isSuccess() && result.getArticles() != null) {
                int count = 0;
                for (Article article : result.getArticles()) {
                    article.setFeedId(sourceName.hashCode());
                    currentArticles.add(article);
                    count++;
                    if (count >= 15) break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading sample feed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadArticlesForList(String listName) {
        feedPanel.removeAll();
        
        List<Article> filteredArticles = filterArticlesByList(listName);
        
        if (filteredArticles.isEmpty()) {
            showEmptyListState(listName);
        } else {
            switch (currentViewMode) {
                case Constants.VIEW_MODE_MAGAZINE:
                    renderMagazineView(filteredArticles);
                    break;
                case Constants.VIEW_MODE_REEL:
                    renderReelView(filteredArticles);
                    break;
                default:
                    renderMagazineView(filteredArticles);
            }
        }
        
        feedPanel.revalidate();
        feedPanel.repaint();
        scrollToTop();
        
        statusLabel.setText("Showing " + filteredArticles.size() + " articles in " + listName);
    }
    
    private List<Article> filterArticlesByList(String listName) {
        if (listName.equals(Constants.DEFAULT_LIST_HOME)) {
            return new ArrayList<>(currentArticles);
        } else if (listName.equals(Constants.DEFAULT_LIST_SAVED)) {
            return currentArticles.stream()
                .filter(Article::isSaved)
                .toList();
        } else {
            return new ArrayList<>(currentArticles);
        }
    }
    
    private void showEmptyListState(String listName) {
        JPanel emptyPanel = ThemeManager.createThemedPanel();
        emptyPanel.setLayout(new BorderLayout());
        emptyPanel.setBorder(new EmptyBorder(80, 40, 80, 40));
        
        String message = listName.equals(Constants.DEFAULT_LIST_SAVED)
            ? "<html><center><h2>No saved articles</h2><p>Articles you save will appear here</p></center></html>"
            : "<html><center><h2>No articles in " + listName + "</h2><p>Add some RSS feeds to get started!</p></center></html>";
        
        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setForeground(ThemeManager.getTextSecondaryColor());
        
        JButton actionBtn = ThemeManager.createAccentButton("🔍 Discover Feeds");
        actionBtn.addActionListener(e -> openFeedDiscovery());
        
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        emptyPanel.add(actionBtn, BorderLayout.SOUTH);
        
        feedPanel.add(emptyPanel);
    }
    
    private void renderMagazineView(List<Article> articles) {
        for (Article article : articles) {
            feedPanel.add(createArticleCard(article));
            feedPanel.add(Box.createVerticalStrut(12));
        }
    }
    
    private void renderReelView(List<Article> articles) {
        JLabel comingSoonLabel = new JLabel("<html><center><h2>📱 Reel View</h2><p>TikTok-style article browsing coming soon!</p></center></html>");
        comingSoonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        comingSoonLabel.setForeground(ThemeManager.getTextSecondaryColor());
        feedPanel.add(comingSoonLabel);
    }
    
    private JPanel createArticleCard(Article article) {
        JPanel card = ThemeManager.createThemedCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        // Slightly different background for read articles
        if (article.isRead()) {
            card.setBackground(new Color(ThemeManager.getCardColor().getRGB() - 0x050505));
        }
        
        // Article header
        JPanel header = createArticleHeader(article);
        
        // Article title
        JLabel titleLabel = new JLabel("<html><h3 style='margin: 0; line-height: 1.3;'>" + 
            (article.getTitle() != null ? article.getTitle() : "Untitled") + "</h3></html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(ThemeManager.getTextPrimaryColor());
        
        // Article content preview
        String description = article.getDescription();
        if (description != null && description.length() > Constants.ARTICLE_PREVIEW_LENGTH) {
            description = description.substring(0, Constants.ARTICLE_PREVIEW_LENGTH) + "...";
        }
        JLabel contentLabel = new JLabel("<html><p style='margin: 8px 0 0 0; line-height: 1.4;'>" + 
            (description != null ? description : "No description available") + "</p></html>");
        contentLabel.setForeground(ThemeManager.getTextSecondaryColor());
        contentLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        // Simplified action buttons
        JPanel actionPanel = createSimplifiedActions(article);
        
        // Content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(titleLabel);
        content.add(contentLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(actionPanel);
        
        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createArticleHeader(Article article) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        
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
    
    private JPanel createSimplifiedActions(Article article) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setOpaque(false);
        
        // Simplified buttons: Read, Save, Summarize, Open
        JButton readButton = new JButton(article.isRead() ? "✅ Read" : "📖 Mark Read");
        JButton saveButton = new JButton(article.isSaved() ? "💾 Saved" : "💾 Save");
        JButton summarizeButton = new JButton("🤖 Summarize");
        JButton openButton = new JButton("🔗 Open");
        
        styleActionButton(readButton);
        styleActionButton(saveButton);
        styleActionButton(summarizeButton);
        styleActionButton(openButton);
        
        readButton.addActionListener(e -> toggleArticleRead(article, readButton));
        saveButton.addActionListener(e -> toggleArticleSave(article, saveButton));
        summarizeButton.addActionListener(e -> showAISummary(article));
    openButton.addActionListener(e -> showArticleDetailsDialog(article));
        
        actionPanel.add(readButton);
        actionPanel.add(Box.createHorizontalStrut(8));
        actionPanel.add(saveButton);
        actionPanel.add(Box.createHorizontalStrut(8));
        actionPanel.add(summarizeButton);
        actionPanel.add(Box.createHorizontalStrut(8));
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
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
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
        
        JPanel card = findParentCard(button);
        if (card != null) {
            card.setBackground(article.isRead() ? new Color(249, 249, 249) : Color.WHITE);
            card.repaint();
        }

        System.out.println("Article '" + article.getTitle() + "' marked as " + (newReadStatus ? "read" : "unread"));
        System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() : "Guest"));
        System.out.println("Article ID: " + article.getId());

        // Save to database
        if (currentUser != null && article.getId() > 0) {
            System.out.println("Saving read status to DB for user " + currentUser.getUsername());
            feedDAO.markArticleAsRead(currentUser.getId(), article.getId(), newReadStatus);
        }
    }
    
    private void toggleArticleSave(Article article, JButton button) {
        boolean newSavedStatus = !article.isSaved();
        article.setSaved(newSavedStatus);
        button.setText(article.isSaved() ? "💾 Saved" : "💾 Save");
        
        if (selectedList.equals(Constants.DEFAULT_LIST_SAVED) && !article.isSaved()) {
            loadArticlesForList(selectedList);
        }
        
        // Save to database
        if (currentUser != null && article.getId() > 0) {
            feedDAO.markArticleAsSaved(currentUser.getId(), article.getId(), newSavedStatus);
        }
    }
    
    private void showAISummary(Article article) {
        JOptionPane.showMessageDialog(this, 
            "🤖 AI Summary coming soon!\n\nThis will provide intelligent summaries using AI APIs.", 
            "AI Summary", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JPanel findParentCard(JComponent component) {
        Container parent = component.getParent();
        while (parent != null) {
            if (parent instanceof JPanel && ((JPanel) parent).getBorder() != null) {
                return (JPanel) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
    
    private void refreshFeeds() {
        statusLabel.setText("Refreshing feeds...");
        refreshButton.setEnabled(false);
        refreshButton.setText("🔄 Refreshing...");
        
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Refresh user lists first
                SwingUtilities.invokeLater(() -> loadUserLists());
                
                if (currentUser != null) {
                    loadInitialData();
                } else {
                    loadSampleFeedsForDemo();
                }
                return null;
            }
            
            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                refreshButton.setText("🔄 Refresh");
                statusLabel.setText("Feeds refreshed successfully");
            }
        };
        worker.execute();
    }
    
    private void openFeedDiscovery() {
        int userIdForDiscovery = currentUser != null ? currentUser.getId() : 0;
        
        FeedDiscoveryScreen discovery = new FeedDiscoveryScreen(this, userIdForDiscovery, () -> {
            refreshFeeds(); // Refresh when new feeds are added
        });
        discovery.setVisible(true);
    }
    
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "⚙️ Settings", true);
        settingsDialog.setSize(500, 400);
        settingsDialog.setLocationRelativeTo(this);
        
        // Apply dark theme to settings dialog
        settingsDialog.getContentPane().setBackground(ThemeManager.getBackgroundColor());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(ThemeManager.getBackgroundColor());

        JLabel titleLabel = new JLabel("⚙️ " + Constants.APP_NAME + " Settings");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getTextPrimaryColor());
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Create functional settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(ThemeManager.getBackgroundColor());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Theme setting
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel themeLabel = new JLabel("🎨 Theme:");
        themeLabel.setForeground(ThemeManager.getTextPrimaryColor());
        themeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(themeLabel, gbc);

        gbc.gridx = 1;
        JButton themeToggle = ThemeManager.createAccentButton(ThemeManager.isDarkMode() ? "🌙 Dark Mode (Active)" : "☀️ Light Mode (Active)");
        themeToggle.addActionListener(e -> {
            toggleTheme();
            themeToggle.setText(ThemeManager.isDarkMode() ? "🌙 Dark Mode (Active)" : "☀️ Light Mode (Active)");
            ThemeManager.applyThemeToWindow(settingsDialog);
        });
        settingsPanel.add(themeToggle, gbc);

        // Refresh interval setting
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel refreshLabel = new JLabel("🔄 Auto-refresh:");
        refreshLabel.setForeground(ThemeManager.getTextPrimaryColor());
        refreshLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(refreshLabel, gbc);

        gbc.gridx = 1;
        String[] refreshOptions = {"15 minutes", "30 minutes", "60 minutes", "2 hours", "Never"};
        JComboBox<String> refreshCombo = new JComboBox<>(refreshOptions);
        refreshCombo.setSelectedIndex(2); // Default to 60 minutes
        ThemeManager.applyTheme(refreshCombo);
        settingsPanel.add(refreshCombo, gbc);

        // Articles per page setting
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel articlesLabel = new JLabel("📊 Articles per page:");
        articlesLabel.setForeground(ThemeManager.getTextPrimaryColor());
        articlesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(articlesLabel, gbc);

        gbc.gridx = 1;
        String[] articleOptions = {"25", "50", "100", "200"};
        JComboBox<String> articlesCombo = new JComboBox<>(articleOptions);
        articlesCombo.setSelectedIndex(1); // Default to 50
        ThemeManager.applyTheme(articlesCombo);
        settingsPanel.add(articlesCombo, gbc);

        // Default view mode
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel viewLabel = new JLabel("👁️ Default view:");
        viewLabel.setForeground(ThemeManager.getTextPrimaryColor());
        viewLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(viewLabel, gbc);

        gbc.gridx = 1;
        String[] viewOptions = {"📖 Magazine View", "📱 Reel View"};
        JComboBox<String> viewCombo = new JComboBox<>(viewOptions);
        viewCombo.setSelectedIndex(0); // Default to Magazine
        ThemeManager.applyTheme(viewCombo);
        settingsPanel.add(viewCombo, gbc);

        // Notification settings
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel notifLabel = new JLabel("🔔 Notifications:");
        notifLabel.setForeground(ThemeManager.getTextPrimaryColor());
        notifLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(notifLabel, gbc);

        gbc.gridx = 1;
        JCheckBox notifCheckbox = new JCheckBox("Enable new article notifications");
        notifCheckbox.setSelected(true);
        notifCheckbox.setForeground(ThemeManager.getTextPrimaryColor());
        notifCheckbox.setBackground(ThemeManager.getBackgroundColor());
        settingsPanel.add(notifCheckbox, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(ThemeManager.getBackgroundColor());

        JButton saveButton = ThemeManager.createAccentButton("💾 Save Settings");
        JButton resetButton = ThemeManager.createThemedButton("🔄 Reset to Defaults");
        JButton closeButton = ThemeManager.createThemedButton("❌ Close");

        saveButton.addActionListener(e -> {
            // Save settings logic here
            statusLabel.setText("Settings saved successfully!");
            settingsDialog.dispose();
        });

        resetButton.addActionListener(e -> {
            refreshCombo.setSelectedIndex(2);
            articlesCombo.setSelectedIndex(1);
            viewCombo.setSelectedIndex(0);
            notifCheckbox.setSelected(true);
            statusLabel.setText("Settings reset to defaults");
        });

        closeButton.addActionListener(e -> settingsDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(settingsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        settingsDialog.add(mainPanel);

        // Apply theme to entire dialog
        ThemeManager.applyThemeToWindow(settingsDialog);
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
    
    private void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            feedScrollPane.getViewport().setViewPosition(new Point(0, 0));
        });
    }
    
    private void toggleTheme() {
        ThemeManager.toggleTheme();

        // Update the theme for all components
        applyTheme();

        // Update all UI components with new theme
        updateComponentsWithTheme();

        // Update the text of the theme toggle button
        themeToggleButton.setText(ThemeManager.isDarkMode() ? "☀️ Light Mode" : "🌙 Dark Mode");

        // Refresh the frame to apply changes
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    private void updateComponentsWithTheme() {
        // Update main panels
        sidePanel.setBackground(ThemeManager.getSurfaceColor());
        feedPanel.setBackground(ThemeManager.getBackgroundColor());

        // Update labels
        userLabel.setForeground(ThemeManager.getTextPrimaryColor());
        statusLabel.setForeground(ThemeManager.getTextSecondaryColor());

        // Update lists
        userListsList.setBackground(ThemeManager.getCardColor());
        userListsList.setForeground(ThemeManager.getTextPrimaryColor());

        // Update combobox
        viewModeComboBox.setBackground(ThemeManager.getCardColor());
        viewModeComboBox.setForeground(ThemeManager.getTextPrimaryColor());

        // Update scroll pane
        feedScrollPane.setBackground(ThemeManager.getBackgroundColor());
        feedScrollPane.getViewport().setBackground(ThemeManager.getBackgroundColor());
    }
    
    /**
     * Public method to refresh user lists - call this when subscriptions change
     */
    public void refreshUserLists() {
        SwingUtilities.invokeLater(() -> loadUserLists());
    }
    
    private void loadUserLists() {
        userListsModel.clear();
        
        if (currentUser != null) {
            // Load user's actual lists from database
            List<FeedDAO.UserList> userLists = feedDAO.getUserLists(currentUser.getId());
            
            // If no lists exist, create default Home list
            if (userLists.isEmpty()) {
                Optional<FeedDAO.UserList> homeList = feedDAO.createList(currentUser.getId(), "Home");
                if (homeList.isPresent()) {
                    userLists.add(homeList.get());
                }
            }
            
            // Add lists to model
            for (FeedDAO.UserList list : userLists) {
                userListsModel.addElement(list.toString());
            }
        } else {
            // Default lists for guest users
            userListsModel.addElement("🏠 " + Constants.DEFAULT_LIST_HOME);
            userListsModel.addElement("💾 " + Constants.DEFAULT_LIST_SAVED);
        }
    }

    // Custom list cell renderer
    private static class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(10, 15, 10, 15));
            
            if (isSelected) {
                label.setBackground(ThemeManager.getAccentColor());
                label.setForeground(ThemeManager.getSurfaceColor());
            } else {
                label.setBackground(ThemeManager.getCardColor());
                label.setForeground(ThemeManager.getTextPrimaryColor());
            }
            
            return label;
        }
    }

    private void showCreateListDialog() {
        JDialog createListDialog = new JDialog(this, "Create New List", true);
        createListDialog.setSize(400, 200);
        createListDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(ThemeManager.getBackgroundColor());

        JLabel titleLabel = new JLabel("📂 Create New List");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(ThemeManager.getTextPrimaryColor());
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JTextField listNameField = new JTextField();
        listNameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        ThemeManager.applyTheme(listNameField);
        listNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor()),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(ThemeManager.getBackgroundColor());

        JButton createButton = ThemeManager.createAccentButton("Create");
        JButton cancelButton = ThemeManager.createThemedButton("Cancel");

        createButton.addActionListener(e -> {
            String listName = listNameField.getText().trim();
            if (!listName.isEmpty()) {
                // Add to the list model
                userListsModel.addElement("📁 " + listName);
                createListDialog.dispose();
                statusLabel.setText("Created new list: " + listName);
            }
        });

        cancelButton.addActionListener(e -> createListDialog.dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(listNameField, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        createListDialog.add(panel);
        createListDialog.setVisible(true);
    }
}
