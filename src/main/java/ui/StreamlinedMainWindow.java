package ui;

import auth.AuthController;
import db.models.User;
import db.models.Article;
import db.models.Feed;
import db.FeedDAO;
import rss.FeedParser;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
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
    private JLabel statusLabel;
    private JLabel userLabel;
    private JComboBox<String> viewModeComboBox;
    
    // Current data
    private List<Article> currentArticles;
    private User currentUser;
    private String selectedList = Constants.DEFAULT_LIST_HOME;
    private String currentViewMode = Constants.VIEW_MODE_LIST;
    
    public StreamlinedMainWindow() {
        this.authController = AuthController.getInstance();
        this.feedParser = new FeedParser();
        this.feedDAO = new FeedDAO();
        this.currentUser = authController.getCurrentUser();
        this.currentArticles = new ArrayList<>();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        checkForEmptyState();
        
        setTitle(Constants.APP_NAME + " - RSS Feed Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    private void initializeComponents() {
        // Side panel for user lists and controls
        sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
        sidePanel.setBackground(new Color(248, 249, 250));
        sidePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // User info
        String displayName = currentUser != null ? currentUser.getUsername() : "Guest";
        userLabel = new JLabel("👤 " + displayName);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        userLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // User Lists (simplified - no category confusion)
        userListsModel = new DefaultListModel<>();
        userListsModel.addElement("🏠 " + Constants.DEFAULT_LIST_HOME);
        userListsModel.addElement("💾 " + Constants.DEFAULT_LIST_SAVED);
        
        userListsList = new JList<>(userListsModel);
        userListsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListsList.setSelectedIndex(0);
        userListsList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userListsList.setCellRenderer(new CustomListCellRenderer());
        
        // View mode selector
        viewModeComboBox = new JComboBox<>(new String[]{"📋 List View", "📖 Magazine View", "📱 Reel View"});
        viewModeComboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Control buttons
        refreshButton = new JButton("🔄 Refresh");
        discoverButton = new JButton("🔍 Discover Feeds");
        settingsButton = new JButton("⚙️ Settings");
        logoutButton = new JButton("🚪 Logout");
        
        styleButton(refreshButton);
        styleButton(discoverButton);
        styleButton(settingsButton);
        styleButton(logoutButton);
        
        // Main feed panel
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(Color.WHITE);
        
        feedScrollPane = new JScrollPane(feedPanel);
        feedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Status bar
        statusLabel = new JLabel("Welcome to " + Constants.APP_NAME + "!");
        statusLabel.setBorder(new EmptyBorder(8, 15, 8, 15));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(Color.GRAY);
    }
    
    private void styleButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(248, 249, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
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
        listsLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        listsPanel.add(listsLabel, BorderLayout.NORTH);
        listsPanel.add(new JScrollPane(userListsList), BorderLayout.CENTER);
        
        // View mode panel
        JPanel viewModePanel = new JPanel(new BorderLayout());
        viewModePanel.setOpaque(false);
        viewModePanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel viewLabel = new JLabel("👁️ View");
        viewLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
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
                if (selected.contains("List")) currentViewMode = Constants.VIEW_MODE_LIST;
                else if (selected.contains("Magazine")) currentViewMode = Constants.VIEW_MODE_MAGAZINE;
                else if (selected.contains("Reel")) currentViewMode = Constants.VIEW_MODE_REEL;
                
                loadArticlesForList(selectedList);
            }
        });
        
        refreshButton.addActionListener(e -> refreshFeeds());
        discoverButton.addActionListener(e -> openFeedDiscovery());
        settingsButton.addActionListener(e -> showSettingsDialog());
        logoutButton.addActionListener(e -> performLogout());
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
        
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new EmptyBorder(100, 50, 100, 50));
        
        JLabel welcomeLabel = new JLabel("<html><center>" +
            "<h1>🎯 Welcome to " + Constants.APP_NAME + "!</h1>" +
            "<h3>Discover amazing RSS feeds to get started</h3>" +
            "<p>Browse our curated collection of high-quality sources<br>" +
            "from news, tech, sports, science, and more.</p>" +
            "</center></html>");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton discoverFeedsBtn = new JButton("🔍 Discover RSS Feeds");
        discoverFeedsBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        discoverFeedsBtn.setBackground(new Color(0, 123, 255));
        discoverFeedsBtn.setForeground(Color.WHITE);
        discoverFeedsBtn.setFocusPainted(false);
        discoverFeedsBtn.setBorder(new EmptyBorder(12, 24, 12, 24));
        discoverFeedsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        }
    }
    
    private void loadArticlesForList(String listName) {
        feedPanel.removeAll();
        
        List<Article> filteredArticles = filterArticlesByList(listName);
        
        if (filteredArticles.isEmpty()) {
            showEmptyListState(listName);
        } else {
            switch (currentViewMode) {
                case Constants.VIEW_MODE_LIST:
                    renderListView(filteredArticles);
                    break;
                case Constants.VIEW_MODE_MAGAZINE:
                    renderMagazineView(filteredArticles);
                    break;
                case Constants.VIEW_MODE_REEL:
                    renderReelView(filteredArticles);
                    break;
                default:
                    renderListView(filteredArticles);
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
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new EmptyBorder(80, 40, 80, 40));
        
        String message = listName.equals(Constants.DEFAULT_LIST_SAVED) 
            ? "<html><center><h2>No saved articles</h2><p>Articles you save will appear here</p></center></html>"
            : "<html><center><h2>No articles in " + listName + "</h2><p>Add some RSS feeds to get started!</p></center></html>";
        
        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setForeground(Color.GRAY);
        
        JButton actionBtn = new JButton("🔍 Discover Feeds");
        actionBtn.addActionListener(e -> openFeedDiscovery());
        styleButton(actionBtn);
        
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        emptyPanel.add(actionBtn, BorderLayout.SOUTH);
        
        feedPanel.add(emptyPanel);
    }
    
    private void renderListView(List<Article> articles) {
        for (Article article : articles) {
            feedPanel.add(createArticleCard(article));
            feedPanel.add(Box.createVerticalStrut(12));
        }
    }
    
    private void renderMagazineView(List<Article> articles) {
        JLabel comingSoonLabel = new JLabel("<html><center><h2>📖 Magazine View</h2><p>Beautiful magazine-style reading experience coming soon!</p></center></html>");
        comingSoonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        comingSoonLabel.setForeground(Color.GRAY);
        feedPanel.add(comingSoonLabel);
    }
    
    private void renderReelView(List<Article> articles) {
        JLabel comingSoonLabel = new JLabel("<html><center><h2>📱 Reel View</h2><p>TikTok-style article browsing coming soon!</p></center></html>");
        comingSoonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        comingSoonLabel.setForeground(Color.GRAY);
        feedPanel.add(comingSoonLabel);
    }
    
    private JPanel createArticleCard(Article article) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 230, 235), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(article.isRead() ? new Color(249, 249, 249) : Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        // Article header
        JPanel header = createArticleHeader(article);
        
        // Article title
        JLabel titleLabel = new JLabel("<html><h3 style='margin: 0; line-height: 1.3;'>" + 
            (article.getTitle() != null ? article.getTitle() : "Untitled") + "</h3></html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        // Article content preview
        String description = article.getDescription();
        if (description != null && description.length() > Constants.ARTICLE_PREVIEW_LENGTH) {
            description = description.substring(0, Constants.ARTICLE_PREVIEW_LENGTH) + "...";
        }
        JLabel contentLabel = new JLabel("<html><p style='margin: 8px 0 0 0; color: #666; line-height: 1.4;'>" + 
            (description != null ? description : "No description available") + "</p></html>");
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
        openButton.addActionListener(e -> openArticleInBrowser(article));
        
        actionPanel.add(readButton);
        actionPanel.add(Box.createHorizontalStrut(8));
        actionPanel.add(saveButton);
        actionPanel.add(Box.createHorizontalStrut(8));
        actionPanel.add(summarizeButton);
        actionPanel.add(Box.createHorizontalStrut(8));
        actionPanel.add(openButton);
        
        return actionPanel;
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
        article.setRead(!article.isRead());
        button.setText(article.isRead() ? "✅ Read" : "📖 Mark Read");
        
        JPanel card = findParentCard(button);
        if (card != null) {
            card.setBackground(article.isRead() ? new Color(249, 249, 249) : Color.WHITE);
            card.repaint();
        }
    }
    
    private void toggleArticleSave(Article article, JButton button) {
        article.setSaved(!article.isSaved());
        button.setText(article.isSaved() ? "💾 Saved" : "💾 Save");
        
        if (selectedList.equals(Constants.DEFAULT_LIST_SAVED) && !article.isSaved()) {
            loadArticlesForList(selectedList);
        }
    }
    
    private void showAISummary(Article article) {
        JOptionPane.showMessageDialog(this, 
            "🤖 AI Summary coming soon!\n\nThis will provide intelligent summaries using AI APIs.", 
            "AI Summary", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openArticleInBrowser(Article article) {
        if (article.getUrl() != null && !article.getUrl().isEmpty()) {
            try {
                Desktop.getDesktop().browse(java.net.URI.create(article.getUrl()));
                if (!article.isRead()) {
                    article.setRead(true);
                    loadArticlesForList(selectedList);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Could not open URL: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setSize(450, 350);
        settingsDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel titleLabel = new JLabel("⚙️ " + Constants.APP_NAME + " Settings");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel settingsPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        settingsPanel.add(new JLabel("🔄 Refresh interval: 60 minutes"));
        settingsPanel.add(new JLabel("🚀 Auto-refresh: Enabled"));
        settingsPanel.add(new JLabel("🎨 Theme: System Default"));
        settingsPanel.add(new JLabel("📊 Articles per page: 50"));
        settingsPanel.add(new JLabel("🤖 AI summaries: Available"));
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
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
    
    private void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            feedScrollPane.getViewport().setViewPosition(new Point(0, 0));
        });
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
