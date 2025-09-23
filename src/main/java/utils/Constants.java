package utils;

public class Constants {
    // Application Information
    public static final String APP_NAME = "FeedHawk";
    public static final String APP_VERSION = "1.0.0";
    public static final String USER_AGENT = APP_NAME + " RSS Reader " + APP_VERSION;

    // Database Configuration
    public static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/feedhawk";
    public static final String DEFAULT_DB_USERNAME = "feedhawk_user";
    public static final String DEFAULT_DB_PASSWORD = "feedhawk_pass";

    // Feed Categories
    public static final String CATEGORY_TECH = "Tech";
    public static final String CATEGORY_NEWS = "News";
    public static final String CATEGORY_SPORTS = "Sports";
    public static final String CATEGORY_SCIENCE = "Science";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_BUSINESS = "Business";
    public static final String CATEGORY_HEALTH = "Health";
    public static final String CATEGORY_LIFESTYLE = "Lifestyle";

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

    // Default Lists
    public static final String DEFAULT_LIST_HOME = "Home";
    public static final String DEFAULT_LIST_BOOKMARKS = "Bookmarks";
    public static final String DEFAULT_LIST_READ_LATER = "Read Later";

    // Article States
    public static final String ARTICLE_STATE_UNREAD = "unread";
    public static final String ARTICLE_STATE_READ = "read";
    public static final String ARTICLE_STATE_BOOKMARKED = "bookmarked";
    public static final String ARTICLE_STATE_READ_LATER = "read_later";

    // Feed Update Status
    public static final String FEED_STATUS_ACTIVE = "active";
    public static final String FEED_STATUS_INACTIVE = "inactive";
    public static final String FEED_STATUS_ERROR = "error";

    // AI Summary Settings (for future implementation)
    public static final int SUMMARY_MAX_LENGTH = 500;
    public static final String SUMMARY_LANGUAGE = "en";

    private Constants() {
        // Prevent instantiation
    }
}
