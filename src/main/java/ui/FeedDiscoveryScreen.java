package ui;

import db.FeedDAO;
import db.models.Feed;
import rss.FeedParser;
import utils.Constants;
import utils.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feed Discovery Screen for browsing and subscribing to curated RSS sources
 * This replaces the confusing category/list mixing with clear discovery UX
 */
public class FeedDiscoveryScreen extends JDialog {
    private final FeedDAO feedDAO;
    private final int userId;
    private final Runnable onFeedAdded;
    
    private JTextField searchField;
    private JList<String> categoryList;
    private JPanel feedsPanel;
    private JScrollPane feedsScrollPane;
    private JLabel statusLabel;
    
    // Curated feeds by category
    private Map<String, List<CuratedFeed>> curatedFeeds;
    
    public FeedDiscoveryScreen(Frame parent, int userId, Runnable onFeedAdded) {
        super(parent, "Discover RSS Feeds", true);
        this.feedDAO = new FeedDAO();
        this.userId = userId;
        this.onFeedAdded = onFeedAdded;
        
        initializeCuratedFeeds();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Load initial category
        loadFeedsForCategory(Constants.CATEGORY_NEWS);
    }
    
    private void initializeCuratedFeeds() {
        curatedFeeds = new HashMap<>();
        
        // News Sources
        List<CuratedFeed> newsFeeds = new ArrayList<>();
        newsFeeds.add(new CuratedFeed("BBC News", "https://feeds.bbci.co.uk/news/world/rss.xml", "Latest breaking news and top stories", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("Reuters", "https://feeds.reuters.com/reuters/topNews", "International breaking news and headlines", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("Associated Press", "https://feeds.apnews.com/rss/apf-topnews", "Breaking news and latest headlines", Constants.CATEGORY_NEWS));
        newsFeeds.add(new CuratedFeed("CNN", "http://rss.cnn.com/rss/edition.rss", "CNN international news", Constants.CATEGORY_NEWS));
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
        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Category list
        DefaultListModel<String> categoryModel = new DefaultListModel<>();
        for (String category : Constants.DISCOVERY_CATEGORIES) {
            categoryModel.addElement(category);
        }
        categoryList = new JList<>(categoryModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setSelectedIndex(0); // Select News by default
        categoryList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        // Feeds panel
        feedsPanel = new JPanel();
        feedsPanel.setLayout(new BoxLayout(feedsPanel, BoxLayout.Y_AXIS));
        feedsPanel.setBackground(Color.WHITE);
        
        feedsScrollPane = new JScrollPane(feedsPanel);
        feedsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Status label
        statusLabel = new JLabel("Browse curated RSS sources by category");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        headerPanel.setBackground(new Color(248, 249, 250));
        
        JLabel titleLabel = new JLabel("🔍 Discover RSS Feeds");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        searchPanel.add(searchField, BorderLayout.CENTER);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // Left sidebar with categories
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(150, 0));
        sidebarPanel.setBorder(new EmptyBorder(0, 0, 0, 20));
        
        JLabel categoriesLabel = new JLabel("Categories");
        categoriesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        categoriesLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        sidebarPanel.add(categoriesLabel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        
        // Right content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(feedsScrollPane, BorderLayout.CENTER);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);
        
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
        searchField.addActionListener(e -> performSearch());
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
