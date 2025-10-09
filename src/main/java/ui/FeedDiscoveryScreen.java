package ui;

import db.FeedDAO;
import db.models.Feed;
import rss.FeedParser;
import rss.RSSSearchService;
import utils.Constants;
import utils.ThemeManager;
import utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced Feed Discovery Screen with powerful search capabilities
 * Can search any website, YouTube channels, and curated RSS sources
 */
public class FeedDiscoveryScreen extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(FeedDiscoveryScreen.class);
    
    private final FeedDAO feedDAO;
    private final int userId;
    private final Runnable onFeedAdded;
    
    private JTextField searchField;
    private JButton searchButton;
    private JList<String> categoryList;
    private JPanel feedsPanel;
    private JPanel liveSearchResultsPanel;  // Separate panel for live search results
    private JScrollPane feedsScrollPane;
    private JLabel statusLabel;
    private JTabbedPane mainTabs;

    // Curated feeds by category
    private Map<String, List<CuratedFeed>> curatedFeeds;
    private List<RSSSearchService.SearchResult> currentSearchResults;

    public FeedDiscoveryScreen(Frame parent, int userId, Runnable onFeedAdded) {
        super(parent, "🔍 Discover RSS Feeds", true);
        this.feedDAO = new FeedDAO();
        this.userId = userId;
        this.onFeedAdded = onFeedAdded;
        this.currentSearchResults = new ArrayList<>();

        // Apply dark theme
        applyTheme();

        initializeCuratedFeeds();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Load initial category
        loadFeedsForCategory(Constants.CATEGORY_NEWS);
    }
    
    private void applyTheme() {
        // Apply dark theme to the entire dialog
        getContentPane().setBackground(ThemeManager.getBackgroundColor());
        ThemeManager.applyThemeToWindow(this);
    }

    private void initializeCuratedFeeds() {
        curatedFeeds = new HashMap<>();
        
        // News Sources - Enhanced with Al Jazeera and more
        List<CuratedFeed> newsFeeds = new ArrayList<>();
        newsFeeds.add(new CuratedFeed("BBC News", "https://feeds.bbci.co.uk/news/world/rss.xml", "Latest breaking news and top stories", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("Al Jazeera English", "https://www.aljazeera.com/xml/rss/all.xml", "Independent news from Middle East perspective", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("Reuters", "https://feeds.reuters.com/reuters/topNews", "International breaking news and headlines", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("Associated Press", "https://feeds.apnews.com/rss/apf-topnews", "Breaking news and latest headlines", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("CNN International", "http://rss.cnn.com/rss/edition.rss", "CNN international news", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("The Guardian", "https://www.theguardian.com/world/rss", "Guardian world news", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("New York Times", "https://rss.nytimes.com/services/xml/rss/nyt/World.xml", "NYT World News", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("NPR News", "https://feeds.npr.org/1001/rss.xml", "National Public Radio news", Constants.CATEGORY_NEWS));
        curatedFeeds.put(Constants.CATEGORY_NEWS, newsFeeds);
        
        // Tech Sources
        List<CuratedFeed> techFeeds = new ArrayList<>();
        techFeeds.add(new CuratedFeed("TechCrunch", "https://techcrunch.com/feed/", "Latest technology news and startup information", Constants.CATEGORY_TECH));
        techFeeds.add(new CuratedFeed("Ars Technica", "http://feeds.arstechnica.com/arstechnica/index", "In-depth technology analysis and reviews", Constants.CATEGORY_TECH));
        techFeeds.add(new CuratedFeed("The Verge", "https://www.theverge.com/rss/index.xml", "Technology, science, art, and culture", Constants.CATEGORY_TECH));
        techFeeds.add(new CuratedFeed("Wired", "https://www.wired.com/feed/rss", "Technology trends and digital culture", Constants.CATEGORY_TECH));
        techFeeds.add(new CuratedFeed("Hacker News", "https://hnrss.org/frontpage", "Tech community news and discussions", Constants.CATEGORY_TECH));
        curatedFeeds.put(Constants.CATEGORY_TECH, techFeeds);
        
        // Sports Sources
        List<CuratedFeed> sportsFeeds = new ArrayList<>();
        sportsFeeds.add(new CuratedFeed("ESPN", "https://www.espn.com/espn/rss/news", "Sports news, scores, and highlights", Constants.CATEGORY_SPORTS));
        sportsFeeds.add(new CuratedFeed("Sports Illustrated", "https://www.si.com/rss/si_topstories.rss", "Sports news and analysis", Constants.CATEGORY_SPORTS));
        sportsFeeds.add(new CuratedFeed("Sky Sports", "http://www.skysports.com/rss/12040", "UK sports news and updates", Constants.CATEGORY_SPORTS));
        curatedFeeds.put(Constants.CATEGORY_SPORTS, sportsFeeds);
        
        // Science Sources
        List<CuratedFeed> scienceFeeds = new ArrayList<>();
        scienceFeeds.add(new CuratedFeed("NASA News", "https://www.nasa.gov/rss/dyn/breaking_news.rss", "Latest space and astronomy news", Constants.CATEGORY_SCIENCE));
        scienceFeeds.add(new CuratedFeed("Scientific American", "http://rss.sciam.com/ScientificAmerican-Global", "Science news and research", Constants.CATEGORY_SCIENCE));
        scienceFeeds.add(new CuratedFeed("Nature", "http://feeds.nature.com/nature/rss/current", "Leading science journal", Constants.CATEGORY_SCIENCE));
        curatedFeeds.put(Constants.CATEGORY_SCIENCE, scienceFeeds);
        
        // Entertainment Sources
        List<CuratedFeed> entertainmentFeeds = new ArrayList<>();
        entertainmentFeeds.add(new CuratedFeed("Entertainment Weekly", "https://ew.com/feed/", "Celebrity news and entertainment updates", Constants.CATEGORY_ENTERTAINMENT));
        entertainmentFeeds.add(new CuratedFeed("Variety", "https://variety.com/feed/", "Entertainment industry news", Constants.CATEGORY_ENTERTAINMENT));
        curatedFeeds.put(Constants.CATEGORY_ENTERTAINMENT, entertainmentFeeds);
        
        // Business & Finance Sources
        List<CuratedFeed> businessFeeds = new ArrayList<>();
        businessFeeds.add(new CuratedFeed("Forbes", "https://www.forbes.com/real-time/feed2/", "Business news and insights", Constants.CATEGORY_BUSINESS));
        businessFeeds.add(new CuratedFeed("Business Insider", "https://feeds.businessinsider.com/custom/all", "Business and finance news", Constants.CATEGORY_BUSINESS));
        curatedFeeds.put(Constants.CATEGORY_BUSINESS, businessFeeds);
    }
    
    private void initializeComponents() {
        // Search field with proper dark theme
        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        ThemeManager.applyTheme(searchField);

        // Add real-time search listener
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private Timer searchTimer = new Timer(500, e -> performLiveSearch());

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }

            private void scheduleSearch() {
                searchTimer.restart();
            }
        });

        // Search button with dark theme
        searchButton = ThemeManager.createAccentButton("🔍 Search");

        // Category list with dark theme
        DefaultListModel<String> categoryModel = new DefaultListModel<>();
        for (String category : Constants.DISCOVERY_CATEGORIES) {
            categoryModel.addElement(category);
        }
        categoryList = new JList<>(categoryModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setSelectedIndex(0);
        categoryList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        ThemeManager.applyTheme(categoryList);

        // Feeds panel with dark theme
        feedsPanel = new JPanel();
        feedsPanel.setLayout(new BoxLayout(feedsPanel, BoxLayout.Y_AXIS));
        feedsPanel.setBackground(ThemeManager.getBackgroundColor());

        feedsScrollPane = new JScrollPane(feedsPanel);
        feedsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ThemeManager.applyTheme(feedsScrollPane);

        // Status label with dark theme
        statusLabel = new JLabel("Browse curated RSS sources by category");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(ThemeManager.getTextSecondaryColor());

        // Main tabs with dark theme
        mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        mainTabs.setBackground(ThemeManager.getCardColor());
        mainTabs.setForeground(ThemeManager.getTextPrimaryColor());

        // Curated Feeds tab with dark theme
        JPanel curatedFeedsPanel = ThemeManager.createThemedPanel();
        curatedFeedsPanel.setLayout(new BorderLayout());
        curatedFeedsPanel.add(feedsScrollPane, BorderLayout.CENTER);
        curatedFeedsPanel.add(statusLabel, BorderLayout.SOUTH);

        mainTabs.addTab("📚 Curated Feeds", curatedFeedsPanel);

        // Add live search tab
        JPanel liveSearchPanel = createLiveSearchPanel();
        mainTabs.addTab("🌐 Live Search", liveSearchPanel);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel with dark theme
        JPanel headerPanel = ThemeManager.createThemedPanel();
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("🔍 Discover RSS Feeds");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(ThemeManager.getAccentColor());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(ThemeManager.getTextPrimaryColor());
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Main content panel with dark theme
        JPanel mainPanel = ThemeManager.createThemedPanel();
        mainPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        mainPanel.setLayout(new BorderLayout());
        
        // Left sidebar with categories
        JPanel sidebarPanel = ThemeManager.createThemedPanel();
        sidebarPanel.setPreferredSize(new Dimension(150, 0));
        sidebarPanel.setBorder(new EmptyBorder(0, 0, 0, 20));
        sidebarPanel.setLayout(new BorderLayout());
        
        JLabel categoriesLabel = new JLabel("Categories");
        categoriesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        categoriesLabel.setForeground(ThemeManager.getTextPrimaryColor());
        categoriesLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        sidebarPanel.add(categoriesLabel, BorderLayout.NORTH);
        
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryScrollPane.setBackground(ThemeManager.getCardColor());
        categoryScrollPane.getViewport().setBackground(ThemeManager.getCardColor());
        categoryScrollPane.setBorder(null);
        sidebarPanel.add(categoryScrollPane, BorderLayout.CENTER);
        
        // Right content area with tabs
        JPanel contentPanel = ThemeManager.createThemedPanel();
        contentPanel.setLayout(new BorderLayout());
        
        // Apply theme to tabs
        ThemeManager.applyTheme(mainTabs);
        contentPanel.add(mainTabs, BorderLayout.CENTER);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventListeners() {
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    loadFeedsForCategory(selectedCategory);
                }
            }
        });
        
        // Search functionality
        searchButton.addActionListener(e -> performSearch());
    }
    
    private void loadFeedsForCategory(String category) {
        feedsPanel.removeAll();
        statusLabel.setText("Loading " + category + " feeds...");
        
        List<CuratedFeed> feeds = curatedFeeds.get(category);
        if (feeds != null) {
            for (CuratedFeed feed : feeds) {
                feedsPanel.add(createFeedDiscoveryCard(feed));
                feedsPanel.add(Box.createVerticalStrut(10));
            }
            statusLabel.setText(feeds.size() + " curated " + category + " sources available");
        } else {
            JLabel noFeedsLabel = new JLabel("No curated feeds available for " + category);
            noFeedsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            feedsPanel.add(noFeedsLabel);
            statusLabel.setText("No feeds available");
        }
        
        feedsPanel.revalidate();
        feedsPanel.repaint();
    }
    
    private JPanel createFeedDiscoveryCard(CuratedFeed curatedFeed) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 230, 235), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Feed info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(curatedFeed.getName());
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        JLabel descLabel = new JLabel("<html>" + curatedFeed.getDescription() + "</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        descLabel.setForeground(Color.GRAY);
        
        JLabel urlLabel = new JLabel(curatedFeed.getUrl());
        urlLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        urlLabel.setForeground(new Color(108, 117, 125));
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(urlLabel);
        
        // Subscribe button
        JButton subscribeButton = new JButton("➕ Add to My Feeds");
        subscribeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        subscribeButton.setBackground(new Color(40, 167, 69));
        subscribeButton.setForeground(Color.WHITE);
        subscribeButton.setFocusPainted(false);
        subscribeButton.setBorder(new EmptyBorder(8, 16, 8, 16));
        subscribeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        subscribeButton.addActionListener(e -> addFeedToUserList(curatedFeed, subscribeButton));
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(subscribeButton, BorderLayout.EAST);
        
        return card;
    }
    
    private void addFeedToUserList(CuratedFeed curatedFeed, JButton button) {
        button.setEnabled(false);
        button.setText("Adding...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // First check if this source already exists
                if (feedDAO.findSourceByUrl(curatedFeed.getUrl()).isPresent()) {
                    return true; // Source already exists, just need to subscribe
                }
                
                // Create new source
                Feed feed = new Feed();
                feed.setTitle(curatedFeed.getName());
                feed.setUrl(curatedFeed.getUrl());
                feed.setDescription(curatedFeed.getDescription());
                feed.setCategory(curatedFeed.getCategory());
                feed.setCreatedAt(LocalDateTime.now());
                
                return feedDAO.createSource(feed).isPresent();
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        button.setText("✅ Added!");
                        button.setBackground(new Color(108, 117, 125));
                        statusLabel.setText("Feed added successfully!");
                        
                        // Notify parent that a feed was added
                        if (onFeedAdded != null) {
                            onFeedAdded.run();
                        }
                        
                        // Re-enable button after delay
                        Timer timer = new Timer(2000, evt -> {
                            button.setEnabled(true);
                            button.setText("➕ Add to My Feeds");
                            button.setBackground(new Color(40, 167, 69));
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        button.setEnabled(true);
                        button.setText("➕ Add to My Feeds");
                        statusLabel.setText("Failed to add feed. Please try again.");
                    }
                } catch (Exception e) {
                    button.setEnabled(true);
                    button.setText("➕ Add to My Feeds");
                    statusLabel.setText("Error adding feed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            // Show all feeds for current category
            String selectedCategory = categoryList.getSelectedValue();
            if (selectedCategory != null) {
                loadFeedsForCategory(selectedCategory);
            }
            return;
        }
        
        feedsPanel.removeAll();
        statusLabel.setText("Searching for '" + searchTerm + "'...");
        
        List<CuratedFeed> searchResults = new ArrayList<>();
        
        // Search across all categories
        for (List<CuratedFeed> categoryFeeds : curatedFeeds.values()) {
            for (CuratedFeed feed : categoryFeeds) {
                if (feed.getName().toLowerCase().contains(searchTerm) ||
                    feed.getDescription().toLowerCase().contains(searchTerm) ||
                    feed.getCategory().toLowerCase().contains(searchTerm)) {
                    searchResults.add(feed);
                }
            }
        }
        
        if (searchResults.isEmpty()) {
            JLabel noResultsLabel = new JLabel("<html><center>No feeds found for '" + searchTerm + "'<br>Try a different search term</center></html>");
            noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noResultsLabel.setForeground(Color.GRAY);
            feedsPanel.add(noResultsLabel);
        } else {
            for (CuratedFeed feed : searchResults) {
                feedsPanel.add(createFeedDiscoveryCard(feed));
                feedsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        statusLabel.setText("Found " + searchResults.size() + " feeds matching '" + searchTerm + "'");
        feedsPanel.revalidate();
        feedsPanel.repaint();
    }
    
    private JPanel createLiveSearchPanel() {
        JPanel liveSearchPanel = ThemeManager.createThemedPanel();
        liveSearchPanel.setLayout(new BorderLayout());

        // Live search instructions
        JLabel instructionsLabel = new JLabel("<html><center>" +
            "<h3 style='color: " + toHexColor(ThemeManager.getAccentColor()) + ";'>🌐 Live Internet RSS Search</h3>" +
            "<p style='font-size: 13px;'>Enter any website URL, YouTube channel, or search term</p>" +
            "<p style='font-size: 12px; color: " + toHexColor(ThemeManager.getTextSecondaryColor()) + ";'>Examples: reddit.com, @mkbhd, techcrunch, nasa</p>" +
            "</center></html>");
        instructionsLabel.setForeground(ThemeManager.getTextPrimaryColor());
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionsLabel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create and store the search results panel
        liveSearchResultsPanel = new JPanel();
        liveSearchResultsPanel.setLayout(new BoxLayout(liveSearchResultsPanel, BoxLayout.Y_AXIS));
        liveSearchResultsPanel.setBackground(ThemeManager.getBackgroundColor());

        JScrollPane searchScrollPane = new JScrollPane(liveSearchResultsPanel);
        searchScrollPane.setBackground(ThemeManager.getBackgroundColor());
        searchScrollPane.getViewport().setBackground(ThemeManager.getBackgroundColor());
        searchScrollPane.setBorder(null);
        ThemeManager.applyTheme(searchScrollPane);

        liveSearchPanel.add(instructionsLabel, BorderLayout.NORTH);
        liveSearchPanel.add(searchScrollPane, BorderLayout.CENTER);

        return liveSearchPanel;
    }
    
    // Helper method to convert Color to hex string for HTML
    private String toHexColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void performLiveSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            return;
        }

        statusLabel.setText("🔍 Searching the internet for RSS feeds...");

        SwingWorker<List<RSSSearchService.SearchResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RSSSearchService.SearchResult> doInBackground() throws Exception {
                return RSSSearchService.searchFeeds(searchTerm);
            }

            @Override
            protected void done() {
                try {
                    List<RSSSearchService.SearchResult> results = get();
                    displayLiveSearchResults(results);
                } catch (Exception e) {
                    statusLabel.setText("Search failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void displayLiveSearchResults(List<RSSSearchService.SearchResult> results) {
        liveSearchResultsPanel.removeAll();

        if (results.isEmpty()) {
            JLabel noResultsLabel = new JLabel("<html><center>" +
                "<h3 style='color: " + toHexColor(ThemeManager.getTextPrimaryColor()) + ";'>No RSS feeds found</h3>" +
                "<p style='color: " + toHexColor(ThemeManager.getTextSecondaryColor()) + ";'>Try a different website or search term</p>" +
                "</center></html>");
            noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noResultsLabel.setForeground(ThemeManager.getTextSecondaryColor());
            liveSearchResultsPanel.add(noResultsLabel);
        } else {
            for (RSSSearchService.SearchResult result : results) {
                liveSearchResultsPanel.add(createSearchResultCard(result));
                liveSearchResultsPanel.add(Box.createVerticalStrut(10));
            }
        }

        statusLabel.setText("✅ Found " + results.size() + " RSS feeds");
        liveSearchResultsPanel.revalidate();
        liveSearchResultsPanel.repaint();
    }

    private JPanel createSearchResultCard(RSSSearchService.SearchResult result) {
        JPanel card = ThemeManager.createThemedCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Feed info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(result.getTitle());
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        nameLabel.setForeground(ThemeManager.getTextPrimaryColor());

        JLabel descLabel = new JLabel("<html>" + result.getDescription() + "</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        descLabel.setForeground(ThemeManager.getTextSecondaryColor());

        JLabel urlLabel = new JLabel(result.getUrl());
        urlLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        urlLabel.setForeground(ThemeManager.getAccentColor());

        JLabel typeLabel = new JLabel("📡 " + result.getType().toUpperCase() + " • " + result.getCategory());
        typeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        typeLabel.setForeground(ThemeManager.getAccentColor());

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(typeLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(urlLabel);

        // Subscribe button
        JButton subscribeButton = ThemeManager.createAccentButton("➕ Subscribe");
        subscribeButton.addActionListener(e -> subscribeToSearchResult(result, subscribeButton));

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(subscribeButton, BorderLayout.EAST);

        return card;
    }

    private void subscribeToSearchResult(RSSSearchService.SearchResult result, JButton button) {
        // Show list selection dialog first
        showListSelectionDialog(result, button);
    }
    
    private void showListSelectionDialog(RSSSearchService.SearchResult result, JButton originalButton) {
        if (userId <= 0) {
            statusLabel.setText("Please log in to subscribe to feeds");
            return;
        }
        
        // Create dialog
        JDialog listDialog = new JDialog(this, "Subscribe to Feed", true);
        listDialog.setSize(400, 300);
        listDialog.setLocationRelativeTo(this);
        listDialog.getContentPane().setBackground(ThemeManager.getBackgroundColor());
        
        JPanel mainPanel = ThemeManager.createThemedPanel();
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("<html><center><h3>Subscribe to:</h3><b>" + result.getTitle() + "</b></center></html>");
        titleLabel.setForeground(ThemeManager.getTextPrimaryColor());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // List selection
        JPanel listPanel = ThemeManager.createThemedPanel();
        listPanel.setLayout(new BorderLayout());
        
        JLabel listLabel = new JLabel("Choose a list:");
        listLabel.setForeground(ThemeManager.getTextPrimaryColor());
        listLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        // Get user's lists
        java.util.List<FeedDAO.UserList> userLists = feedDAO.getUserLists(userId);
        if (userLists.isEmpty()) {
            // Create default Home list if none exist
            Optional<FeedDAO.UserList> homeList = feedDAO.createList(userId, "Home");
            if (homeList.isPresent()) {
                userLists.add(homeList.get());
            }
        }
        
        DefaultListModel<FeedDAO.UserList> listModel = new DefaultListModel<>();
        for (FeedDAO.UserList list : userLists) {
            listModel.addElement(list);
        }
        
        JList<FeedDAO.UserList> listsList = new JList<>(listModel);
        listsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listsList.setSelectedIndex(0); // Select first (Home) by default
        listsList.setBackground(ThemeManager.getCardColor());
        listsList.setForeground(ThemeManager.getTextPrimaryColor());
        
        JScrollPane listsScrollPane = new JScrollPane(listsList);
        listsScrollPane.setPreferredSize(new Dimension(300, 120));
        ThemeManager.applyTheme(listsScrollPane);
        
        listPanel.add(listLabel, BorderLayout.NORTH);
        listPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        listPanel.add(listsScrollPane, BorderLayout.SOUTH);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton subscribeBtn = ThemeManager.createAccentButton("✅ Subscribe");
        JButton cancelBtn = ThemeManager.createThemedButton("Cancel");
        JButton newListBtn = ThemeManager.createThemedButton("+ New List");
        
        subscribeBtn.addActionListener(e -> {
            FeedDAO.UserList selectedList = listsList.getSelectedValue();
            if (selectedList != null) {
                listDialog.dispose();
                performSubscription(result, selectedList, originalButton);
            }
        });
        
        cancelBtn.addActionListener(e -> listDialog.dispose());
        
        newListBtn.addActionListener(e -> {
            String newListName = JOptionPane.showInputDialog(listDialog, 
                "Enter new list name:", "Create New List", 
                JOptionPane.QUESTION_MESSAGE);
            if (newListName != null && !newListName.trim().isEmpty()) {
                Optional<FeedDAO.UserList> newList = feedDAO.createList(userId, newListName.trim());
                if (newList.isPresent()) {
                    listModel.addElement(newList.get());
                    listsList.setSelectedValue(newList.get(), true);
                }
            }
        });
        
        buttonPanel.add(newListBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(subscribeBtn);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        listDialog.add(mainPanel);
        listDialog.setVisible(true);
    }
    
    private void performSubscription(RSSSearchService.SearchResult result, FeedDAO.UserList selectedList, JButton button) {
        button.setEnabled(false);
        button.setText("Adding...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // First, check if source already exists
                    Optional<Feed> existingFeed = feedDAO.findSourceByUrl(result.getUrl());
                    
                    int sourceId;
                    if (existingFeed.isPresent()) {
                        sourceId = existingFeed.get().getId();
                        logger.info("Feed already exists with ID: {}", sourceId);
                    } else {
                        // Create new feed
                        Feed feed = new Feed();
                        feed.setTitle(result.getTitle());
                        feed.setUrl(result.getUrl());
                        feed.setDescription(result.getDescription());
                        feed.setCategory(result.getCategory());
                        feed.setCreatedAt(LocalDateTime.now());

                        Optional<Feed> createdFeed = feedDAO.createSource(feed);
                        if (!createdFeed.isPresent()) {
                            logger.error("Failed to create feed: {}", result.getTitle());
                            return false;
                        }
                        sourceId = createdFeed.get().getId();
                        logger.info("Created new feed with ID: {}", sourceId);
                    }

                    // Subscribe to the selected list
                    boolean subscribed = feedDAO.subscribeToFeedInList(selectedList.getId(), sourceId);
                    if (!subscribed) {
                        logger.warn("Failed to subscribe to feed {} in list {}", sourceId, selectedList.getName());
                        return false;
                    }

                    return true;
                } catch (Exception e) {
                    logger.error("Error subscribing to feed: " + e.getMessage(), e);
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        button.setText("✅ Subscribed!");
                        button.setBackground(ThemeManager.getCardColor());
                        button.setForeground(ThemeManager.getTextPrimaryColor());
                        statusLabel.setText("Successfully subscribed to " + result.getTitle() + " in " + selectedList.getName());

                        // Notify parent that a feed was added
                        if (onFeedAdded != null) {
                            onFeedAdded.run();
                        }

                        // Re-enable button after delay
                        Timer timer = new Timer(3000, evt -> {
                            button.setEnabled(true);
                            button.setText("➕ Subscribe");
                            button.setBackground(ThemeManager.getAccentColor());
                            button.setForeground(ThemeManager.isDarkMode() ? Color.BLACK : Color.WHITE);
                        });
                        timer.setRepeats(false);
                        timer.start();

                    } else {
                        button.setEnabled(true);
                        button.setText("❌ Failed");
                        statusLabel.setText("Failed to subscribe to feed. Please try again.");

                        Timer timer = new Timer(2000, evt -> {
                            button.setText("➕ Subscribe");
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                } catch (Exception e) {
                    button.setEnabled(true);
                    button.setText("❌ Error");
                    statusLabel.setText("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // Inner class for curated feed data
    private static class CuratedFeed {
        private final String name;
        private final String url;
        private final String description;
        private final String category;
        
        public CuratedFeed(String name, String url, String description, String category) {
            this.name = name;
            this.url = url;
            this.description = description;
            this.category = category;
        }
        
        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
    }
}
