package utils;

import java.awt.Color;

public class Constants {
    // Application Information
    public static final String APP_NAME = "FeedHawk";
    public static final String APP_VERSION = "1.0.0";
    public static final String USER_AGENT = APP_NAME + " RSS Reader " + APP_VERSION;

    // Database Configuration
    public static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/feedhawk";
    public static final String DEFAULT_DB_USERNAME = "feedhawk_user";
    public static final String DEFAULT_DB_PASSWORD = "feedhawk_pass";

    // Feed Categories (for discovery and filtering)
    public static final String CATEGORY_NEWS = "News";
    public static final String CATEGORY_TECH = "Tech";
    public static final String CATEGORY_SPORTS = "Sports";
    public static final String CATEGORY_SCIENCE = "Science";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_BUSINESS = "Business";
    public static final String CATEGORY_HEALTH = "Health";
    public static final String CATEGORY_LIFESTYLE = "Lifestyle";
    public static final String CATEGORY_GAMING = "Gaming";
    public static final String CATEGORY_FINANCE = "Finance";

    // UI Constants
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int SIDEBAR_WIDTH = 250;
    public static final int ARTICLE_PREVIEW_LENGTH = 200;

    // Feed Refresh Settings
    public static final int DEFAULT_REFRESH_INTERVAL_MINUTES = 60;
    public static final int MIN_REFRESH_INTERVAL_MINUTES = 15;
    public static final int MAX_REFRESH_INTERVAL_MINUTES = 1440; // 24 hours

    // Network Settings
    public static final int CONNECTION_TIMEOUT_MS = 10000; // 10 seconds
    public static final int READ_TIMEOUT_MS = 15000; // 15 seconds
    public static final int MAX_RETRIES = 3;

    // Pagination and Limits
    public static final int DEFAULT_ARTICLES_PER_PAGE = 50;
    public static final int MAX_ARTICLES_PER_PAGE = 200;
    public static final int RECENT_ARTICLES_LIMIT = 100;
    public static final int ITEMS_PER_LIST_IN_HOME = 10; // For stacked display

    // Password Requirements
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;

    // File Paths
    public static final String CACHE_DIR = "cache";
    public static final String LOGS_DIR = "logs";
    public static final String CONFIG_FILE = "feedhawk.properties";

    // Regex Patterns
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]+$";
    public static final String URL_PATTERN = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    // Error Messages
    public static final String ERROR_DATABASE_CONNECTION = "Unable to connect to database";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_FEED_NOT_FOUND = "Feed not found";
    public static final String ERROR_INVALID_URL = "Invalid URL format";
    public static final String ERROR_NETWORK_TIMEOUT = "Network timeout - please check your connection";

    // Success Messages
    public static final String SUCCESS_USER_CREATED = "User account created successfully";
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_FEED_ADDED = "Feed added successfully";
    public static final String SUCCESS_FEED_UPDATED = "Feed updated successfully";

    // Default Lists (User-created organization)
    public static final String DEFAULT_LIST_HOME = "Home";
    public static final String DEFAULT_LIST_SAVED = "Saved";
    public static final String DEFAULT_LIST_BOOKMARKS = "Bookmarks";
    public static final String DEFAULT_LIST_READ_LATER = "Read Later";

    // Article States
    public static final String ARTICLE_STATE_UNREAD = "unread";
    public static final String ARTICLE_STATE_READ = "read";
    public static final String ARTICLE_STATE_SAVED = "saved";

    // Feed Update Status
    public static final String FEED_STATUS_ACTIVE = "active";
    public static final String FEED_STATUS_INACTIVE = "inactive";
    public static final String FEED_STATUS_ERROR = "error";

    // View Modes - Only Magazine and Reel views (removed list view)
    public static final String VIEW_MODE_MAGAZINE = "magazine";
    public static final String VIEW_MODE_REEL = "reel";

    // Theme Colors - PROPER Dark Mode (Default) - Actually dark and aesthetic
    public static final Color DARK_BACKGROUND = new Color(16, 16, 16);          // Almost black
    public static final Color DARK_SURFACE = new Color(24, 24, 24);             // Very dark gray
    public static final Color DARK_CARD = new Color(32, 32, 32);                // Dark card background
    public static final Color DARK_TEXT_PRIMARY = new Color(255, 255, 255);     // Pure white text
    public static final Color DARK_TEXT_SECONDARY = new Color(170, 170, 170);   // Light gray text
    public static final Color DARK_ACCENT = new Color(255, 149, 0);             // Bright orange accent
    public static final Color DARK_BORDER = new Color(64, 64, 64);              // Dark border
    public static final Color DARK_HOVER = new Color(48, 48, 48);               // Hover state

    // Theme Colors - Light Mode
    public static final Color LIGHT_BACKGROUND = new Color(255, 255, 255);      // Pure white
    public static final Color LIGHT_SURFACE = new Color(248, 249, 250);         // Very light gray
    public static final Color LIGHT_CARD = new Color(255, 255, 255);            // White cards
    public static final Color LIGHT_TEXT_PRIMARY = new Color(0, 0, 0);          // Pure black text
    public static final Color LIGHT_TEXT_SECONDARY = new Color(85, 85, 85);     // Dark gray text
    public static final Color LIGHT_ACCENT = new Color(0, 122, 255);            // Blue accent
    public static final Color LIGHT_BORDER = new Color(200, 200, 200);          // Light border
    public static final Color LIGHT_HOVER = new Color(240, 240, 240);           // Light hover

    // Theme Settings
    public static final boolean DEFAULT_DARK_MODE = true;

    // Search and Discovery
    public static final String YOUTUBE_RSS_BASE = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    public static final String YOUTUBE_USER_RSS_BASE = "https://www.youtube.com/feeds/videos.xml?user=";
    public static final String[] POPULAR_RSS_PROVIDERS = {
        "rss.cnn.com", "feeds.bbci.co.uk", "rss.nytimes.com",
        "feeds.reuters.com", "feeds.washingtonpost.com", "feeds.theguardian.com"
    };

    // AI Summary Settings
    public static final int SUMMARY_MAX_LENGTH = 500;
    public static final String SUMMARY_LANGUAGE = "en";

    // Discovery Categories for Search
    public static final String[] DISCOVERY_CATEGORIES = {
        CATEGORY_NEWS, CATEGORY_TECH, CATEGORY_SPORTS, CATEGORY_SCIENCE,
        CATEGORY_ENTERTAINMENT, CATEGORY_BUSINESS, CATEGORY_HEALTH,
        CATEGORY_LIFESTYLE, CATEGORY_GAMING, CATEGORY_FINANCE
    };

    private Constants() {
        // Prevent instantiation
    }
}
