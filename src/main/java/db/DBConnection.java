package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static HikariDataSource dataSource;

    // Database configuration
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/feedhawk?currentSchema=feedhawk";
    private static final String DB_USERNAME = "feedhawk_user";
    private static final String DB_PASSWORD = "feedhawk_pass";
    private static final String SCHEMA_NAME = "feedhawk";

    static {
        initializeDataSource();
    }

    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USERNAME);
            config.setPassword(DB_PASSWORD);

            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000); // 30 seconds
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute

            // PostgreSQL-specific performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");

            // Create tables if they don't exist
            createSchemaAndTablesIfNotExists();

        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Creates schema and all necessary tables if they don't exist
     */
    private static void createSchemaAndTablesIfNotExists() {
        logger.info("Checking and creating database schema and tables if needed...");
        
        try (Connection conn = dataSource.getConnection()) {
            // First create the schema if it doesn't exist
            createSchemaIfNotExists(conn);
            
            // Create tables in order due to foreign key dependencies
            createUsersTable(conn);
            createListsTable(conn);
            createSourcesTable(conn);
            createSubscriptionsTable(conn);
            createFeedItemsTable(conn);
            createReadStatusTable(conn);
            createUserBookmarksTable(conn);
            
            // Insert default data
            insertDefaultSources(conn);
            createDefaultUser(conn);
            
            logger.info("Database schema verification completed successfully");
            
        } catch (SQLException e) {
            logger.error("Failed to create database schema/tables", e);
            throw new RuntimeException("Database schema creation failed", e);
        }
    }
    
    private static void createSchemaIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE SCHEMA IF NOT EXISTS " + SCHEMA_NAME;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Schema '{}' ensured to exist", SCHEMA_NAME);
        }
    }

    private static void createUsersTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                user_id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT NOW(),
                last_login TIMESTAMP NULL
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Users table verified/created");
        }
    }

    private static void createListsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS lists (
                list_id SERIAL PRIMARY KEY,
                user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                name VARCHAR(50) NOT NULL,
                is_default BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT NOW()
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Lists table verified/created");
        }
    }

    private static void createSourcesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sources (
                source_id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                url TEXT UNIQUE NOT NULL,
                description TEXT,
                category VARCHAR(50),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT NOW()
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Sources table verified/created");
        }
    }

    private static void createSubscriptionsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS subscriptions (
                sub_id SERIAL PRIMARY KEY,
                list_id INT NOT NULL REFERENCES lists(list_id) ON DELETE CASCADE,
                source_id INT NOT NULL REFERENCES sources(source_id) ON DELETE CASCADE,
                subscribed_at TIMESTAMP DEFAULT NOW(),
                UNIQUE(list_id, source_id)
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Subscriptions table verified/created");
        }
    }

    private static void createFeedItemsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS feed_items (
                feed_id SERIAL PRIMARY KEY,
                source_id INT NOT NULL REFERENCES sources(source_id) ON DELETE CASCADE,
                title TEXT NOT NULL,
                content TEXT,
                summary TEXT,
                url TEXT UNIQUE NOT NULL,
                published_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT NOW(),
                UNIQUE(source_id, url)
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Feed items table verified/created");
        }
    }

    private static void createReadStatusTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS read_status (
                status_id SERIAL PRIMARY KEY,
                user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                feed_id INT NOT NULL REFERENCES feed_items(feed_id) ON DELETE CASCADE,
                is_read BOOLEAN DEFAULT FALSE,
                marked_read_at TIMESTAMP,
                UNIQUE(user_id, feed_id)
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Read status table verified/created");
        }
    }

    private static void createUserBookmarksTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_bookmarks (
                bookmark_id SERIAL PRIMARY KEY,
                user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                feed_id INT NOT NULL REFERENCES feed_items(feed_id) ON DELETE CASCADE,
                created_at TIMESTAMP DEFAULT NOW(),
                UNIQUE(user_id, feed_id)
            )
            """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("User bookmarks table verified/created");
        }
    }

    private static void insertDefaultSources(Connection conn) throws SQLException {
        // Check if sources already exist
        String checkSql = "SELECT COUNT(*) FROM sources";
        try (var stmt = conn.createStatement(); 
             var rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                logger.debug("Default sources already exist, skipping insertion");
                return;
            }
        }

        // Insert default RSS sources
        String sql = """
            INSERT INTO sources (name, url, description, category) VALUES
            ('TechCrunch', 'https://techcrunch.com/feed/', 'Latest technology news and startup information', 'Tech'),
            ('BBC News', 'http://feeds.bbci.co.uk/news/rss.xml', 'Latest breaking news and top stories', 'News'),
            ('Reuters', 'https://feeds.reuters.com/reuters/topNews', 'International breaking news and headlines', 'News'),
            ('The Verge', 'https://www.theverge.com/rss/index.xml', 'Technology, science, art, and culture', 'Tech'),
            ('Ars Technica', 'http://feeds.arstechnica.com/arstechnica/index', 'In-depth technology analysis and reviews', 'Tech'),
            ('NASA News', 'https://www.nasa.gov/rss/dyn/breaking_news.rss', 'Latest space and astronomy news', 'Science')
            """;
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Default RSS sources inserted successfully");
        }
    }

    private static void createDefaultUser(Connection conn) throws SQLException {
        // Check if users already exist
        String checkSql = "SELECT COUNT(*) FROM users";
        try (var stmt = conn.createStatement(); 
             var rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                logger.debug("Users already exist, skipping default user creation");
                return;
            }
        }

        // Create default demo user (password: demo123)
        // Using BCrypt hash for "demo123"
        String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMye7l6R2LAGhjDr/i3uFJaG/nNd5Kf4Z8q";
        
        String userSql = """
            INSERT INTO users (username, email, password_hash, created_at) 
            VALUES ('demo', 'demo@feedhawk.com', ?, NOW())
            """;
        
        try (var stmt = conn.prepareStatement(userSql)) {
            stmt.setString(1, hashedPassword);
            stmt.executeUpdate();
            logger.info("Default demo user created (username: demo, password: demo123)");
        }

        // Create default "Home" list for the demo user
        String listSql = """
            INSERT INTO lists (user_id, name, is_default, created_at) 
            VALUES (1, 'Home', true, NOW())
            """;
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(listSql);
            logger.info("Default 'Home' list created for demo user");
        }

        // Subscribe demo user to some default sources
        String subscriptionSql = """
            INSERT INTO subscriptions (list_id, source_id, subscribed_at) 
            SELECT 1, source_id, NOW() FROM sources WHERE category IN ('News', 'Tech') LIMIT 4
            """;
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(subscriptionSql);
            logger.info("Default subscriptions created for demo user");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
